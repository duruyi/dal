package com.ctrip.sysdev.das.common.zk;

import com.ctrip.sysdev.das.common.zk.to.LogicDbGroup;

public class LogicDbGroupAccessor extends DasZkAccessor {
	public String[] listGroups() {
		String[] logicDBs = null;
		return logicDBs;
	}
	
	public LogicDbGroup getGroup(String name) {
		return null;
	}
	
	public void createGroup(String name, String[] logicDBs) {
		
	}

	public void modifyGroup(String oldName, String newName, String[] logicDBs) {
		
	}

	public void removeGroup(String name) {
		
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}
}
