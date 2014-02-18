package com.ctrip.platform.dal.daogen.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlTask  extends AbstractTask{

	private int id;
	
	private int project_id;
	
	private String db_name;
	
	private String class_name;
	
	private String method_name;
	
	private String crud_type;
	
	private String sql_content;
	
	private String parameters;

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProject_id() {
		return project_id;
	}

	public void setProject_id(int project_id) {
		this.project_id = project_id;
	}

	public String getDb_name() {
		return db_name;
	}

	public void setDb_name(String db_name) {
		this.db_name = db_name;
	}

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public String getMethod_name() {
		return method_name;
	}

	public void setMethod_name(String method_name) {
		this.method_name = method_name;
	}

	public String getCrud_type() {
		return crud_type;
	}

	public void setCrud_type(String crud_type) {
		this.crud_type = crud_type;
	}

	public String getSql_content() {
		return sql_content;
	}

	public void setSql_content(String sql_content) {
		this.sql_content = sql_content;
	}

	public static SqlTask visitRow(ResultSet rs) throws SQLException {
		SqlTask task = new SqlTask();
		task.setId(rs.getInt(1));
		task.setProject_id(rs.getInt(2));
		task.setServer_id(rs.getInt(3));
		task.setDb_name(rs.getString(4));
		task.setClass_name(rs.getString(5));
		task.setMethod_name(rs.getString(6));
		task.setCrud_type(rs.getString(7));
		task.setSql_content(rs.getString(8));
		return task;
	}
	
}
