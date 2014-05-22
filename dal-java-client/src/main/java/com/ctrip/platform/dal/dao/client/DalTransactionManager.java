package com.ctrip.platform.dal.dao.client;

import java.sql.SQLException;

import com.ctrip.platform.dal.dao.DalHints;
import com.ctrip.platform.dal.dao.logging.DalEventEnum;

public class DalTransactionManager {
	private DalConnectionManager connManager;

	private static final ThreadLocal<DalTransaction> connectionCacheHolder = new ThreadLocal<DalTransaction>();

	public DalTransactionManager(DalConnectionManager connManager) {
		this.connManager = connManager;
	}
	
	public int startTransaction(DalHints hints, DalEventEnum operation) throws SQLException {
		DalTransaction connCache = connectionCacheHolder.get();

		if(connCache == null) {
			connCache = new DalTransaction( 
					getConnection(hints, true, operation), 
					connManager.getLogicDbName());
			
			connectionCacheHolder.set(connCache);
		}
		return connCache.startTransaction();
	}

	public void endTransaction(int startLevel) throws SQLException {
		DalTransaction connCache = connectionCacheHolder.get();
		
		if(connCache == null)
			throw new SQLException("calling endTransaction with empty ConnectionCache");

		connCache.endTransaction(startLevel);
	}

	public static boolean isInTransaction() {
		return connectionCacheHolder.get() != null;
	}
	
	public void rollbackTransaction(int startLevel) throws SQLException {
		DalTransaction connCache = connectionCacheHolder.get();
		
		// Already handled in deeper level
		if(connCache == null)
			return;

		connCache.rollbackTransaction(startLevel);
	}
	
	public DalConnection getConnection(DalHints hints, DalEventEnum operation) throws SQLException {
		return getConnection(hints, false, operation);
	}
	
	public DbMeta getCurrentDbMeta() throws SQLException {
		return connectionCacheHolder.get().getConnection().getMeta();
	}
	
	private DalConnection getConnection(DalHints hints, boolean useMaster, DalEventEnum operation) throws SQLException {
		DalTransaction connCache = connectionCacheHolder.get();
		
		if(connCache == null) {
			return connManager.getNewConnection(hints, useMaster, operation);
		} else {
			connCache.validate(connManager.getLogicDbName());
			return connCache.getConnection();
		}
	}
	
	public static void clearCache() {
		connectionCacheHolder.set(null);
	}
	
	public <T> T doInTransaction(ConnectionAction<T> action, DalHints hints)
			throws SQLException {
		action.start();
		
		Throwable ex = null;
		T result = null;
		
		int level = 0;
		try {
			level = startTransaction(hints, action.operation);
			
			action.initLogEntry(connManager.getLogicDbName(), hints);
			action.populateDbMeta();
			
			result = action.execute();	
			
			action.cleanup();
			endTransaction(level);
			return result;
		} catch (Throwable e) {
			ex = e;
			action.cleanup();
			rollbackTransaction(level);
		}
		
		action.end(result, ex);
		
		return result;
	}
}
