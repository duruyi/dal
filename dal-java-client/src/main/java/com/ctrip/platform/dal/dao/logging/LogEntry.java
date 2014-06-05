package com.ctrip.platform.dal.dao.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.ctrip.platform.dal.dao.DalHintEnum;
import com.ctrip.platform.dal.dao.DalHints;
import com.ctrip.platform.dal.dao.DalQueryDao;
import com.ctrip.platform.dal.dao.DalTableDao;
import com.ctrip.platform.dal.dao.StatementParameter;
import com.ctrip.platform.dal.dao.StatementParameters;
import com.ctrip.platform.dal.dao.client.DalDirectClient;
import com.ctrip.security.encryption.AESCrypto;

public class LogEntry {
	public static final String TAG_IN_TRANSACTION = "InTransaction";
	public static final String TAG_DURATION_TIME = "DurationTime";
	public static final String TAG_DATABASE_NAME = "DatabaseName";
	public static final String TAG_SERVER_ADDRESS = "ServerAddress";
	public static final String TAG_COMMAND_TYPE = "CommandType";
	public static final String TAG_USER_NAME = "UserName";
	public static final String TAG_RECORD_COUNT = "RecordCount";
	public static final int LOG_LIMIT = 32*1024;
	
	private static final String SQLHIDDENString = "*";
	private static final String JSON_PATTERN = "{'HasSql':'%s','Hash':'%s','SqlTpl':'%s','Param':'%s','IsSuccess':'%s','ErrorMsg':'%s'}";
	private static ConcurrentHashMap<String, Integer> hashes = null;
	// DateTime
	private Date timeStamp;
	private String machine;
	private String sqlHash;
	private boolean sensitive;
	private String sql;
	private String callString;
	private String[] sqls;
	private StatementParameters parameters;
	private StatementParameters[] parametersList;
	private boolean success;
	private String errorMsg = "";
	private String inputParamStr;
	private String outputParamStr = "";
	private String dao;
	private String method;
	private String source;
	private DalEventEnum event;
	private String message;
	private String level;
	private String title = "Dal Fx";
	private long duration;
	private String userName = "N/A";
	private String serverAddress = "N/A";
	private String databaseName;
	private boolean transactional;
	private int resultCount;
	private String commandType;
	private static Set<String> execludedClasses;

	static {
		execludedClasses = new HashSet<String>();
		hashes = new ConcurrentHashMap<String, Integer>();
		execludedClasses.add(DalDirectClient.class.getName());
		execludedClasses.add(DalTableDao.class.getName());
		execludedClasses.add(DalQueryDao.class.getName());
	}

	public LogEntry(DalHints hints) {
		this.timeStamp = new Date();
		this.machine = CommonUtil.MACHINE;
		this.sensitive = hints.is(DalHintEnum.sensitive);
		this.getSourceAndMessage();
	}

	public void setCallString(String callString) {
		this.callString = callString;
	}

	public void setSqls(String[] sqls) {
		this.sqls = sqls;
	}

	public void setParameters(StatementParameters parameters) {
		this.parameters = parameters;
	}
	
	public void setParametersList(StatementParameters[] parametersList) {
		this.parametersList = parametersList;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setEvent(DalEventEnum event) {
		this.event = event;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setTransactional(boolean transactional) {
		this.transactional = transactional;
	}

	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle(){
		return this.title;
	}
	
	public String getDao(){
		return this.dao;
	}
	
	public String getMethod(){
		return this.method;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public void setErrorMsg(Throwable e) {
		try {  
            StringWriter sw = new StringWriter();  
            PrintWriter pw = new PrintWriter(sw);  
            e.printStackTrace(pw);  
            this.errorMsg = "\r\n" + sw.toString() + "\r\n";  
        } catch (Exception e2) {  
        	this.errorMsg = "bad getErrorInfoFromException";  
        }
	}

	public String getInputParamStr(){
		if(null != this.parameters) {
			this.inputParamStr = this.getInputParameterPrint(this.parameters);
		}
		else if(null != this.parametersList && this.parametersList.length > 0){
			this.inputParamStr = this.getInputParameterPrint(this.parametersList);
		}
		return this.inputParamStr;
	}
	
	private void getSourceAndMessage() {
		StackTraceElement[] callers = Thread.currentThread().getStackTrace();

		for (int i = 4; i < callers.length; i++) {
			StackTraceElement caller = callers[i];
			if (execludedClasses.contains(caller.getClassName()))
				continue;

			dao = caller.getClassName();
			method = caller.getMethodName();
			source = caller.toString();
			break;
		}
	}
	
	private String getInputParameterPrint(StatementParameters parameters) {
		if(null == parameters)
			return "";
		StringBuilder sbin = new StringBuilder();
		for (StatementParameter param : parameters.values()) {
			if (param.isInputParameter()) {
				// /sbin.append(String.Format("  %s(%d):%s\r\n", para.Name,
				// para.DbType.ToString(), para.isSensitive() ? SQLHIDDENString
				// : m_InputParameters[para.Name]);
				String paramName = param.getIndex() > 0 ? String.valueOf(param
						.getIndex()) : param.getName();
				sbin.append(String.format("  %s(%d):%s\r\n", paramName, param
						.getSqlType(), param.isSensitive() ? SQLHIDDENString
						: param.getValue()));
			}
		}

		return sbin.toString();
	}

	private String getInputParameterPrint(StatementParameters[] parametersList){
		StringBuilder sbin = new StringBuilder();
		for(StatementParameters param : parametersList){
			sbin.append(this.getInputParameterPrint(param));
		}
		return sbin.toString();
	}
	
	public String getSqlTpl(){
		if(this.event == DalEventEnum.QUERY || 
				this.event == DalEventEnum.UPDATE_SIMPLE ||
				this.event == DalEventEnum.UPDATE_KH ||
				this.event == DalEventEnum.BATCH_UPDATE_PARAM){
			return this.sql;
		}
		if(this.event == DalEventEnum.BATCH_UPDATE){
			return StringUtils.join(this.sqls, ";");
		}
		if(this.event == DalEventEnum.CALL || 
				this.event == DalEventEnum.BATCH_CALL){
			return this.callString;
		}
		
		return "";
	}
	
	public void setCommandType(){
		if(this.event == DalEventEnum.CALL || 
				this.event == DalEventEnum.BATCH_CALL)
			this.commandType = "StoreProcedure";
		else
			this.commandType = "Text";
	}
	
	public String getCommandType(){
		return this.commandType;
	}
	
	private String getParams(StatementParameters params){
		List<String> plantPrams = new ArrayList<String>();
		for (StatementParameter param : params.values()) {
			plantPrams.add(String.format("%s=%s", 
					param.getName() == null ? param.getIndex() : param.getName(), 
					param.isSensitive() ? SQLHIDDENString : param.getValue()));
		}
		return StringUtils.join(plantPrams, ",");
	}
	
	private String getParams(){
		StringBuilder sbout = new StringBuilder();
		if(this.event == DalEventEnum.QUERY || 
				this.event == DalEventEnum.UPDATE_SIMPLE ||
				this.event == DalEventEnum.UPDATE_KH ||
				this.event == DalEventEnum.CALL){
			return this.getParams(this.parameters);
		}
		if(this.event == DalEventEnum.BATCH_UPDATE_PARAM ||
				this.event == DalEventEnum.BATCH_CALL){
			for(StatementParameters param : this.parametersList){
				sbout.append(this.getParams(param) + ";");
			}
			return sbout.substring(0, sbout.length() - 1);
		}
		return "";
	}
	
	private boolean hasHashCode(String sqlTpl, int hashCode){
		if(!hashes.containsKey(sqlTpl)){
			hashes.put(sqlTpl, hashCode);
			return false;
		}else{
			return true;
		}
	}

	public int getSqlSize(){
		int size = 0;
		if(this.event == DalEventEnum.QUERY || 
				this.event == DalEventEnum.UPDATE_SIMPLE ||
				this.event == DalEventEnum.UPDATE_KH ||
				this.event == DalEventEnum.BATCH_UPDATE_PARAM){
			size = this.sql.length();
		}
		if(this.event == DalEventEnum.BATCH_UPDATE){
			for(String sqll : this.sqls){
				size += sqll.length();
			}
		}
		if(this.event == DalEventEnum.CALL || 
				this.event == DalEventEnum.BATCH_CALL){
			size = this.callString.length();
		}
		
		return size;
	}
	
	public Map<String, String> getTag() {
		Map<String, String> tag = new LinkedHashMap<String, String>();
		tag.put(TAG_IN_TRANSACTION, this.transactional ? "True" : "False");
		tag.put(TAG_DURATION_TIME, Long.toString(this.duration) + "ms");
		tag.put(TAG_DATABASE_NAME, CommonUtil.null2NA(this.databaseName));
		tag.put(TAG_SERVER_ADDRESS, CommonUtil.null2NA(this.getServerAddress()));
		tag.put(TAG_COMMAND_TYPE, CommonUtil.null2NA(this.getCommandType()));
		tag.put(TAG_USER_NAME, CommonUtil.null2NA(this.userName));
		tag.put(TAG_RECORD_COUNT, Long.toString(this.resultCount));

		return tag;
	}
	
	/**
	 * To be called after execute Sp
	 * 
	 * @param parameters
	 */
	public void setOutputParameters(StatementParameters parameters) {
		StringBuilder sbout = new StringBuilder();
		for (StatementParameter param : parameters.values()) {
			if (param.isOutParameter())
				sbout.append(String.format(
						"  %s(%d):%s\r\n",
						param.getName(),
						param.getSqlType(),
						param.isSensitive() ? SQLHIDDENString : param
								.getValue()));
		}

		outputParamStr = sbout.toString();

		if (outputParamStr.length() > 0 && Logger.encryptOut) {
			outputParamStr = CommonUtil.desEncrypt(outputParamStr);
		}
	}

	public String ToString() {
		StringBuilder sb = new StringBuilder();
		sb.append('\n');
		sb.append(String.format("Log Name:%s\r\n", this.databaseName));
		sb.append(String.format("Log Source:%s\r\n", source));
		sb.append(String.format("Level:%s\r\n", level));
		sb.append(String.format("DateTime:%s\r\n", timeStamp.toString()));
		sb.append(String.format("Event:%d\r\n", event.getEventId()));
		sb.append(String.format("Machine:%s\r\n", machine));
		sb.append(String.format("Message:%s\r\n", message));
		if (sql != null) {
			sb.append(String.format("Duration:%d\r\n", duration))
				.append(String.format("SQL Text:%s\r\n", this.sensitive ? SQLHIDDENString : sql))
				.append(String.format("SQL Hash:%s\r\n", sqlHash))
				.append("Input Parameters:")
				.append(CommonUtil.desEncrypt(this.getInputParamStr())).append("\r\n")
				.append("Output Parameters:").append(outputParamStr)
				.append("\r\n");
		}
		sb.append('\n');
		return sb.toString();
	}

	/**
	 * 获取LogEntry字符串概要表示，用于Central Logging等本身已生成date, appid, machine,
	 * level等字段的日志工具
	 */
	public String toBrief() {
		StringBuilder sb = new StringBuilder();
		if (sql != null) {
			sb.append(String.format("Event:%d\r\n", event.getEventId()))
				.append(String.format("Message:%s\r\n", message))
				.append(String.format("SQL Text: %s\r\n", CommonUtil.tagSql(sql)))
				.append(String.format("%s\r\n", this.sensitive ? SQLHIDDENString : sql))
				.append("Input Parameters:").append(CommonUtil.desEncrypt(this.getInputParamStr())).append("\r\n")
				.append("Output Parameters:").append(outputParamStr)
				.append("\r\n");
		} else {
			sb.append(String.format("Log Name:%s\r\n", this.databaseName))
				.append(String.format("Log Source:%s\r\n", source))
				.append(String.format("Event:%d\r\n", event.getEventId()))
				.append(String.format("Message:%s\r\n", message))
				.append(String.format("SQL Text: %s\r\n", CommonUtil.tagSql("")));
		}
		sb.append('\n');
		return sb.toString();
	}

	public String toJson(){
		String sqlTpl = this.sensitive ?  SQLHIDDENString : this.getSqlTpl();
		String params = "";
		if(this.sensitive){
			try {
				params = AESCrypto.getInstance().crypt(this.getParams());
			} catch (Exception e) {
				this.errorMsg = e.getMessage();
			}
		} else {
			params = this.getParams();
		}
		int tplLength = sqlTpl.length();
		int paramsLength = params.length();
		if(tplLength + paramsLength > LOG_LIMIT){
			sqlTpl = sqlTpl.substring(0, tplLength > LOG_LIMIT ? LOG_LIMIT : tplLength);
			params = "over long with param, can not be recorded";
		}
		int hashCode = CommonUtil.GetHashCode(sqlTpl);
		boolean existed = this.hasHashCode(sqlTpl, hashCode);
		return String.format(JSON_PATTERN, 
				existed ? 1 : 0, 
				hashCode, 
				existed ? "" : sqlTpl, 
				params, 
				this.success ? 1 : 0, 
				this.errorMsg);
	}
}