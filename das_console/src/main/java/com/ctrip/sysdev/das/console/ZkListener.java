package com.ctrip.sysdev.das.console;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.zookeeper.ZooKeeper;

public class ZkListener implements ServletContextListener, DasConsoleConstants {
	
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		String hostPorts = context.getInitParameter("ZkHostPorts");
		try {
			ZooKeeper zk = new ZooKeeper(hostPorts, 30 * 1000, null);
			context.setAttribute(ZK, zk);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		ZooKeeper zk = (ZooKeeper)context.getAttribute(ZK);
		if(zk != null)
			try {
				zk.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		sce.getServletContext().removeAttribute(ZK);
	}

}
