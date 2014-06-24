package com.ctrip.platform.dal.sql.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.ctrip.freeway.gen.v2.LogLevel;
import com.ctrip.freeway.gen.v2.LogType;
import com.ctrip.freeway.logging.ILog;
import com.ctrip.freeway.logging.LogManager;
import com.ctrip.freeway.tracing.ITrace;
import com.ctrip.freeway.tracing.TraceManager;


public class Logger {
	public static final String TITLE = "Dal Fx";
	
	private static ILog logger = LogManager.getLogger("DAL Java Client");
	private static ITrace trace = TraceManager.getTracer("DAL Java Client");

	public static void success(LogEntry entry, long duration, int count) {
		entry.setDuration(duration);
		entry.setSuccess(true);
		entry.setResultCount(count);
		log(entry);
	}
	
	public static void fail(LogEntry entry, long duration, Throwable e) {
		entry.setDuration(duration);
		entry.setSuccess(false);
		entry.setErrorMsg(getExceptionStack(e));
		log(entry);
	}
	
	public static void log(LogEntry entry) {
		trace.log(LogType.SQL, LogLevel.INFO, TITLE, entry.toJson(), entry.getTag());
	}
	
	public static void logGetConnectionFailed(String realDbName, Throwable e)
	{
		try {
			String msg = getExceptionStack(e);
			
			String logMsg = "Connectiing to " + realDbName + " database failed." +
					System.lineSeparator() + System.lineSeparator()  +
					"********** Exception Info **********" + System.lineSeparator()  + msg;
			Logger.log("Get connection", DalEventEnum.CONNECTION_FAILED, LogLevel.ERROR, logMsg);
		} catch (Throwable e1) {
			e1.printStackTrace();
		}	
	}
	
	public static void error(String desc, Throwable e) {
		try {
			String msg = getExceptionStack(e);
			
			String logMsg = desc + System.lineSeparator() + System.lineSeparator() +
			"********** Exception Info **********" + System.lineSeparator() + msg;
			logger.error(TITLE, logMsg);
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
	}
	
	public static void log(String name, DalEventEnum event, LogLevel level, String msg)
	{
		StringBuffer sbuffer = new StringBuffer();
		sbuffer.append(String.format("Log Name: %s" + System.lineSeparator(), name));
		sbuffer.append(String.format("Event: %s" + System.lineSeparator(), event.getEventId()));
		sbuffer.append(String.format("Message: %s " + System.lineSeparator(), msg));
		
		switch (level) {
			case DEBUG:
				logger.debug(TITLE, sbuffer.toString());
			case INFO: 
				logger.info(TITLE, sbuffer.toString());
				break;
			case ERROR:
				logger.error(TITLE, sbuffer.toString());
				break;
			case FATAL:
				logger.fatal(TITLE, sbuffer.toString());
				break;
		default:
			break;
		}
	}
	
	private static String getExceptionStack(Throwable e)
	{
		String msg = e.getMessage();
		try {  
            StringWriter sw = new StringWriter();  
            PrintWriter pw = new PrintWriter(sw);  
            e.printStackTrace(pw);  
            msg = "\r\n" + sw.toString() + "\r\n";  
        } catch (Throwable e2) {  
        	msg = "bad getErrorInfoFromException";  
        }
		
		return msg;
	}
}
