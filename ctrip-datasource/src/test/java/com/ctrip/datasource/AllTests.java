package com.ctrip.datasource;

import com.ctrip.datasource.cluster.ClusterSwitchTest;
import com.ctrip.datasource.configure.*;
import com.ctrip.datasource.configure.qconfig.DalPropertiesProviderImplTest;
import com.ctrip.datasource.configure.qconfig.PoolPropertiesProviderImplTest;
import com.ctrip.datasource.datasource.BackgroundExecutor.DatasourceBackgroundExecutorTest;
import com.ctrip.datasource.datasource.ConnectionListener.CtripConnectionListenerTest;
import com.ctrip.datasource.datasource.DataSourceLocatorTest;
import com.ctrip.datasource.datasource.DataSourceValidatorTest;
import com.ctrip.datasource.datasource.MockQConfigProvider.ExceptionQConfigPoolPropertiesProviderTest;
import com.ctrip.datasource.dynamicdatasource.DalDataSourceFactoryTest;
import com.ctrip.datasource.dynamicdatasource.DataSourceConfigureEncryptTest;
import com.ctrip.datasource.dynamicdatasource.MGRDataSourceSwitchTest;
import com.ctrip.datasource.dynamicdatasource.QConfigConnectionStringProvider.QConfigConnectionStringProviderTest;
import com.ctrip.datasource.readonly.SqlServerReadonlyTest;
import com.ctrip.datasource.titan.DataSourceConfigureHelperTest;
import com.ctrip.datasource.titan.TitanDataSourceLocatorTest;
import com.ctrip.datasource.util.MysqlApiConnectionStringUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ctrip.datasource.mybatis.interceptor.ExecutorInterceptorTest;
import com.ctrip.datasource.mybatis.interceptor.StatementHandlerInterceptorTest;
import com.ctrip.datasource.mybatis.interceptor.StatementPrepareHandlerInterceptorTest;
import com.ctrip.datasource.spring.JavaConfigMybatisTest;
import com.ctrip.datasource.spring.XmlConfigMybatisTest;
import com.ctrip.datasource.titan.TitanServiceReaderTest;
import com.ctrip.datasource.util.DalEncrypterTest;
import com.ctrip.datasource.dynamicdatasource.DalPropertiesChangedTest;

@RunWith(Suite.class)
@SuiteClasses({
		TitanDataSourceLocatorTest.class,
		ExceptionQConfigPoolPropertiesProviderTest.class,
		DalPropertiesChangedTest.class,
		TitanServiceReaderTest.class,
		AllInOneConfigureReaderTest.class,
		ConnectionStringParserParserTest.class,
		CtripDalDataSourceTest.class,
		MysqlApiConnectionStringConfigureProviderTest.class,
		MysqlApiConnectionStringParserTest.class,
		MetricTest.class,
  		DalEncrypterTest.class,
		MysqlApiConnectionStringUtilsTest.class,
		JavaConfigMybatisTest.class,
  		XmlConfigMybatisTest.class,
  		ExecutorInterceptorTest.class,
  		StatementHandlerInterceptorTest.class,
  		StatementPrepareHandlerInterceptorTest.class,
		DalDataSourceFactoryTest.class,
		MGRDataSourceSwitchTest.class,
		QConfigConnectionStringProviderTest.class,
		CtripConnectionListenerTest.class,
		DataSourceValidatorTest.class,
		SqlServerReadonlyTest.class,
		DatasourceBackgroundExecutorTest.class,
		DalPropertiesProviderImplTest.class,
		PoolPropertiesProviderImplTest.class,
		DataSourceConfigureEncryptTest.class,
		ClusterSwitchTest.class,
		DataSourceLocatorTest.class,
		CtripClusterInfoProviderTest.class,
		CtripFxLocalResourceLoaderTest.class,
		CtripLocalClusterConfigProviderTest.class,
		CtripLocalConnectionStringProviderTest.class,
		CtripLocalDatabasePropertiesParserTest.class,
		CtripLocalResourceLoaderTest.class,
		DataSourceConfigureHelperTest.class
	})

public class AllTests {

}
