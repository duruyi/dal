package com.ctrip.platform.dal.dao.helper;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * @author gzxia
 * 
 */
public class EntityManager<T> {

	private Class<T> clazz = null;
	private Field[] fields = null;
	
	public EntityManager(Class<T> clazz) throws SQLException {
		this.clazz = clazz;
		this.fields = getFields(clazz);
	}
	
	private Field[] getFields(Class<T> clazz) throws SQLException {
		Field[] fields = clazz.getDeclaredFields();
		emptyCheck(fields);
		setFieldAccessible(fields);
		return fields;
	}
	
	private void emptyCheck(Field[] fields) throws SQLException {
		if (null == fields || fields.length == 0)
			throw new SQLException("The entity[" + clazz.getName() +"] has no fields.");
	}
	
	private void setFieldAccessible(Field[] fields) {
		for(Field field : fields)
			field.setAccessible(true);
	}
	
	public String getTableName() {
		Table table = clazz.getAnnotation(Table.class);
		if (table != null && table.name() != null)
			return table.name();
		Entity entity = clazz.getAnnotation(Entity.class);
		if ( entity != null && (!entity.name().isEmpty()) )
			return entity.name();
		return clazz.getSimpleName();
	}
	
	public boolean isAutoIncrement() throws SQLException {
		for (Field field : fields) {
			GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
			if (null != generatedValue && (generatedValue.strategy() == GenerationType.AUTO 
					|| generatedValue.strategy() == GenerationType.IDENTITY)) {
				return true;
			}
		}
		return false;
	}
	
	public String[] getSensitiveColumnNames() {
		List<String> sensitiveColumnNames = new ArrayList<String>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Sensitive sensitive = field.getAnnotation(Sensitive.class);
			if (sensitive != null)
				sensitiveColumnNames.add(getColumnName(field));
		}
		String[] scns = new String[sensitiveColumnNames.size()];
		sensitiveColumnNames.toArray(scns);
		return scns;
	}
	
	public String[] getPrimaryKeyNames() throws SQLException {
		List<String> primaryKeyNames = new ArrayList<String>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Id id = field.getAnnotation(Id.class);
			if(id != null)
				primaryKeyNames.add(getColumnName(field));
		}
		String[] primaryKeys = new String[primaryKeyNames.size()];
		primaryKeyNames.toArray(primaryKeys);
		return primaryKeys;
	}
	
	public Field[] getIdentity() {
		List<Field> identities = new ArrayList<Field>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Id id = field.getAnnotation(Id.class);
			GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
			if (id != null && generatedValue != null && generatedValue.strategy() == GenerationType.AUTO) {
				identities.add(field);
			}
		}
		Field[] result = new Field[identities.size()];
		identities.toArray(result);
		return result;
	}
	
	public Map<String, Field> getFieldMap() throws SQLException {
		Map<String, Field> fieldsMap = new HashMap<String, Field>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String columnName = getColumnName(field);
			if(!fieldsMap.containsKey(columnName))
				fieldsMap.put(columnName, field);
		}
		return fieldsMap;
	}
	
	public String[] getColumnNames() {
		int length = fields.length;
		String[] columnNames = new String[length];
		for (int i = 0; i < length; i++) {
			Field field = fields[i];
			columnNames[i] = getColumnName(field);
		}
		return columnNames;
	}
	
	public int[] getColumnTypes() throws SQLException {
		int length = fields.length;
		int[] columnTypes = new int[length];
		for (int i = 0; i < length; i++) {
			Field field = fields[i];
			Type sqlType = field.getAnnotation(Type.class);
			if (sqlType == null)
				throw new SQLException("Each field of entity[" + clazz.getName() +"] must declare it's Type annotation.");
			columnTypes[i] = sqlType.value();
		}
		return columnTypes;
	}
	
	private String getColumnName(Field field) {
		Column column = field.getAnnotation(Column.class);
		if (null != column && column.name() != null)
			return column.name().isEmpty() ? field.getName() : column.name();
		return field.getName();
	}
	
	public static void setValue(Field field, Object entity, Object val)
			throws ReflectiveOperationException {
		if (val == null) {
			field.set(entity, val);
			return;
		}
		if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
			field.set(entity, ((Number) val).intValue());
			return;
		}
		if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
			field.set(entity, ((Number) val).longValue());
			return;
		}
		if (field.getType().equals(Byte.class) || field.getType().equals(byte.class)) {
			field.set(entity, ((Number) val).byteValue());
			return;
		}
		if (field.getType().equals(Short.class) || field.getType().equals(short.class)) {
			field.set(entity, ((Number) val).shortValue());
			return;
		}
		if (field.getType().equals(Float.class) || field.getType().equals(float.class)) {
			field.set(entity, ((Number) val).floatValue());
			return;
		}
		if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
			field.set(entity, ((Number) val).doubleValue());
			return;
		}
		field.set(entity, val);
	}
	
	public static Object getValue(Field field, Object entity) throws IllegalArgumentException, IllegalAccessException {
		return field.get(entity);
	}
}
