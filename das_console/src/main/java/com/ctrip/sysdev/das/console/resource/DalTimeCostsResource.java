package com.ctrip.sysdev.das.console.resource;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.ctrip.sysdev.das.common.Status;
import com.ctrip.sysdev.das.console.domain.StringIdSet;
import com.ctrip.sysdev.das.console.domain.TimeCost;

@Resource
@Path("monitor/timeCosts")
@Singleton
public class DalTimeCostsResource extends DalBaseResource {
	@Context
	private ServletContext sContext;
	private ConcurrentHashMap<String, TimeCost> store = new ConcurrentHashMap<String, TimeCost>();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StringIdSet getTimeCosts() {
		StringIdSet ids = new StringIdSet();
		ids.setIds(store.keySet());
		return ids;
	}
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public TimeCost getTimeCost(@PathParam("id") String id) {
		TimeCost tc = store.get(id);
		return tc;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Status addTimeCost(@FormParam("id") String id, @FormParam("timeCost") String timeCost) {
		// TimeCost looks like: name0:value0;name1:value1
		TimeCost tc = new TimeCost(id, timeCost);
		TimeCost oldTc = store.putIfAbsent(id, tc);
		if(oldTc != null)
			oldTc.merge(tc);
		return Status.OK;
	}
}
