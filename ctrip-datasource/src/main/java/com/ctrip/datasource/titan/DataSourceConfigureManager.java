package com.ctrip.datasource.titan;

import com.ctrip.datasource.configure.ConnectionStringProvider;
import com.ctrip.datasource.configure.PoolPropertiesProvider;
import com.ctrip.datasource.util.DalEncrypter;
import com.ctrip.platform.dal.common.enums.SourceType;
import com.ctrip.platform.dal.dao.client.LoggerAdapter;
import com.ctrip.platform.dal.dao.configure.DataSourceConfigure;
import com.ctrip.platform.dal.dao.configure.DataSourceConfigureChangeEvent;
import com.ctrip.platform.dal.dao.configure.DataSourceConfigureChangeListener;
import com.ctrip.platform.dal.dao.configure.DataSourceConfigureLocator;
import com.ctrip.platform.dal.dao.configure.DataSourceConfigureParser;
import com.ctrip.platform.dal.dao.datasource.ConnectionStringChanged;
import com.ctrip.platform.dal.dao.datasource.PoolPropertiesChanged;
import com.ctrip.platform.dal.dao.helper.ConnectionStringKeyNameHelper;
import com.ctrip.platform.dal.exceptions.DalConfigException;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.status.ProductVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataSourceConfigureManager {
    private volatile static DataSourceConfigureManager manager = null;

    public synchronized static DataSourceConfigureManager getInstance() {
        if (manager == null) {
            manager = new DataSourceConfigureManager();
        }
        return manager;
    }

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfigureManager.class);
    private static final String DAL_LOCAL_DATASOURCE = "DAL.local.datasource";

    private static final String DAL_DYNAMIC_DATASOURCE = "DAL";
    private static final String DAL_NOTIFY_LISTENER = "DataSource::notifyListener";
    private static final String DAL_NOTIFY_LISTENER_START = "DataSource.notifyListener.start";
    private static final String DAL_NOTIFY_LISTENER_END = "DataSource.notifyListener.end";

    private static final String DAL_REFRESH_DATASOURCE = "DataSource::refreshDataSourceConfig";
    private static final String DATASOURCE_OLD_CONNECTIONURL = "DataSource::oldConnectionUrl";
    private static final String DATASOURCE_NEW_CONNECTIONURL = "DataSource::newConnectionUrl";
    private static final String DATASOURCE_OLD_CONFIGURE = "DataSource::oldConfigure";
    private static final String DATASOURCE_NEW_CONFIGURE = "DataSource::newConfigure";

    private DataSourceConfigureLocator dataSourceConfigureLocator = DataSourceConfigureLocator.getInstance();
    private DataSourceConfigureParser dataSourceConfigureParser = DataSourceConfigureParser.getInstance();

    private ConnectionStringProvider connectionStringProvider = ConnectionStringProvider.getInstance();
    private PoolPropertiesProvider poolPropertiesProvider = PoolPropertiesProvider.getInstance();
    private DalEncrypter encrypter = null;
    private volatile boolean isInitialized = false;

    private Map<String, DataSourceConfigureChangeListener> dataSourceConfigureChangeListeners =
            new ConcurrentHashMap<>();

    private Map<String, SourceType> keyNameMap = new ConcurrentHashMap<>();
    private Set<String> listenerKeyNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    // Single-thread thread pool,used as queue.
    private ThreadPoolExecutor executor =
            new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public synchronized void initialize(Map<String, String> settings) throws Exception {
        if (isInitialized)
            return;

        encrypter = new DalEncrypter(LoggerAdapter.DEFAULT_SECERET_KEY);
        connectionStringProvider.initialize(settings);
        isInitialized |= true;
    }

    public DataSourceConfigure getDataSourceConfigure(String name) {
        return dataSourceConfigureLocator.getDataSourceConfigure(name);
    }

    public synchronized void setup(Set<String> dbNames, SourceType sourceType) {
        Set<String> names = null;
        try {
            names = getFilteredNames(dbNames, sourceType);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        if (names == null || names.isEmpty())
            return;

        if (dataSourceConfigureParser.isDataSourceXmlExist())
            ProductVersionManager.getInstance().register(DAL_LOCAL_DATASOURCE, connectionStringProvider.getAppId());

        dataSourceConfigureLocator.addDataSourceConfigureKeySet(names);
        Map<String, DataSourceConfigure> configures =
                connectionStringProvider.initializeConnectionStrings(names, sourceType);
        configures = mergeDataSourceConfigures(configures);
        addDataSourceConfigures(configures);

        if (sourceType == SourceType.Remote) {
            addConnectionStringChangedListeners(names);
            addDataSourceConfigureChangeListeners();
        }

        connectionStringProvider.info("--- End datasource config  ---");
    }

    private Set<String> getFilteredNames(Set<String> names, SourceType sourceType) throws Exception {
        Set<String> set = new HashSet<>();
        if (names == null || names.isEmpty())
            return set;

        for (String name : names) {
            if (keyNameMap.containsKey(name)) {
                SourceType st = keyNameMap.get(name);
                if (st != sourceType) {
                    String msg =
                            String.format("Key %s is used in both local and remote mode which is prohibited.", name);
                    Exception e = new RuntimeException(msg);
                    Cat.logError(e);
                    logger.error(msg, e);
                    throw e;
                }
                continue;
            }

            keyNameMap.put(name, sourceType);
            set.add(name);
        }

        return set;
    }

    private Map<String, DataSourceConfigure> mergeDataSourceConfigures(Map<String, DataSourceConfigure> map) {
        if (map == null || map.isEmpty())
            return null;

        Map<String, DataSourceConfigure> configures = new HashMap<>();
        for (Map.Entry<String, DataSourceConfigure> entry : map.entrySet()) {
            String key = entry.getKey();
            connectionStringProvider.info("--- Key datasource config for " + key + " ---");
            DataSourceConfigure configure = poolPropertiesProvider.mergeDataSourceConfigure(entry.getValue());
            configures.put(key, configure);
        }

        return configures;
    }

    private void addDataSourceConfigures(Map<String, DataSourceConfigure> map) {
        if (map == null || map.isEmpty())
            return;

        for (Map.Entry<String, DataSourceConfigure> entry : map.entrySet()) {
            dataSourceConfigureLocator.addDataSourceConfigure(entry.getKey(), entry.getValue());
        }
    }

    private void addConnectionStringChangedListeners(Set<String> names) {
        if (names == null || names.isEmpty())
            return;

        for (final String name : names) {
            // double check to avoid adding listener multiple times
            if (listenerKeyNames.contains(name))
                continue;

            connectionStringProvider.addConnectionStringChangedListener(name, new ConnectionStringChanged() {
                @Override
                public void onChanged(Map<String, String> map) {
                    addConnectionStringNotifyTask(name, map);
                }
            });

            listenerKeyNames.add(name);
        }
    }

    private void addConnectionStringNotifyTask(String name, Map<String, String> map) {
        String transactionName = String.format("%s:%s", DAL_NOTIFY_LISTENER, name);
        Transaction transaction = Cat.newTransaction(DAL_DYNAMIC_DATASOURCE, transactionName);
        Cat.logEvent(DAL_DYNAMIC_DATASOURCE, transactionName, Message.SUCCESS, DAL_NOTIFY_LISTENER_START);
        if (map != null) {
            String normalConnectionString = map.get(ConnectionStringProvider.TITAN_KEY_NORMAL);
            String failoverConnectionString = map.get(ConnectionStringProvider.TITAN_KEY_FAILOVER);
            transaction.addData(encrypter.desEncrypt(normalConnectionString));
            transaction.addData(encrypter.desEncrypt(failoverConnectionString));
        }

        Set<String> names = new HashSet<>();
        names.add(name);
        try {
            addNotifyTask(names);
            Cat.logEvent(DAL_DYNAMIC_DATASOURCE, transactionName, Message.SUCCESS, DAL_NOTIFY_LISTENER_END);
            transaction.setStatus(Transaction.SUCCESS);
        } catch (Throwable e) {
            DalConfigException exception = new DalConfigException(e);
            transaction.setStatus(exception);
            Cat.logError(exception);
            logger.error(String.format("DalConfigException:%s", e.getMessage()), exception);
        } finally {
            transaction.complete();
        }
    }

    private void addDataSourceConfigureChangeListeners() {
        poolPropertiesProvider.addPoolPropertiesChangedListener(new PoolPropertiesChanged() {
            @Override
            public void onChanged(Map<String, String> map) {
                Transaction t = Cat.newTransaction(DAL_DYNAMIC_DATASOURCE, DAL_NOTIFY_LISTENER);
                t.addData(DAL_NOTIFY_LISTENER_START);
                try {
                    addNotifyTask(null);
                    t.addData(DAL_NOTIFY_LISTENER_END);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Throwable e) {
                    String msg = "DataSourceConfigureProcessor addChangeListener warn:" + e.getMessage();
                    logger.warn(msg, e);
                } finally {
                    t.complete();
                }
            }
        });
    }

    private void addNotifyTask(final Set<String> names) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean connectionStringChanged = true;

                    // connection string changed
                    if (names != null && names.size() > 0) {
                        executeNotifyTask(names, connectionStringChanged);
                    } else { // datasource.properties changed
                        Set<String> keySet = dataSourceConfigureLocator.getDataSourceConfigureKeySet();
                        connectionStringChanged &= false;
                        executeNotifyTask(keySet, connectionStringChanged);
                    }
                } catch (Throwable e) {
                    Cat.logError(e);
                }
            }
        });
    }

    private void executeNotifyTask(Set<String> names, boolean isConnectionStringChanged) throws Exception {
        if (names == null || names.isEmpty())
            return;

        Map<String, DataSourceConfigure> configures = connectionStringProvider.getConnectionStrings(names);
        poolPropertiesProvider.refreshPoolProperties();
        configures = mergeDataSourceConfigures(configures);

        Map<String, DataSourceConfigureChangeListener> listeners =
                copyChangeListeners(dataSourceConfigureChangeListeners);

        for (String name : names) {
            String keyName = ConnectionStringKeyNameHelper.getKeyName(name);
            String transactionName = String.format("%s:%s", DAL_REFRESH_DATASOURCE, name);
            Transaction transaction = Cat.newTransaction(DAL_DYNAMIC_DATASOURCE, transactionName);

            try {
                // old configure
                DataSourceConfigure oldConfigure = getDataSourceConfigure(name);
                String oldVersion = oldConfigure.getVersion();
                String oldConnectionUrl = oldConfigure.toConnectionUrl();

                // log
                transaction.addData(DATASOURCE_OLD_CONNECTIONURL, String.format("%s:%s", name, oldConnectionUrl));
                Cat.logEvent(DAL_DYNAMIC_DATASOURCE, transactionName, Message.SUCCESS,
                        String.format("%s:%s:%s", DATASOURCE_OLD_CONNECTIONURL, name, oldConnectionUrl));
                transaction.addData(DATASOURCE_OLD_CONFIGURE,
                        String.format("%s:%s", name, poolPropertiesProvider.mapToString(oldConfigure.toMap())));
                Cat.logEvent(DAL_DYNAMIC_DATASOURCE, transactionName, Message.SUCCESS, String.format("%s:%s:%s",
                        DATASOURCE_OLD_CONFIGURE, name, poolPropertiesProvider.mapToString(oldConfigure.toMap())));

                // new configure
                DataSourceConfigure newConfigure = configures.get(keyName);
                String newVersion = newConfigure.getVersion();
                String newConnectionUrl = newConfigure.toConnectionUrl();

                // log
                transaction.addData(DATASOURCE_NEW_CONNECTIONURL, String.format("%s:%s", name, newConnectionUrl));
                Cat.logEvent(DAL_DYNAMIC_DATASOURCE, transactionName, Message.SUCCESS,
                        String.format("%s:%s:%s", DATASOURCE_NEW_CONNECTIONURL, name, newConnectionUrl));
                transaction.addData(DATASOURCE_NEW_CONFIGURE,
                        String.format("%s:%s", name, poolPropertiesProvider.mapToString(newConfigure.toMap())));
                Cat.logEvent(DAL_DYNAMIC_DATASOURCE, transactionName, Message.SUCCESS, String.format("%s:%s:%s",
                        DATASOURCE_NEW_CONFIGURE, name, poolPropertiesProvider.mapToString(newConfigure.toMap())));

                transaction.setStatus(Transaction.SUCCESS);

                // compare version of connection string
                if (isConnectionStringChanged && oldVersion != null && newVersion != null) {
                    if (oldVersion.equals(newVersion)) {
                        String msg = String.format("New version of %s equals to old version.", name);
                        Cat.logEvent(DAL_DYNAMIC_DATASOURCE, transactionName, Message.SUCCESS, msg);
                        logger.info(msg);
                        continue;
                    }
                }

                dataSourceConfigureLocator.addDataSourceConfigure(name, newConfigure);

                DataSourceConfigureChangeListener listener = listeners.get(keyName);
                if (listener == null) {
                    String msg = String.format("Listener of %s is null.", keyName);
                    Exception exception = new RuntimeException(msg);
                    Cat.logError(exception);
                    logger.error(msg, exception);
                    throw exception;
                }

                DataSourceConfigureChangeEvent event =
                        new DataSourceConfigureChangeEvent(name, newConfigure, oldConfigure);
                listener.configChanged(event);
            } catch (Throwable e) {
                transaction.setStatus(e);
                Cat.logError(e);
                logger.error(e.getMessage(), e);
                throw e;
            } finally {
                transaction.complete();
            }
        }
    }

    public void register(String dbName, DataSourceConfigureChangeListener listener) {
        String keyName = ConnectionStringKeyNameHelper.getKeyName(dbName);
        dataSourceConfigureChangeListeners.put(keyName, listener);
    }

    private Map<String, DataSourceConfigureChangeListener> copyChangeListeners(
            Map<String, DataSourceConfigureChangeListener> map) {
        if (map == null || map.isEmpty())
            return new ConcurrentHashMap<>();

        return new ConcurrentHashMap<>(map);
    }

}
