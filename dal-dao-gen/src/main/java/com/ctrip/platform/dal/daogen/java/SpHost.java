package com.ctrip.platform.dal.daogen.java;

import java.util.List;

import com.ctrip.platform.dal.daogen.pojo.DatabaseCategory;

public class SpHost {
	private DatabaseCategory databaseCategory;
	private String packageName;
	private String dbName;
	private String pojoClassName;
	private String spName;
	private List<JavaParameterHost> parameters;
	public DatabaseCategory getDatabaseCategory() {
		return databaseCategory;
	}
	public void setDatabaseCategory(DatabaseCategory databaseCategory) {
		this.databaseCategory = databaseCategory;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getPojoClassName() {
		return pojoClassName;
	}
	public void setPojoClassName(String pojoClassName) {
		this.pojoClassName = pojoClassName;
	}
	public String getSpName() {
		return spName;
	}
	public void setSpName(String spName) {
		this.spName = spName;
	}
	public List<JavaParameterHost> getParameters() {
		return parameters;
	}
	public void setParameters(List<JavaParameterHost> parameters) {
		this.parameters = parameters;
	}
	
}
