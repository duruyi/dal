package com.ctrip.platform.dal.daogen.pojo;

public class FieldMeta {
	
	private String name;
	
	//指Java或者CSharp的数据类型
	private String type;
	
	//指对应的数据库类型，如varchar
	private String dbType;
	
	private int sqlType;
	
	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	private int position;
	
	private boolean nullable;
	
	private boolean primary;
	
	private boolean identity;
	
	public boolean isIdentity() {
		return identity;
	}

	public void setIdentity(boolean identity) {
		this.identity = identity;
	}

	private boolean valueType;
	
	private boolean indexed;

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public boolean isValueType() {
		return valueType;
	}

	public void setValueType(boolean valueType) {
		this.valueType = valueType;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}
	
	

}
