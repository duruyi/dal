package com.ctrip.datasource;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;

public class LocalDataSourceProvider<K extends CharSequence,V extends DataSource> extends ConcurrentHashMap<K,V>{

	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5752249323568785554L;

	private static final Log log = LogFactory.getLog(LocalDataSourceProvider.class);
	
	//private final ConcurrentHashMap<String,DataSource> dataSourcePool = new ConcurrentHashMap<String,DataSource>();
	
	final Map<String,String[]> props = AllInOneConfigParser.newInstance().getDBAllInOneConfig();
	
	
	@SuppressWarnings("unchecked")
	public Set<K> keySet(){
		return (Set<K>) props.keySet();
	}
		
	/**
	 * override
	 * @param data source name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public V get(Object name){
		
		V ds = super.get(name); 
		
		if(ds==null){
			try {
				ds = (V)createDataSource(name);
				V d = this.putIfAbsent((K)name, ds);
				if(d!=null){
					ds=d;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.error("Creating DataSource "+name+" error:"+e.getMessage(), e);
			}
		}
		
		return ds;
		
	}
	
	private DataSource createDataSource(Object name) throws SQLException{
		
		String[] prop = props.get(name);
		
		PoolProperties p = new PoolProperties();
		
		p.setUrl(prop[0]);
        p.setUsername(prop[1]);
        p.setPassword(prop[2]);
        p.setDriverClassName(prop[3]);
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(100);
        p.setInitialSize(10);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(p);
		
        ds.createPool();
        
        log.info("Datasource "+name+" created, Driver:"+prop[3]);
		
		/*DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName(prop[3]);
        ds.setUrl(prop[0]);
        ds.setUsername(prop[1]);
        ds.setPassword(prop[2]);

        ds.setInitialSize(1);
        ds.setMinIdle(1);
        ds.setMaxActive(20);
        ds.setMaxWait(60000);

        ds.setTimeBetweenEvictionRunsMillis(60000);
        ds.setMinEvictableIdleTimeMillis(60000);
        ds.setValidationQuery("SELECT 'x'");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);
        ds.init();
        
        log.info("druid datasource inited");*/
		
		return ds;

	}
	

}
