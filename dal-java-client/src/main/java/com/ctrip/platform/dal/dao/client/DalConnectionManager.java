package com.ctrip.platform.dal.dao.client;

import java.sql.Connection;
import java.sql.SQLException;

import com.ctrip.datasource.locator.DataSourceLocator;
import com.ctrip.platform.dal.catlog.CatInfo;
import com.ctrip.platform.dal.dao.DalHintEnum;
import com.ctrip.platform.dal.dao.DalHints;
import com.ctrip.platform.dal.dao.configbeans.ConfigBeanFactory;
import com.ctrip.platform.dal.dao.configure.DalConfigure;
import com.ctrip.platform.dal.dao.configure.DatabaseSet;
import com.ctrip.platform.dal.dao.markdown.MarkdownManager;
import com.ctrip.platform.dal.dao.strategy.DalShardingStrategy;
import com.ctrip.platform.dal.exceptions.DalException;
import com.ctrip.platform.dal.exceptions.ErrorCode;
import com.ctrip.platform.dal.sql.logging.DalEventEnum;
import com.ctrip.platform.dal.sql.logging.DalLogger;
import com.ctrip.platform.dal.sql.logging.DalWatcher;
import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.apache.commons.lang.StringUtils;

public class DalConnectionManager {
	private DalConfigure config;
	private String logicDbName;

	public DalConnectionManager(String logicDbName, DalConfigure config) {
		this.logicDbName = logicDbName;
		this.config = config;
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
			
			connHolder = getConnectionFromDSLocator(hints, isMaster, isSelect);

			connHolder.setAutoCommit(true);
			connHolder.applyHints(hints);
			
			if(hints.getHA() != null){
				hints.getHA().setDatabaseCategory(connHolder.getMeta().getDatabaseCategory());
			}

			realDbName = connHolder.getDatabaseName();
		}
		catch(SQLException ex)
		{
			DalLogger.logGetConnectionFailed(realDbName, ex);
			throw ex;
		}
		return connHolder;
	}

	private DalConnection getConnectionFromDSLocator(DalHints hints,
			boolean isMaster, boolean isSelect) throws SQLException {
		Connection conn;
		String allInOneKey;
		DatabaseSet dbSet = config.getDatabaseSet(logicDbName);
		String shardId = null;
		
		if(dbSet.isShardingSupported()){
			DalShardingStrategy strategy = dbSet.getStrategy();

			// In case the sharding strategy indicate that master shall be used
			isMaster |= strategy.isMaster(config, logicDbName, hints);
			shardId = hints.getShardId();
			if(shardId == null)
				shardId = strategy.locateDbShard(config, logicDbName, hints);
			if(shardId == null)
				throw new DalException(ErrorCode.ShardLocated, logicDbName);
			dbSet.validate(shardId);
			
			allInOneKey = dbSet.getRandomRealDbName(hints.getHA(), shardId, isMaster, isSelect);
		} else {
			allInOneKey = dbSet.getRandomRealDbName(hints.getHA(), isMaster, isSelect);
		}
		
		if(allInOneKey == null && hints.getHA().isOver()){
			throw new DalException(ErrorCode.NoMoreConnectionToFailOver);
		}
		
		try {	
			conn = DataSourceLocator.newInstance().getDataSource(allInOneKey).getConnection();
			DbMeta meta = DbMeta.createIfAbsent(allInOneKey, dbSet.getDatabaseCategory(), shardId, isMaster, conn);
			return new DalConnection(conn, meta);
		} catch (Throwable e) {
			throw new DalException(ErrorCode.CantGetConnection, e, allInOneKey);
		}
	}
	
	public <T> T doInConnection(ConnectionAction<T> action, DalHints hints)
			throws SQLException {
		// If HA disabled or not query, we just directly call _doInConnnection

		if(!ConfigBeanFactory.getHAConfigBean().isEnable() 
				|| action.operation != DalEventEnum.QUERY)
			return _doInConnection(action, hints);

		DalHA highAvalible = new DalHA();
		hints.setHA(highAvalible);
		do{
			try {
				return _doInConnection(action, hints);			
			} catch (SQLException e) {
				highAvalible.update(e);	
			}
		}while(highAvalible.needTryAgain());
		
		throw highAvalible.getException();
	}

	private <T> T _doInConnection(ConnectionAction<T> action, DalHints hints)
			throws SQLException {
		if(action == null || action.operation == null){
			return _doInConnectionWithoutCat(action, hints);
		}else{
			return _doInConnectionWithCat(action, hints);
		}
	}
	private <T> T _doInConnectionWithCat(ConnectionAction<T> action, DalHints hints)
			throws SQLException {
		action.initLogEntry(logicDbName, hints);
		action.start();

		Throwable ex = null;
		T result = null;
		String sqlType = CatInfo.getTypeSQLInfo(action.operation);
		Transaction t = Cat.newTransaction(CatConstants.TYPE_SQL, sqlType);
		try {
			result = action.execute();
			if(action.sql != null)
				t.addData(action.sql);
			if(action.sqls != null)
				t.addData(StringUtils.join(action.sqls, ";"));
			Cat.logEvent(CatConstants.TYPE_SQL_METHOD, sqlType, Message.SUCCESS, "");
			Cat.logEvent(CatConstants.TYPE_SQL_DATABASE, action.connHolder.getMeta().getUrl());
			t.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			MarkdownManager.detect(action.connHolder, action.start, e);
			ex = e;
			t.setStatus(e);
			Cat.logError(e);
		} finally {
			DalWatcher.endExectue();
			action.populateDbMeta();
			action.cleanup();
			t.complete();
		}

		action.end(result, ex);
		return result;
	}

	private <T> T _doInConnectionWithoutCat(ConnectionAction<T> action, DalHints hints)
			throws SQLException {
		action.initLogEntry(logicDbName, hints);
		action.start();
		
		Throwable ex = null;
		T result = null;
		try {
			result = action.execute();
		} catch (Throwable e) {
			MarkdownManager.detect(action.connHolder, action.start, e);
			ex = e;
		} finally {
			DalWatcher.endExectue();
			action.populateDbMeta();
			action.cleanup();
		}
		
		action.end(result, ex);
		return result;
	}
}
