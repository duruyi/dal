package com.ctrip.platform.dal.tester.baseDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.freeway.config.LogConfig;
import com.ctrip.freeway.logging.ILog;
import com.ctrip.freeway.logging.LogManager;
import com.ctrip.platform.dal.dao.DalClient;
import com.ctrip.platform.dal.dao.DalClientFactory;
import com.ctrip.platform.dal.dao.DalHintEnum;
import com.ctrip.platform.dal.dao.DalHints;
import com.ctrip.platform.dal.dao.DalResultSetExtractor;
import com.ctrip.platform.dal.dao.StatementParameters;
import com.ctrip.platform.dal.dao.logging.DalEventEnum;

public class DirectClientDaoShardTest {
	public static void test() {
		try {
			DalClient client = DalClientFactory.getClient("AbacusDB_INSERT_1");
			
			String sql = "select * from AbacusAddInfoLog";
			
			StatementParameters parameters = new StatementParameters();
			DalHints hints = new DalHints();
			Map<String, Integer> colValues = new HashMap<String, Integer>();
			colValues.put("user_id", 0);
			hints.set(DalHintEnum.shardColValues, colValues);

			client.query(sql, parameters, hints, new DalResultSetExtractor<Object>() {
				@Override
				public Object extract(ResultSet rs) throws SQLException {
					while(rs.next()){
						rs.getObject(1);
					}
					return null;
				}
				
			});
			
			hints = new DalHints();
			colValues = new HashMap<String, Integer>();
			colValues.put("user_id", 2);
			hints.masterOnly();
			hints.set(DalHintEnum.shardColValues, colValues);

			client.query(sql, parameters, hints, new DalResultSetExtractor<Object>() {
				@Override
				public Object extract(ResultSet rs) throws SQLException {
					while(rs.next()){
						rs.getObject(1);
					}
					return null;
				}
				
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void test2() {
		try {
			DalClient client = DalClientFactory.getClient("AbacusDB_INSERT_1");
			StatementParameters parameters = new StatementParameters();
			DalHints hints = new DalHints();
			//String delete = "update AbacusAddInfoLog set PNR='dafas' where id = 100";
			String select = "select PNR from AbacusAddInfoLog where LOGID = 100";
			String update = "update AbacusAddInfoLog set PNR='dafas11' where LOGID = 100";
			String restore = "update AbacusAddInfoLog set PNR='dafas' where LOGID = 100";
			
			hints = new DalHints();
			Map<String, Integer> colValues = new HashMap<String, Integer>();
			colValues.put("user_id", 0);
			hints.set(DalHintEnum.shardColValues, colValues);

			client.update(update, parameters, hints);
			
			client.query(select, parameters, hints, new DalResultSetExtractor<Object>() {
				@Override
				public Object extract(ResultSet rs) throws SQLException {
					while(rs.next()){
						System.out.println(rs.getObject(1));
					}
					return null;
				}
				
			});
			

			client.update(restore, parameters, hints);
			
			client.query(select, parameters, hints, new DalResultSetExtractor<Object>() {
				@Override
				public Object extract(ResultSet rs) throws SQLException {
					while(rs.next()){
						System.out.println(rs.getObject(1));
					}
					return null;
				}
				
			});
						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ILog logger = LogManager.getLogger("DAL Java Client");
		logger.debug("test");
		System.out.print(LogConfig.getAppID());
		try {
			DalClientFactory.initClientFactory("d:/DalMult.config");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		test();
		test2();

		System.exit(0);
	}
}