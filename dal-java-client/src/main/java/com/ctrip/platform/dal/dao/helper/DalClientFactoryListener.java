package com.ctrip.platform.dal.dao.helper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.platform.dal.dao.DalClientFactory;
import com.ctrip.platform.dal.sql.logging.DalLogger;

public class DalClientFactoryListener implements ServletContextListener {
	private Logger logger = LoggerFactory.getLogger(DalClientFactoryListener.class);
	
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("Dal Factory Listener is about to start");
		ServletContext context = sce.getServletContext();
		
		String DalConfigPath = context.getInitParameter("com.ctrip.platform.dal.dao.DalConfigPath");
		String warmUp = context.getInitParameter("com.ctrip.platform.dal.dao.DalWarmUp");
		String diableLogging = context.getInitParameter("com.ctrip.platform.dal.dao.EnableLogging");
		String simplifyLogging = context.getInitParameter("com.ctrip.platform.dal.dao.SimplifyLogging");

		try {
			if(DalConfigPath == null || DalConfigPath.trim().length() == 0)
				DalClientFactory.initClientFactory();
			else
				DalClientFactory.initClientFactory(DalConfigPath.trim());
			
			if(Boolean.parseBoolean(warmUp))
				DalClientFactory.warmUpConnections();
			
			DalLogger.setDisableLogging(Boolean.parseBoolean(diableLogging));
			DalLogger.setSimplifyLogging(Boolean.parseBoolean(simplifyLogging));
			
		} catch (Exception e) {
			logger.error("Error when init client factory", e);
		}
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		DalClientFactory.shutdownFactory();
	}
}
