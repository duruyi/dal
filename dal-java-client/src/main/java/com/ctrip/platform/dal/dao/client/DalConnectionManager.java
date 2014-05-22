package com.ctrip.platform.dal.dao.client;

import java.awt.color.CMMException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ctrip.datasource.locator.DataSourceLocator;
import com.ctrip.platform.dal.common.db.DruidDataSourceWrapper;
import com.ctrip.platform.dal.dao.DalHintEnum;
import com.ctrip.platform.dal.dao.DalHints;
import com.ctrip.platform.dal.dao.configure.DalConfigure;
import com.ctrip.platform.dal.dao.configure.DatabaseSet;
import com.ctrip.platform.dal.dao.logging.DalEventEnum;
import com.ctrip.platform.dal.dao.logging.LogEntry;
import com.ctrip.platform.dal.dao.logging.Logger;
import com.ctrip.platform.dal.dao.strategy.DalShardStrategy;

public class DalConnectionManager {
	private DalConfigure config;
	private String logicDbName;
	private DruidDataSourceWrapper connPool;

	public DalConnectionManager(String logicDbName, DalConfigure config) {
		this.logicDbName = logicDbName;
		this.config = config;
	}
	
	public DalConnectionManager(String logicDbName, DruidDataSourceWrapper connPool) {
		this.logicDbName = logicDbName;
		this.connPool = connPool;
	}
	
	public String getLogicDbName() {
		return logicDbName;
	}

	public DalConnection getNewConnection(DalHints hints, boolean useMaster, DalEventEnum operation)
			throws SQLException {
		DalConnection connHolder = null;
		String realDbName = logicDbName;
		try
		{
			boolean isMaster = hints.is(DalHintEnum.masterOnly) || useMaster;
			boolean isSelect = operation == DalEventEnum.QUERY;
			
			// The internal test path
			if(config == null) {
				Connection conn = null;
				conn = connPool.getConnection(logicDbName, isMaster, isSelect);
				connHolder = new DalConnection(conn, DbMeta.getDbMeta(conn.getCatalog(), conn));
			}else {
				connHolder = getConnectionFromDSLocator(hints, isMaster, isSelect);
			}
			
			connHolder.setAutoCommit(true);
			connHolder.applyHints(hints);

			realDbName = connHolder.getCatalog();
			Logger.logGetConnectionSuccess(realDbName);
		}
		catch(SQLException ex)
		{
			Logger.logGetConnectionFailed(realDbName, ex);
			throw ex;
		}
		return connHolder;
	}

	private DalConnection getConnectionFromDSLocator(DalHints hints,
			boolean isMaster, boolean isSelect) throws SQLException {
		Connection conn;
		String allInOneKey;
		DatabaseSet dbSet = config.getDatabaseSet(logicDbName);
		if(dbSet.isShardingSupported()){
			DalShardStrategy strategy = dbSet.getStrategy();

			// In case the sharding strategy indicate that master shall be used
			isMaster |= strategy.isMaster(config, logicDbName, hints);
			String shard = strategy.locateShard(config, logicDbName, hints);
			dbSet.validate(shard);
			
			allInOneKey = dbSet.getRandomRealDbName(shard, isMaster, isSelect);
		} else {
			allInOneKey = dbSet.getRandomRealDbName(isMaster, isSelect);
		}
		
		try {
			conn = DataSourceLocator.newInstance().getDataSource(allInOneKey).getConnection();
			DbMeta meta = DbMeta.getDbMeta(allInOneKey, conn);
			return new DalConnection(conn, meta);
		} catch (Throwable e) {
			throw new SQLException("Can not get connection from DB " + allInOneKey, e);
		}
	}
	
	public <T> T doInConnection(ConnectionAction<T> action, DalHints hints)
			throws SQLException {
		action.initLogEntry(logicDbName, hints);
		action.start();
		
		Throwable ex = null;
		T result = null;
		
		try {
			result = action.execute();
		} catch (Throwable e) {
			ex = e;
		} finally {
			action.populateDbMeta();
			action.cleanup();
		}
		
		action.end(result, ex);

		return result;
	}
	
	public static void cleanup(ResultSet rs, Statement statement, DalConnection connHolder) {
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		closeConnection(connHolder);
	}

	private static void closeConnection(DalConnection connHolder) {
		//do nothing for connection in transaction
		if(DalTransactionManager.isInTransaction())
			return;
		
		// For list of nested commands, the top level action will not hold any connHolder
		if(connHolder == null)
			return;
		
		connHolder.closeConnection();
	}
}
