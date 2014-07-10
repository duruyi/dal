package com.ctrip.platform.dal.sql.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ctrip.freeway.gen.v2.LogLevel;
import com.ctrip.freeway.gen.v2.LogType;
import com.ctrip.freeway.logging.ILog;
import com.ctrip.freeway.logging.LogManager;
import com.ctrip.freeway.tracing.ITrace;
import com.ctrip.freeway.tracing.TraceManager;


public class DalLogger {
	public static final String TITLE = "Dal Fx";
	public static AtomicBoolean disableLogging = new AtomicBoolean(false);
	public static AtomicBoolean simplifyLogging = new AtomicBoolean(false);
	
	public static ThreadLocal<DalWatcher> watcher = new ThreadLocal<DalWatcher>();
	
	private static ILog logger = LogManager.getLogger("DAL Java Client");
	private static ITrace trace = TraceManager.getTracer("DAL Java Client");

	public static boolean isDisableLogging() {
		return disableLogging.get();
	}
	
	public static void setDisableLogging(boolean value) {
		disableLogging.set(value);
	}
	
	public static boolean isSimplifyLogging() {
		return simplifyLogging.get();
	}
	
	public static void setSimplifyLogging(boolean simplify) {
		simplifyLogging.set(simplify);
	}
	
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
		if(isDisableLogging())
			return;
		
		if(isSimplifyLogging()) {
			logger.info(TITLE, DalWatcher.toJson(), entry.getTag());
		} else {
			//logger.info(TITLE, entry.toJson(), entry.getTag());
			trace.log(LogType.SQL, LogLevel.INFO, TITLE, entry.toJson(), entry.getTag());
		}
	}
	
	public static void logGetConnectionFailed(String realDbName, Throwable e)
	{
		try {
			String msg = getExceptionStack(e);
			
			String logMsg = "Connectiing to " + realDbName + " database failed." +
					System.lineSeparator() + System.lineSeparator()  +
					"********** Exception Info **********" + System.lineSeparator()  + msg;
			log("Get connection", DalEventEnum.CONNECTION_FAILED, LogLevel.ERROR, logMsg);
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
	
	public static String getExceptionStack(Throwable e)
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

	public static void watcherBegin(){
		DalWatcher wac = watcher.get();
		if(null == wac){
			wac = new DalWatcher();
			wac.begin();
			watcher.set(wac);		
		}		
	}
	
	public static void watcherBeginConnect(){
		if(watcher.get() != null)
			watcher.get().beginConnect();
	}
	
	public static void watcherEndConnect(){
		if(watcher.get() != null)
			watcher.get().endConnect();
	}
	
	public static void watcherBeginExecute(){
		if(watcher.get() != null)
			watcher.get().beginExecute();
	}
	
	public static void watcherEndExecute(){
		if(watcher.get() != null)
			watcher.get().endExectue();
	}
	
	public static void watcherEnd(){
		if(watcher.get() != null)
			watcher.get().end();
	}
	
	public static DalWatcher getAndRemoveWatcher(){
		DalWatcher wac = watcher.get();
		watcher.remove();
		return wac;  
	}
}
