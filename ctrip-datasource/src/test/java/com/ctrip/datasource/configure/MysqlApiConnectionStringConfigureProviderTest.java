package com.ctrip.datasource.configure;

import com.ctrip.datasource.titan.DataSourceConfigureManager;
import com.ctrip.datasource.util.EnvUtil;
import com.ctrip.framework.dal.cluster.client.base.Listener;
import com.ctrip.platform.dal.dao.configure.DalConnectionStringConfigure;
import com.ctrip.platform.dal.dao.configure.DataSourceConfigure;
import com.ctrip.platform.dal.dao.configure.DataSourceConfigureLocator;
import com.ctrip.platform.dal.dao.configure.PropertiesWrapper;
import com.ctrip.platform.dal.dao.datasource.ApiDataSourceIdentity;
import com.ctrip.platform.dal.dao.datasource.ConnectionStringConfigureProvider;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

public class MysqlApiConnectionStringConfigureProviderTest {

    private static final String DB_NAME_1 = "qconfig";
    private static final String DB_NAME_2 = "fxdalclusterbenchmarkdb";
    private static final String DB_NAME_3 = "fxqconfigtestdb";
    private static final String DB_NAME_MGR = "kevin";

    @Test
    public void testConnectionString() throws Exception {
        String mgrUrl = "jdbc:mysql:replication://address=(type=master)(protocol=tcp)(host=10.9.72.67)(port=55944)," +
                "address=(type=master)(protocol=tcp)(host=10.25.82.137)(port=55944)," +
                "address=(type=master)(protocol=tcp)(host=10.60.53.211)(port=55944)/qconfig" +
                "?useUnicode=true&characterEncoding=UTF-8" +
                "&loadBalanceStrategy=serverAffinity&serverAffinityOrder="+
                "address=(type=master)(protocol=tcp)(host=10.25.82.137)(port=55944):3306," +
                "address=(type=master)(protocol=tcp)(host=10.60.53.211)(port=55944):3306," +
                "address=(type=master)(protocol=tcp)(host=10.9.72.67)(port=55944):3306";
        ConnectionStringConfigureProvider provider = new MysqlApiConnectionStringConfigureProvider(DB_NAME_1);
        EnvUtil.setEnv("pro");
        DalConnectionStringConfigure configure = provider.getConnectionString();
        Assert.assertEquals(configure.getConnectionUrl(), mgrUrl);
        EnvUtil.setEnv(null);
    }

    @Test
    public void testCustomGetDataSourceConfigure() {
        ConnectionStringConfigureProvider provider = new MockConnectionStringConfigureProvider();
        DataSourceConfigureLocator locator = DataSourceConfigureManager.getInstance().getDataSourceConfigureLocator();
        DataSourceConfigure dataSourceConfigure = locator.getDataSourceConfigure(new ApiDataSourceIdentity(provider));
        Assert.assertNotNull(dataSourceConfigure);
    }

    @Test
    public void testCreateDataSource() throws Exception {
        DalDataSourceFactory factory = new DalDataSourceFactory();
        DataSource dataSource = factory.createVariableTypeDataSource(DB_NAME_1);
        Assert.assertNotNull(dataSource);
    }

    @Test
    public void testCreateMGRDataSource() throws Exception {
        DalDataSourceFactory factory = new DalDataSourceFactory();
        EnvUtil.setEnv("pro");
        DataSource dataSource = factory.createVariableTypeDataSource(DB_NAME_3);
        Assert.assertNotNull(dataSource);
        EnvUtil.setEnv(null);
    }

    @Test
    public void testCreateMGRDataSourceCustomProvider() throws Exception {
        String mgrUrl = "jdbc:mysql://address=(type=master)(protocol=tcp)(host=10.2.7.184)(port=3306):3306:3306/";
        DalDataSourceFactory factory = new DalDataSourceFactory();
        DataSource dataSource = factory.createVariableTypeDataSource(new MockConnectionStringConfigureProvider());

        Assert.assertNotNull(dataSource);
        for (int i = 0; i < 5; ++i) {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            String url = metaData.getURL();
            Assert.assertTrue(mgrUrl.equalsIgnoreCase(url));
        }
    }

    @Test
    public void testDBModelChange() throws Exception {
        String fixMgrUrl = "jdbc:mysql:replication://address=(type=master)(protocol=tcp)(host=10.8.37.82)(port=55944),address=(type=master)(protocol=tcp)(host=10.25.91.204)(port=55944),address=(type=master)(protocol=tcp)(host=10.60.45.198)(port=55944)/fxdalclusterbenchmarkdb?useUnicode=true&characterEncoding=UTF-8" +
                "&loadBalanceStrategy=serverAffinity&serverAffinityOrder=address=(type=master)(protocol=tcp)(host=10.25.91.204)(port=55944):3306,address=(type=master)(protocol=tcp)(host=10.60.45.198)(port=55944):3306,address=(type=master)(protocol=tcp)(host=10.8.37.82)(port=55944):3306";
        String fixNormalUrl = "jdbc:mysql://fxdalclusterbenchmark.mysql.db.ctripcorp.com:55944/fxdalclusterbenchmarkdb?useUnicode=true&characterEncoding=UTF-8";
        EnvUtil.setEnv("pro");
        ConnectionStringConfigureProvider provider = new MysqlApiConnectionStringConfigureProvider(DB_NAME_2);
        /*provider.addListener(new Listener<DalConnectionStringConfigure>() {
            @Override
            public void onChanged(DalConnectionStringConfigure current) {
                String mgrUrl = current.getConnectionUrl();
            }
        });*/
        DalConnectionStringConfigure configure = provider.getConnectionString();
        String normalUrl = configure.getConnectionUrl();
        Assert.assertEquals(normalUrl, fixNormalUrl);
        DataSourceConfigureLocator dataSourceConfigureLocator = DataSourceConfigureManager.getInstance().getDataSourceConfigureLocator();
        PropertiesWrapper propertiesWrapper = dataSourceConfigureLocator.getPoolProperties();
        propertiesWrapper.getDatasourceProperties().get(DB_NAME_2).setProperty("dbModel", "mgr");

        String mgrUrl = provider.getConnectionString().getConnectionUrl();
        Assert.assertEquals(mgrUrl, fixMgrUrl);
        EnvUtil.setEnv(null);
    }
}
