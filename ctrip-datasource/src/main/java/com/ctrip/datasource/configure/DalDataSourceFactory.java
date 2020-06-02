package com.ctrip.datasource.configure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.ctrip.datasource.titan.TitanDataSourceLocator;
import com.ctrip.datasource.titan.TitanProvider;
import com.ctrip.framework.dal.cluster.client.database.DatabaseRole;
import com.ctrip.platform.dal.dao.configure.ClusterInfo;
import com.ctrip.platform.dal.dao.datasource.*;

public class DalDataSourceFactory {
    private static final String IGNORE_EXTERNAL_EXCEPTION = "ignoreExternalException";

    /**
     * Create DataSource for given name. The appid and titan url will be discoved by framework foundation
     * 
     * @param allInOneKey
     * @return DataSource
     * @throws Exception
     */
    public DataSource createDataSource(String allInOneKey) throws Exception {
        return createDataSource(allInOneKey, null, null);
    }

    public DataSource createDataSource(String allInOneKey, boolean isForceInitialize) throws Exception {
        return createDataSource(allInOneKey, null, null, isForceInitialize);
    }

    /**
     * Create DataSource for given name. In case user has clog or cat configured. The name will be same for both PROD
     * and DEV environment
     * 
     * @param allInOneKey
     * @param svcUrl
     * @return DataSource
     * @throws Exception
     */
    public DataSource createDataSource(String allInOneKey, String svcUrl) throws Exception {
        return createDataSource(allInOneKey, svcUrl, null);
    }

    /**
     * Create DataSource for given name. In case user has no clog or cat configured. The name will be same for both PROD
     * and DEV environment
     * 
     * @param allInOneKey
     * @param svcUrl
     * @param appid
     * @return DataSource
     * @throws Exception
     */
    public DataSource createDataSource(String allInOneKey, String svcUrl, String appid) throws Exception {
        return createDataSource(allInOneKey, svcUrl, appid, false);
    }

    /**
     * support create mgr or standalone datasource
     *
     * @param dbName
     * @return DataSource
     * @throws Exception
     */
    public DataSource createVariableTypeDataSource(String dbName) throws Exception {
        return createVariableTypeDataSource(new MysqlApiConnectionStringConfigureProvider(dbName), false);
    }

    /**
     * support create mgr or standalone datasource
     *
     * @param dbName
     * @param isForceInitialize
     * @return DataSource
     * @throws Exception
     */
    public DataSource createVariableTypeDataSource(String dbName, boolean isForceInitialize) throws Exception {
        return createVariableTypeDataSource(new MysqlApiConnectionStringConfigureProvider(dbName), isForceInitialize);
    }

    /**
     * support create mgr or standalone datasource
     *
     * @param connectionStringConfigureProvider
     * @return DataSource
     * @throws Exception
     */
    public DataSource createVariableTypeDataSource(ConnectionStringConfigureProvider connectionStringConfigureProvider) throws Exception {
        return createVariableTypeDataSource(connectionStringConfigureProvider, false);
    }

    /**
     * support create mgr or standalone datasource
     *
     * @param connectionStringConfigureProvider
     * @return DataSource
     * @throws Exception
     */
    public DataSource createVariableTypeDataSource(ConnectionStringConfigureProvider connectionStringConfigureProvider, boolean isForceInitialize) throws Exception {
        TitanProvider provider = initTitanProvider(isForceInitialize);

        Set<String> names = new HashSet<>();
        provider.setup(names);

        DataSourceIdentity id = new ApiDataSourceIdentity(connectionStringConfigureProvider);
        DataSourceLocator locator = new DataSourceLocator(provider, isForceInitialize);
        return locator.getDataSource(id);
    }

    /**
     * Get or create the DataSource for the only master of the specified cluster.
     * Exception will be thrown when the cluster is sharding.
     * @param clusterName dal cluster name
     * @return DataSource
     * @throws Exception
     */
    public DataSource getOrCreateNonShardingDataSource(String clusterName) throws Exception {
        return null;
    }

    public DataSource createDataSource(String allInOneKey, String svcUrl, String appid, boolean isForceInitialize) throws Exception {
        TitanProvider provider = initTitanProvider(isForceInitialize);

        Set<String> names = new HashSet<>();
        ClusterInfo clusterInfo = provider.tryGetClusterInfo(allInOneKey);

        if (clusterInfo == null || clusterInfo.getRole() != DatabaseRole.MASTER)
            names.add(allInOneKey);

        provider.setup(names);
        DataSourceLocator locator = new DataSourceLocator(provider, isForceInitialize);

        if (clusterInfo == null || clusterInfo.getRole() != DatabaseRole.MASTER)
            return locator.getDataSource(allInOneKey);
        else
            return locator.getDataSource(clusterInfo);
    }

    private TitanProvider initTitanProvider(boolean isForceInitialize) throws Exception {
        TitanProvider provider = new TitanProvider();
        Map<String, String> settings = new HashMap<>();
        settings.put(IGNORE_EXTERNAL_EXCEPTION, String.valueOf(isForceInitialize));
        provider.initialize(settings);
        provider.setSourceTypeByEnv();
        return provider;
    }

    /**
     * This is only for cross environment usage
     * 
     * @param allInOneKey
     * @param svcUrl
     * @return
     * @throws Exception
     */
    public DataSource createTitanDataSource(String allInOneKey, String svcUrl) throws Exception {
        return new TitanDataSourceLocator().getDataSource(svcUrl, allInOneKey);
    }
}