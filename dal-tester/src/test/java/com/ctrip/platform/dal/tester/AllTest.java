package com.ctrip.platform.dal.tester;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	com.ctrip.platform.dal.dao.dialet.test.MySqlHelperTest.class,
	com.ctrip.platform.dal.dao.unittests.AllTest.class,
	com.ctrip.platform.dal.parser.AllTest.class,
	com.ctrip.platform.dal.tester.client.AllTest.class,
	com.ctrip.platform.dal.tester.baseDao.AllTest.class,
	com.ctrip.platform.dal.tester.shard.AllTest.class,
	com.ctrip.platform.dal.dao.helpers.DalColumnMapRowMapperTest.class,
	com.ctrip.platform.dal.dao.helpers.DalCustomRowMapperTest.class,
	com.ctrip.platform.dal.dao.ha.HATest.class,
	com.ctrip.platform.dal.dao.markdown.AllTests.class,
	com.ctrip.platform.dal.dao.sqlbuilder.AllTests.class,
	com.ctrip.platform.dal.async.dao.AllTests.class,
	com.ctrip.platform.dal.dao.helpers.AllTests.class
})
public class AllTest {}
