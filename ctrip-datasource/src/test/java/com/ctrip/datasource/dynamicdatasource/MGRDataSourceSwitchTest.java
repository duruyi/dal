package com.ctrip.datasource.dynamicdatasource;

import com.ctrip.datasource.configure.DalDataSourceFactory;
import com.ctrip.datasource.configure.MockConnectionStringConfigureProvider;
import com.ctrip.datasource.configure.MysqlApiConnectionStringConfigureProvider;
import com.ctrip.datasource.titan.TitanProvider;
import com.ctrip.datasource.util.CtripEnvUtils;
import com.ctrip.platform.dal.dao.DalClientFactory;
import com.ctrip.platform.dal.dao.configure.DalConnectionStringConfigure;
import com.ctrip.platform.dal.dao.configure.DataSourceConfigure;
import com.ctrip.platform.dal.dao.configure.FirstAidKit;
import com.ctrip.platform.dal.dao.configure.IDataSourceConfigure;
import com.ctrip.platform.dal.dao.datasource.*;
import com.ctrip.platform.dal.dao.helper.DalElementFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

public class MGRDataSourceSwitchTest {

    private static CtripEnvUtils envUtils = (CtripEnvUtils) DalElementFactory.DEFAULT.getEnvUtils();

    @Test
    public void testExecuteListenerSwitchDataSource() throws Exception {
        String mgrUrl1 = "jdbc:mysql://address=(type=master)(protocol=tcp)(host=10.2.7.196)(port=3306):3306:3306/";
        String mgrUrl2 = "jdbc:mysql://address=(type=master)(protocol=tcp)(host=10.2.7.187)(port=3306):3306:3306/";
        String mgrUrl3 = "jdbc:mysql://address=(type=master)(protocol=tcp)(host=10.2.7.184)(port=3306):3306:3306/";
        String normalUrl1 = "jdbc:mysql://localhost:3306/test";

        MockConnectionStringConfigureProvider provider = new MockConnectionStringConfigureProvider();
        DalDataSourceFactory factory = new DalDataSourceFactory();
        DataSource ds1 = factory.createVariableTypeDataSource(provider);

        DatabaseMetaData metaData1 = ds1.getConnection().getMetaData();
        String url1 = metaData1.getURL();
        Assert.assertTrue(mgrUrl1.equalsIgnoreCase(url1) || mgrUrl2.equalsIgnoreCase(url1) || mgrUrl3.equalsIgnoreCase(url1));

        provider.switchDataSource();

        Thread.sleep(2000);

        DatabaseMetaData metaData2 = ds1.getConnection().getMetaData();
        String url2 = metaData2.getURL();
        Assert.assertTrue(normalUrl1.equalsIgnoreCase(url2));
    }

    @Test
    public void testDynamicSwitchDataSource() throws Exception {
        String mgrJdbcUrl1 = "jdbc:mysql:replication://address=(type=master)(protocol=tcp)(host=10.2.7.196)(port=3306),address=((type=master)(protocol=tcp)(host=10.2.7.184)(port=3306)/kevin";
        String mgrJdbcUrl2 = "jdbc:mysql:replication://address=(type=master)(protocol=tcp)(host=10.2.7.196)(port=3306),address=((type=master)(protocol=tcp)(host=10.2.7.187)(port=3306)/kevin";
        //String mgrJdbcUrl3 = "jdbc:mysql:replication://address=(type=master)(protocol=tcp)(host=10.2.7.184)(port=3306),address=((type=master)(protocol=tcp)(host=10.2.7.187)(port=3306)/kevin";

        String mgrUrl1 = "jdbc:mysql://address=(type=master)(protocol=tcp)(host=10.2.7.196)(port=3306):3306:3306/";
        String mgrUrl2 = "jdbc:mysql://address=((type=master)(protocol=tcp)(host=10.2.7.187)(port=3306):3306:3306/";
        String mgrUrl3 = "jdbc:mysql://address=((type=master)(protocol=tcp)(host=10.2.7.184)(port=3306):3306:3306/";

        DynamicConnectionStringConfigureProvider provider = new DynamicConnectionStringConfigureProvider("unused");
        DalDataSourceFactory factory = new DalDataSourceFactory();
        DataSource ds1 = null;
        try {
            ds1 = factory.createVariableTypeDataSource(provider);
        } catch (Exception e) {
            provider.setConnectionStringSwitch(true);
            DataSourceIdentity id = new ApiDataSourceIdentity(provider);
            DalClientFactory.getDalConfigure().getLocator().getConnection(id);
            DataSourceLocator locator = new DataSourceLocator(new TitanProvider());
            ds1 = locator.getDataSource(id);
        }
        RefreshableDataSource refreshableDataSource = (RefreshableDataSource) ds1;

        provider.setUrl(mgrJdbcUrl1);
        Thread.sleep(6000);

        String testUrl1 = ((org.apache.tomcat.jdbc.pool.DataSource) refreshableDataSource.getSingleDataSource().getDataSource()).getUrl();
        Assert.assertEquals(mgrJdbcUrl1, testUrl1);
        DatabaseMetaData metaData1 = ds1.getConnection().getMetaData();
        for (int i = 0; i < 3; ++i) {
            String url1 = metaData1.getURL();
            Assert.assertTrue(mgrUrl1.equalsIgnoreCase(url1) || mgrUrl3.equalsIgnoreCase(url1));
        }

        provider.setUrl(mgrJdbcUrl2);
        Thread.sleep(6000);

        Assert.assertEquals(mgrJdbcUrl2, ((org.apache.tomcat.jdbc.pool.DataSource) refreshableDataSource.getSingleDataSource().getDataSource()).getUrl());
        DatabaseMetaData metaData2 = ds1.getConnection().getMetaData();
        for (int i = 0; i < 3; ++i) {
            String url2 = metaData2.getURL();
            Assert.assertTrue(mgrUrl1.equalsIgnoreCase(url2) || mgrUrl2.equalsIgnoreCase(url2));
        }
    }

    @Test
    public void testGetFirstAidKit() throws Exception {
        envUtils.setEnv("pro");
        DalDataSourceFactory factory = new DalDataSourceFactory();
        DataSource dataSource = factory.createVariableTypeDataSource(new MockConnectionStringConfigureProvider(), true);
        Assert.assertTrue(dataSource instanceof ForceSwitchableDataSource);
        Assert.assertTrue(((ForceSwitchableDataSource) dataSource).getSingleDataSource()
                .getDataSourceConfigure().getConnectionUrl().startsWith("jdbc:mysql:replication://"));
        FirstAidKit kit = ((ForceSwitchableDataSource) dataSource).getFirstAidKit();
        Assert.assertTrue(kit instanceof IDataSourceConfigure);
        Assert.assertTrue(((IDataSourceConfigure) kit).getConnectionUrl().startsWith("jdbc:mysql:replication://"));
        envUtils.setEnv(null);
    }

}
