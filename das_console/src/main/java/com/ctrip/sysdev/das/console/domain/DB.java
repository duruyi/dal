package com.ctrip.sysdev.das.console.domain;

public class DB {
	private String name;
	private DbSetting setting;
	private DB[] slave;

	public DB[] getSlave() {
		return slave;
	}

	public void setSlave(DB[] slave) {
		this.slave = slave;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DbSetting getSetting() {
		return setting;
	}

	public void setSetting(DbSetting setting) {
		this.setting = setting;
	}
}
