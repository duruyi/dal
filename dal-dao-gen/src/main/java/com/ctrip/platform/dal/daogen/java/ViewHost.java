package com.ctrip.platform.dal.daogen.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import com.ctrip.platform.dal.daogen.enums.DatabaseCategory;

public class ViewHost {
	private String packageName;
	private String dbName;
	private String pojoClassName;
	private String ViewName;
	private List<JavaParameterHost> fields = new ArrayList<JavaParameterHost>();
	private DatabaseCategory databaseCategory;
	private Set<String> imports = new TreeSet<String>();
	
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
	public String getViewName() {
		return ViewName;
	}
	public void setViewName(String viewName) {
		ViewName = viewName;
	}
	public List<JavaParameterHost> getFields() {
		return fields;
	}
	public void setFields(List<JavaParameterHost> fields) {
		this.fields = fields;
	}
	public Set<String> getImports() {
		return imports;
	}
	public void setImports(Set<String> imports) {
		this.imports = imports;
	}
	
	public DatabaseCategory getDatabaseCategory() {
		return databaseCategory;
	}
	public void setDatabaseCategory(DatabaseCategory databaseCategory) {
		this.databaseCategory = databaseCategory;
	}
	
	/**
	 * Get the CTE order by columns to generate row-number
	 * @return
	 */
	public String getOverColumns()
	{
		List<String> tokens = new ArrayList<String>();
		for(JavaParameterHost p : this.fields)
		{
			if(p.isPrimary())
				tokens.add(p.getName());
		}
		if(tokens.size() > 0)
			return StringUtils.join(tokens,",");
		else
			return this.fields.get(0).getName();
	}
	
	public Set<String> getDaoImports()
	{
		Set<String> imports = new TreeSet<String>();
		imports.add("com.ctrip.platform.dal.dao.*");
		imports.add("com.ctrip.platform.dal.dao.helper.*");
		imports.add(java.sql.ResultSet.class.getName());
		imports.add(java.sql.SQLException.class.getName());
		imports.add(java.sql.Timestamp.class.getName());
		imports.add(java.util.List.class.getName());
		
		return imports;
	}
	
	public Set<String> getTestImports()
	{
		Set<String> imports = new TreeSet<String>();
		imports.add(java.util.List.class.getName());
		return imports;
	}
	
	public Set<String> getPojoImports()
	{
Set<String> imports = new TreeSet<String>();
		
		List<JavaParameterHost> allTypes = new ArrayList<JavaParameterHost>(fields);
		for(JavaParameterHost field: allTypes) {
			Class<?> clazz = field.getJavaClass();
			if(byte[].class.equals(clazz))
				continue;
			if(clazz.getPackage().getName().equals(String.class.getPackage().getName()))
				continue;
			imports.add(clazz.getName());
		}
		return imports;
	}
	
	
	public String getColumns()
	{
		List<String> tokens = new ArrayList<String>();
		for(JavaParameterHost p : this.fields)
		{
			tokens.add(p.getName());
		}
		return StringUtils.join(tokens,",");
	}
}
