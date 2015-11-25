package com.ctrip.platform.dal.dao.sqlbuilder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	DeleteSqlBuilderTest.class, 
	SelectSqlBuilderTest.class,
	UpdateSqlBuilderTest.class,
	AbstractBuilderTest.class})
public class AllTests {

}
