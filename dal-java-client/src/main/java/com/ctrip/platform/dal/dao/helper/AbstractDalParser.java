package com.ctrip.platform.dal.dao.helper;

import com.ctrip.platform.dal.dao.DalParser;

public abstract class AbstractDalParser<T> implements DalParser<T> {
	protected String dataBaseName;
	protected String tableName;
	protected String[] columns;
	protected String[] primaryKeyColumns;
	protected int[] columnTypes;
	
	public AbstractDalParser(){}
	
	public AbstractDalParser(
			String dataBaseName,
			String tableName,
			String[] columns,
			String[] primaryKeyColumns,
			int[] columnTypes) {
		this.dataBaseName = dataBaseName;
		this.tableName = tableName;
		this.columns = columns;
		this.primaryKeyColumns = primaryKeyColumns;
		this.columnTypes = columnTypes;
	}
	
	@Override
	public String getDatabaseName() {
		return dataBaseName;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public String[] getColumnNames() {
		return columns;
	}

	@Override
	public String[] getPrimaryKeyNames() {
		return primaryKeyColumns;
	}
	
	@Override
	public int[] getColumnTypes() {
		return columnTypes;
	}
}
