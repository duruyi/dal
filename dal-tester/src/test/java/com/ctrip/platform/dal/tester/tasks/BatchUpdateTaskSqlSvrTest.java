package com.ctrip.platform.dal.tester.tasks;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class BatchUpdateTaskSqlSvrTest extends BatchUpdateTaskTestStub {
	private final static String DATABASE_NAME_SQLSVR = "dao_test_sqlsvr";
	
	public BatchUpdateTaskSqlSvrTest() {
		super(DATABASE_NAME_SQLSVR);
	}
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		SqlServerTestInitializer.setUpBeforeClass();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		SqlServerTestInitializer.tearDownAfterClass();
	}

	@Before
	public void setUp() throws Exception {
		SqlServerTestInitializer.setUp();
	}

	@After
	public void tearDown() throws Exception {
		SqlServerTestInitializer.tearDown();
	}
}
