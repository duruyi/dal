package com.ctrip.sysdev.das.console.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.zookeeper.ZooKeeper;

import com.ctrip.sysdev.das.console.domain.Port;
import com.ctrip.sysdev.das.console.domain.Worker;

@Resource
@Path("instance/worker")
@Singleton
public class WorkerResource {
	@Context
	private ServletContext sContext;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Worker> getWorkerInstance() {
		List<Worker> workerList = new ArrayList<Worker>();
		ZooKeeper zk = (ZooKeeper)sContext.getAttribute("com.ctrip.sysdev.das.console.zk");
		try {
			List<String> workerNameList = zk.getChildren("/dal/das/instance/worker", false);
			for(String workerName: workerNameList) {
				String workerPath = "/dal/das/instance/worker" + "/" + workerName;
				Worker worker = new Worker();
				worker.setIp(workerName);
				Port port = new Port();
				Set<Integer> ports = new HashSet<Integer>();
				port.setPorts(ports);
				List<String> portNumberList = zk.getChildren(workerPath, false);
				for(String number: portNumberList)
					ports.add(new Integer(number));
				workerList.add(worker);
			}				
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workerList;
	}

}
