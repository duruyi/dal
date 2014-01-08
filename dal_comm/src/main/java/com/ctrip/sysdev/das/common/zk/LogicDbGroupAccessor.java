package com.ctrip.sysdev.das.common.zk;

import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.ZooKeeper;

import com.ctrip.sysdev.das.common.to.LogicDbGroup;

public class LogicDbGroupAccessor extends DasZkAccessor {
	public LogicDbGroupAccessor(ZooKeeper zk) {
		super(zk);
	}
	
	public List<String> listName() throws Exception {
		return getChildren(DB_GROUP);
	}
	
	public List<LogicDbGroup> list() throws Exception {
		List<String> names = listName();
		List<LogicDbGroup> dbs = new ArrayList<LogicDbGroup>();
		for(String name: names) {
			LogicDbGroup dbGroup = new LogicDbGroup();
			dbGroup.setName(name);
			dbGroup.setLogicDBs(getGroup(name));
		}
		return dbs;
	}
	
	public String[] getGroup(String name) throws Exception {
		return getStringValue(DB_GROUP_NODE, name).split(VALUE_SEPARATOR);
	}
	
	public void createGroup(String name, String[] logicDBs) throws Exception {
		create(DB_GROUP_NODE, name, combine(logicDBs));
	}

	public void modifyGroup(String name, String[] logicDBs) throws Exception {
		setValue(DB_GROUP_NODE, name, combine(logicDBs));
	}

	public void removeGroup(String name) throws Exception {
		delete(DB_GROUP_NODE, name);
	}
	
	private String combine(String[] logicDBs) {
		StringBuilder sb = new StringBuilder();
		for(String db: logicDBs) {
			sb.append(db).append(VALUE_SEPARATOR);
		}
		
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	@Override
	public void initialize() {
		createPath(DB_GROUP);
		createPath(DB_GROUP_NODE);
	}
}
