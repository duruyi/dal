package com.ctrip.platform.dal.dao.configbeans;

import java.util.HashSet;
import java.util.Set;

import com.ctrip.platform.appinternals.annotations.BeanMeta;
import com.ctrip.platform.appinternals.configuration.ChangeEvent;
import com.ctrip.platform.appinternals.configuration.ConfigBeanBase;

@BeanMeta(alias="TimeoutMarkDownBean")
public class TimeoutMarkDownBean extends ConfigBeanBase{
	@BeanMeta(alias = "EnableTimeoutMarkDown")
	private volatile boolean enableTimeoutMarkDown = true;
	@BeanMeta(alias = "SamplingDuration")
    private volatile int samplingDuration = 60;
	@BeanMeta(alias = "TimeoutThreshold")
	private volatile int minTimeOut = 1;
	@BeanMeta(alias = "ErrorCountThreshold")
    private volatile int errorCountBaseLine = 1000;
	@BeanMeta(alias = "ErrorPercentThreshold")
    private volatile float errorPercent = 0.5f;
	@BeanMeta(alias = "ErrorPercentReferCount")
	private volatile int errorPercentBaseLine = 2000;
	
	@BeanMeta(alias = "MySqlErrorCodes")
    private String mySqlErrorCodes = "0";
	@BeanMeta(alias = "SqlServerErrorCodes")
    private String sqlServerErrorCodes = "-2";
    
    private Set<Integer> mysqlTimeoutMarkdownCodes = new HashSet<Integer>();
    private Set<Integer> sqlServerTimeoutMarkdownCodes = new HashSet<Integer>();
    
    public TimeoutMarkDownBean(){
    	mysqlTimeoutMarkdownCodes = this.parseErrorCodes(this.mySqlErrorCodes);
    	sqlServerTimeoutMarkdownCodes = this.parseErrorCodes(this.sqlServerErrorCodes);
    	this.addChangeEvent("mySqlErrorCodes", new ChangeEvent() {
    		
			@Override
			public void end(Object oldVal, String newVal) throws Exception {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void before(Object oldVal, String newVal) throws Exception {
				mysqlTimeoutMarkdownCodes = parseErrorCodes(newVal);
			}
		});
    	
    	this.addChangeEvent("SqlServerErrorCodes", new ChangeEvent() {		
			@Override
			public void end(Object oldVal, String newVal) throws Exception {
				// TODO Auto-generated method stub			
			}
			
			@Override
			public void before(Object oldVal, String newVal) throws Exception {
				sqlServerTimeoutMarkdownCodes = parseErrorCodes(newVal);
			}
		});
    }
    
	public boolean isEnableTimeoutMarkDown() {
		return enableTimeoutMarkDown;
	}
	public void setEnableTimeoutMarkDown(boolean enableTimeoutMarkDown) {
		this.enableTimeoutMarkDown = enableTimeoutMarkDown;
	}
	public int getSamplingDuration() {
		return samplingDuration;
	}
	public void setSamplingDuration(int samplingDuration) {
		this.samplingDuration = samplingDuration;
	}
	public int getErrorCountBaseLine() {
		return errorCountBaseLine;
	}
	public void setErrorCountBaseLine(int errorCountBaseLine) {
		this.errorCountBaseLine = errorCountBaseLine;
	}
	public float getErrorPercent() {
		return errorPercent;
	}
	public void setErrorPercent(float errorPercent) {
		this.errorPercent = errorPercent;
	}
	public String getMySqlErrorCodes() {
		return mySqlErrorCodes;
	}
	public void setMySqlErrorCodes(String mySqlErrorCodes) {
		this.mySqlErrorCodes = mySqlErrorCodes;
	}
	public int getErrorPercentBaseLine() {
		return errorPercentBaseLine;
	}
	public void setErrorPercentBaseLine(int errorPercentBaseLine) {
		this.errorPercentBaseLine = errorPercentBaseLine;
	}
	public String getSqlServerErrorCodes() {
		return sqlServerErrorCodes;
	}
	public void setSqlServerErrorCodes(String sqlServerErrorCodes) {
		this.sqlServerErrorCodes = sqlServerErrorCodes;
	}
	public Set<Integer> getMysqlTimeoutMarkdownCodes() {
		return mysqlTimeoutMarkdownCodes;
	}
	public Set<Integer> getSqlServerTimeoutMarkdownCodes() {
		return sqlServerTimeoutMarkdownCodes;
	}

	public int getMinTimeOut() {
		return minTimeOut;
	}

	public void setMinTimeOut(int minTimeOut) {
		this.minTimeOut = minTimeOut;
	}

	private Set<Integer> parseErrorCodes(String codes){
		Set<Integer> temp = new HashSet<Integer>();
		if(codes == null || codes.isEmpty())
			return temp;
		String[] tokens = codes.split(",");
		for (String token : tokens) {
			temp.add(Integer.valueOf(token));
		}
		return temp;
	}
}
