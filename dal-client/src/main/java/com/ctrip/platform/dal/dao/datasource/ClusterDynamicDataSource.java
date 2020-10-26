package com.ctrip.platform.dal.dao.datasource;

import com.ctrip.framework.dal.cluster.client.cluster.ClusterType;
import com.ctrip.framework.dal.cluster.client.cluster.DrcCluster;
import com.ctrip.framework.dal.cluster.client.config.LocalizationConfig;
import com.ctrip.framework.dal.cluster.client.database.ConnectionString;
import com.ctrip.framework.dal.cluster.client.database.Database;
import com.ctrip.framework.dal.cluster.client.database.DatabaseRole;
import com.ctrip.framework.dal.cluster.client.util.StringUtils;
import com.ctrip.platform.dal.common.enums.ForceSwitchedStatus;
import com.ctrip.platform.dal.dao.configure.ClusterInfo;
import com.ctrip.framework.dal.cluster.client.Cluster;
import com.ctrip.framework.dal.cluster.client.base.Listener;
import com.ctrip.framework.dal.cluster.client.cluster.ClusterSwitchedEvent;
import com.ctrip.platform.dal.dao.configure.*;
import com.ctrip.platform.dal.dao.configure.dalproperties.DalPropertiesLocator;
import com.ctrip.platform.dal.dao.datasource.cluster.*;
import com.ctrip.platform.dal.dao.helper.DalElementFactory;
import com.ctrip.platform.dal.dao.helper.ServiceLoaderHelper;
import com.ctrip.platform.dal.dao.log.Callback;
import com.ctrip.platform.dal.dao.log.DalLogTypes;
import com.ctrip.platform.dal.dao.log.ILogger;
import com.ctrip.platform.dal.exceptions.DalRuntimeException;
import com.ctrip.platform.dal.exceptions.UnsupportedFeatureException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class ClusterDynamicDataSource extends DataSourceDelegate implements DataSource,
        ClosableDataSource, SingleDataSourceWrapper, DataSourceConfigureChangeListener {

    private static final ILogger LOGGER = DalElementFactory.DEFAULT.getILogger();

    private static final String CAT_LOG_NAME_NORMAL = "createNormalDataSource:%s";
    private static final String CAT_LOG_NAME_DRC = "createDrcDataSource:%s";
    private static final String CAT_LOG_NAME_DRC_FAIL = "createDrcDataSource:EXCEPTION:%s";

    private ClusterInfo clusterInfo;
    private Cluster cluster;
    private DataSourceConfigureProvider provider;
    private LocalizationValidatorFactory factory;
    private AtomicReference<DataSource> dataSourceRef = new AtomicReference<>();

    public ClusterDynamicDataSource(ClusterInfo clusterInfo, Cluster cluster, DataSourceConfigureProvider provider,
                                    LocalizationValidatorFactory factory) {
        this.clusterInfo = clusterInfo;
        this.cluster = cluster;
        this.provider = provider;
        this.factory = factory;
        prepare();
    }

    protected void prepare() {
        cluster.addListener(event -> {
            try {
                close(dataSourceRef.getAndSet(createInnerDataSource()));
            } catch (Throwable t) {
                String msg = "Cluster switch listener error";
                LOGGER.error(msg, t);
                throw new DalRuntimeException(msg, t);
            }
        });
        dataSourceRef.set(createInnerDataSource());
    }

    protected DataSource createInnerDataSource() {
        if (cluster == null)
            throw new DalRuntimeException("null cluster");
        return cluster.getClusterType() != ClusterType.MGR ? createStandaloneDataSource() : createMultiHostDataSource();
    }

    protected DataSource createStandaloneDataSource() {
        DataSourceIdentity id = getStandaloneDataSourceIdentity(clusterInfo, cluster);
        DataSourceConfigure config = provider.getDataSourceConfigure(id);
        try {
            if (cluster.getClusterType() == ClusterType.DRC) {
                LocalizationConfig localizationConfig = cluster.getLocalizationConfig();
                LocalizationValidator validator = factory.createValidator(clusterInfo, localizationConfig);
                LOGGER.logEvent(DalLogTypes.DAL_DATASOURCE, String.format(CAT_LOG_NAME_DRC, clusterInfo.toString()), localizationConfig.toString());
                return new LocalizedDataSource(validator, id, config);
            }
        } catch (Throwable t) {
            LOGGER.logEvent(DalLogTypes.DAL_DATASOURCE, String.format(CAT_LOG_NAME_DRC_FAIL, clusterInfo.toString()), t.getMessage());
            throw t;
        }
        LOGGER.logEvent(DalLogTypes.DAL_DATASOURCE, String.format(CAT_LOG_NAME_NORMAL, clusterInfo.toString()), "");
        return new RefreshableDataSource(id, config);
    }

    protected DataSource createMultiHostDataSource() {
        if (cluster.dbShardingEnabled())
            throw new UnsupportedFeatureException("ClusterDataSource does not support sharding cluster, cluster name: " + cluster.getClusterName());
        DataSourceIdentity id = getMultiHostDataSourceIdentity(cluster);
        return new ClusterDataSource(id, cluster, provider);
    }

    @Override
    public void configChanged(DataSourceConfigureChangeEvent event) throws SQLException {
        DataSource ds = getDelegated();
        if (ds instanceof DataSourceConfigureChangeListener)
            ((DataSourceConfigureChangeListener) ds).configChanged(event);
    }

    @Override
    public SingleDataSource getSingleDataSource() {
        DataSource ds = getDelegated();
        if (ds instanceof SingleDataSourceWrapper)
            return ((SingleDataSourceWrapper) ds).getSingleDataSource();
        return null;
    }

    @Override
    public void forceRefreshDataSource(String name, DataSourceConfigure configure) {
        DataSource ds = getDelegated();
        if (ds instanceof SingleDataSourceWrapper)
            ((SingleDataSourceWrapper) ds).forceRefreshDataSource(name, configure);
    }

    @Override
    public void close() {
        close(getDelegated());
    }

    protected void close(DataSource dataSource) {
        if (dataSource instanceof ClosableDataSource) {
            ((ClosableDataSource) dataSource).close();
        }
    }

    @Override
    public DataSource getDelegated() {
        return dataSourceRef.get();
    }

    private DataSourceIdentity getStandaloneDataSourceIdentity(ClusterInfo clusterInfo, Cluster cluster) {
        if (clusterInfo.getRole() == DatabaseRole.MASTER)
            return new TraceableClusterDataSourceIdentity(cluster.getMasterOnShard(clusterInfo.getShardIndex()));
        else {
            List<Database> slaves = cluster.getSlavesOnShard(clusterInfo.getShardIndex());
            if (slaves == null || slaves.size() == 0)
                throw new IllegalStateException(String.format(
                        "slave is not found for cluster '%s', shard %d",
                        clusterInfo.getClusterName(), clusterInfo.getShardIndex()));
            if (slaves.size() > 1)
                throw new UnsupportedFeatureException(String.format(
                        "multi slaves are found for cluster '%s', shard %d, which is not supported yet",
                        clusterInfo.getClusterName(), clusterInfo.getShardIndex()));
            return new TraceableClusterDataSourceIdentity(slaves.iterator().next());
        }
    }

    private DataSourceIdentity getMultiHostDataSourceIdentity(Cluster cluster) {
        return new DefaultClusterIdentity(cluster);
    }

    // force switch

    private static final String FORCE_SWITCH = "ForceSwitch::forceSwitch:%s";
    private static final String GET_STATUS = "ForceSwitch::getStatus:%s";
    private static final String RESTORE = "ForceSwitch::restore:%s";
    private final Lock lock = new ReentrantLock();
    private final AtomicReference<HostSpec> currentHost = new AtomicReference<>();
    private static volatile ThreadPoolExecutor executor;

}
