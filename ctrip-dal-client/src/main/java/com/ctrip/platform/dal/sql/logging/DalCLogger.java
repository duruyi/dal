package com.ctrip.platform.dal.sql.logging;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ctrip.framework.clogging.agent.log.ILog;
import com.ctrip.framework.clogging.agent.log.LogManager;
import com.ctrip.framework.clogging.agent.trace.ITrace;
import com.ctrip.framework.clogging.agent.trace.TraceManager;
import com.ctrip.framework.clogging.domain.thrift.LogLevel;
import com.ctrip.framework.clogging.domain.thrift.LogType;
import com.ctrip.platform.dal.dao.DalEventEnum;
import com.ctrip.platform.dal.dao.Version;
import com.ctrip.platform.dal.dao.client.DalWatcher;
import com.ctrip.platform.dal.dao.helper.LoggerHelper;

public class DalCLogger {
	public static final String TITLE = "Dal Fx";
	//public static final String LOG_NAME = "DAL Java Client " + DalClientVersion.version;
	private static final String CLIENT_VERSION = "dal.client.version";
	public static AtomicBoolean simplifyLogging = new AtomicBoolean(false);
	public static AtomicBoolean encryptLogging = new AtomicBoolean(true);

	public static ThreadLocal<DalWatcher> watcher = new ThreadLocal<DalWatcher>();

	private static ILog logger;
	private static ITrace trace;

	static {
		logger = LogManager.getLogger("DAL Java Client " + Version.getVersion());
		trace = TraceManager.getTracer("DAL Java Client " + Version.getVersion());
	}

	public static boolean isSimplifyLogging() {
		return simplifyLogging.get();
	}

	public static void setSimplifyLogging(boolean simplify) {
		simplifyLogging.set(simplify);
	}

	public static void setEncryptLogging(boolean encrypt) {
		encryptLogging.set(encrypt);
	}
	
	public static boolean isEncryptLogging() {
		return encryptLogging.get();
	}
	
	public static void success(CtripLogEntry entry, int count) {
		entry.setSuccess(true);
		entry.setResultCount(count);
		log(entry);
	}

	public static void fail(CtripLogEntry entry, Throwable e) {
		entry.setSuccess(false);
		entry.setErrorMsg(e.getMessage());
		entry.setException(e);
		log(entry);
	}

	public static void log(CtripLogEntry entry) {
		if (isSimplifyLogging()) {
			if (entry.getException() == null) {
				logger.info(TITLE, entry.toJson(isEncryptLogging(), entry), entry.getTag());
			} else {
				logger.error(TITLE, entry.toJson(isEncryptLogging(), entry), entry.getTag());
			}
		} else {
			if (entry.getException() == null)
				trace.log(LogType.SQL, LogLevel.ERROR, TITLE, entry.toJson(isEncryptLogging(), entry),
						entry.getTag());
			else
				trace.log(LogType.SQL, LogLevel.ERROR, TITLE, entry.toJson(isEncryptLogging(), entry),
						entry.getTag());
		}
	}

	public static void error(String desc, Throwable e) {
		try {
			String msg = LoggerHelper.getExceptionStack(e);

			String logMsg = desc + System.lineSeparator()
					+ System.lineSeparator()
					+ "********** Exception Info **********"
					+ System.lineSeparator() + msg;
			logger.error(TITLE, logMsg);
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
	}

	public static void getConnectionFailed(String realDbName, Throwable e) {
		StringBuffer sbuffer = new StringBuffer();
		sbuffer.append(String.format("Log Name: %s" + System.lineSeparator(), "Get connection"));
		sbuffer.append(String.format("Event: %s" + System.lineSeparator(), 
				DalEventEnum.CONNECTION_FAILED.getEventId()));
		
		String msg= "Connectiing to " + realDbName
				+ " database failed." + System.lineSeparator();

		sbuffer.append(String.format("Message: %s " + System.lineSeparator(), msg));
		
		error(sbuffer.toString(), e);
	}
	
	public static void log(LogLevel level, String pattern, Object... args){
		log(level, String.format(pattern, args));
	}
	
	public static void log(LogLevel level, String msg) {
		switch (level) {
		case DEBUG:
			logger.debug(TITLE, msg);
		case INFO:
			logger.info(TITLE, msg);
			break;
		case ERROR:
			logger.error(TITLE, msg);
			break;
		case FATAL:
			logger.fatal(TITLE, msg);
			break;
		default:
			break;
		}
	}

}
