package com.ctrip.platform.dal.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BatchUpdateSp3TaskTest {

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
		DalHints hints = new DalHints();
		String[] insertSqls = null;
		insertSqls = new String[6];
		insertSqls[0] = "SET IDENTITY_INSERT "+ TABLE_NAME + " ON";
		insertSqls[1] = "DELETE FROM " + TABLE_NAME;
		for(int i = 0; i < 3; i ++) {
			int id = i;
			insertSqls[i+2] = "INSERT INTO " + TABLE_NAME +" ([PeopleID], [Name], [CityID], [ProvinceID], [CountryID])"
					+ " VALUES(" + id + ", " + "'test name' , 1, 1, 1)";
		}
		insertSqls[5] = "SET IDENTITY_INSERT "+ TABLE_NAME + " OFF";
		client.batchUpdate(insertSqls, hints.inShard(0));
	}

	@After
	public void tearDown() throws Exception {
		client.update("DELETE FROM " + TABLE_NAME, new StatementParameters(), new DalHints().inShard(0));
	}
	
	@Test
	public void testExecute() {
		BatchUpdateSp3Task<People> test = new BatchUpdateSp3Task<>();
		PeopleParser parser = new PeopleParser();
		test.initialize(parser);
		
		List<People> p = new ArrayList<>();
		
		for(int i = 0; i < 3; i++) {
			People p1 = new People();
		 	p1.setPeopleID((long)i);
		 	p1.setName("test");
		 	p1.setCityID(-1);
		 	p1.setProvinceID(-1);
		 	p1.setCountryID(-1);
		 	p.add(p1);
		}
		
		try {
			DalHints hints = new DalHints();
			test.execute(hints.inShard(0), getPojosFields(p, parser));
			
			DalTableDao<People> dao = new DalTableDao<>(parser);
			p = dao.query("1=1", new StatementParameters(), new DalHints().inAllShards());
			
			for(People pe: p) {
				Assert.assertTrue(-1==pe.getProvinceID());
				Assert.assertTrue(-1==pe.getCityID());
				Assert.assertTrue(-1==pe.getCountryID());
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testExecuteNullFields() {
		BatchUpdateSp3Task<People> test = new BatchUpdateSp3Task<>();
		PeopleParser parser = new PeopleParser();
		test.initialize(parser);
		
		List<People> p = new ArrayList<>();
		
		for(int i = 0; i < 3; i++) {
			People p1 = new People();
		 	p1.setPeopleID((long)i);
		 	p1.setName("test");
		 	p1.setCityID(null);
		 	p1.setProvinceID(null);
		 	p1.setCountryID(null);
		 	p.add(p1);
		}
		
		try {
			DalHints hints = new DalHints();
			test.execute(hints.inShard(0), getPojosFields(p, parser));
			
			DalTableDao<People> dao = new DalTableDao<>(parser);
			p = dao.query("1=1", new StatementParameters(), new DalHints().inShard(0));
			
			for(People pe: p) {
				Assert.assertTrue(1==pe.getProvinceID());
				Assert.assertTrue(1==pe.getCityID());
				Assert.assertTrue(1==pe.getCountryID());
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	private <T> Map<Integer, Map<String, ?>> getPojosFields(List<T> daoPojos, DalParser<T> parser) {
		Map<Integer, Map<String, ?>> pojoFields = new HashMap<>();
		if (null == daoPojos || daoPojos.size() < 1)
			return pojoFields;
		
		int i = 0;
		for (T pojo: daoPojos){
			pojoFields.put(i++, parser.getFields(pojo));
		}
		
		return pojoFields;
	}
}
