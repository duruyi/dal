package com.ctrip.platform.dal.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctrip.platform.dal.dao.task.DefaultTaskContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ctrip.platform.dal.dao.task.SingleTask;

public abstract class BaseSingleDeleteTest {
    public abstract void setOptionTest();
	private static final String CALL_SP_BY_NAME = "callSpbyName";
	private static final String CALL_SP_BY_SQLSEVER = "callSpbySqlServerSyntax";
	private static final String CALL_SPT = "callSpt";

    private <T> SingleTask<T> getTest(DalParser<T> parser) {
    	CtripTaskFactory ctripTaskFactory=new CtripTaskFactory();
    	ctripTaskFactory.setCallSpt(false);
    	ctripTaskFactory.setCallSpbySqlServerSyntax(true);
    	ctripTaskFactory.setCallSpByName(false);
		Map<String,String> settings=new HashMap<>();
		settings.put(CALL_SP_BY_NAME,"false");
		settings.put(CALL_SP_BY_SQLSEVER,"true");
		settings.put(CALL_SPT,"false");
		ctripTaskFactory.setCtripTaskSettings(settings);
        return ctripTaskFactory.createSingleDeleteTask(parser);
    }
    
	private final static String DATABASE_NAME = "SimpleShard";
	
	private final static String TABLE_NAME = "People";
	
	private static DalClient client;
	static {
		try {
			DalClientFactory.initClientFactory();
			client = DalClientFactory.getClient(DATABASE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
        setOptionTest();
		String[] insertSqls = new String[4];
		insertSqls[0] = "SET IDENTITY_INSERT "+ TABLE_NAME + " ON";
		insertSqls[1] = "DELETE FROM " + TABLE_NAME;
		insertSqls[2] = "INSERT INTO " + TABLE_NAME +" ([PeopleID], [Name], [CityID], [ProvinceID], [CountryID])"
					+ " VALUES(" + 1 + ", " + "'test name' , 1, 1, 1)";
		insertSqls[3] = "SET IDENTITY_INSERT "+ TABLE_NAME + " OFF";
		client.batchUpdate(insertSqls, new DalHints().inShard(0));
		setUpShard();
	}

	public void setUpShard(){
		try {
			for(int i = 0; i < 2; i++) {
				for(int j = 0; j < 2; j++) {
					String tableName = TABLE_NAME + "_" + j;
					DalHints hints = new DalHints().inShard(i);
					String[] insertSqls = null;
					insertSqls = new String[6];
					insertSqls[0] = "SET IDENTITY_INSERT "+ tableName + " ON";
					insertSqls[1] = "DELETE FROM " + tableName;
					for(int k = 0; k < 3; k ++) {
						int id = k;
						insertSqls[k+2] = "INSERT INTO " + tableName +" ([PeopleID], [Name], [CityID], [ProvinceID], [CountryID])"
								+ " VALUES(" + id + ", " + "'test name' , " + j + ", 1, " + i + ")";
					}
					insertSqls[5] = "SET IDENTITY_INSERT "+ tableName + " OFF";
					client.batchUpdate(insertSqls, hints);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
		client.update("DELETE FROM " + TABLE_NAME, new StatementParameters(), new DalHints().inShard(0));
		tearDownShard();
	}
	
	public void tearDownShard() throws Exception {
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 2; j++) {
				String tableName = TABLE_NAME + "_" + j;
				client.update("DELETE FROM " + tableName, new StatementParameters(), new DalHints().inShard(i));
			}
		}
	}
	
	@Test
	public void testExecute() {
		PeopleParser parser = new PeopleParser();
        SingleTask<People> test = getTest(parser);
		
		People p1 = new People();
	 	p1.setPeopleID((long)1);
	 	p1.setName("test");
	 	p1.setCityID(-1);
	 	p1.setProvinceID(-1);
	 	p1.setCountryID(-1);

		try {
			test.execute(new DalHints().inShard(0), parser.getFields(p1), p1, new DefaultTaskContext());
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testExecuteShard() {
		PeopleParser parser = new PeopleParser("SimpleDbTableShard");
		DalTableDao<People> dao = new DalTableDao<>(parser);
		SingleTask<People> test = getTest(parser);
		
		try {
			for(int i = 0; i < 2; i++) {
				for(int j = 0; j < 2; j++) {
					DalHints hints = new DalHints().inShard(i).inTableShard(j);
					List<People> p = dao.query("1=1", new StatementParameters(), hints);
					Assert.assertTrue(p.size() == 3);
					
					for(People p1: p)
						test.execute(hints, parser.getFields(p1), p1, new DefaultTaskContext());
					
					hints = new DalHints().inShard(i).inTableShard(j);
					int c = dao.count("1=1", new StatementParameters(), hints).intValue();
					Assert.assertTrue(c == 0);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testExecuteShardByDao() {
		PeopleParser parser = new PeopleParser("SimpleDbTableShard");
		DalTableDao<People> dao = new DalTableDao<>(parser);
		
		try {
			for(int i = 0; i < 2; i++) {
				for(int j = 0; j < 2; j++) {
					DalHints hints = new DalHints().inShard(i).inTableShard(j);
					List<People> p = dao.query("1=1", new StatementParameters(), hints);
					Assert.assertTrue(p.size() == 3);
					dao.delete(hints, p);
					
					hints = new DalHints().inShard(i).inTableShard(j);
					int c = dao.count("1=1", new StatementParameters(), hints).intValue();
					Assert.assertTrue(c == 0);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testExecuteShardByDao2() {
		PeopleParser parser = new PeopleParser("SimpleDbTableShard");
		DalTableDao<People> dao = new DalTableDao<>(parser);
		
		try {
			List<People> pAll = new ArrayList<>();
			for(int i = 0; i < 2; i++) {
				for(int j = 0; j < 2; j++) {
					DalHints hints = new DalHints().inShard(i).inTableShard(j);
					List<People> p = dao.query("1=1", new StatementParameters(), hints);
					Assert.assertTrue(p.size() == 3);
					pAll.addAll(p);
				}
			}
			dao.delete(new DalHints(), pAll);
					
			for(int i = 0; i < 2; i++) {
				for(int j = 0; j < 2; j++) {
					DalHints hints = new DalHints().inShard(i).inTableShard(j);
					int c = dao.count("1=1", new StatementParameters(), hints).intValue();
					Assert.assertTrue(c == 0);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
