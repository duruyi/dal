package com.ctrip.platform.dal.daogen.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import microsoft.sql.DateTimeOffset;

import org.apache.log4j.Logger;
import org.springframework.jdbc.support.JdbcUtils;

import com.ctrip.platform.dal.common.enums.DatabaseCategory;
import com.ctrip.platform.dal.common.enums.DbType;
import com.ctrip.platform.dal.common.enums.ParameterDirection;
import com.ctrip.platform.dal.daogen.Consts;
import com.ctrip.platform.dal.daogen.domain.StoredProcedure;
import com.ctrip.platform.dal.daogen.enums.CurrentLanguage;
import com.ctrip.platform.dal.daogen.host.AbstractParameterHost;
import com.ctrip.platform.dal.daogen.host.csharp.CSharpParameterHost;
import com.ctrip.platform.dal.daogen.host.java.JavaParameterHost;
import com.mysql.jdbc.StringUtils;

public class DbUtils {
	private static Logger log;
	private static List<Integer> validMode;
	private static String regEx = null;
	private static Pattern inRegxPattern = null;

	static {
		log = Logger.getLogger(DbUtils.class);
		validMode = new ArrayList<Integer>();
		validMode.add(DatabaseMetaData.procedureColumnIn);
		validMode.add(DatabaseMetaData.procedureColumnInOut);
		validMode.add(DatabaseMetaData.procedureColumnOut);
		regEx="in\\s(@\\w+)";
		inRegxPattern = Pattern.compile(regEx, java.util.regex.Pattern.CASE_INSENSITIVE);
	}

	public static boolean tableExists(String allInOneName, String tableName) {

		boolean result = false;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);

			String dbType = null;
			if (Consts.databaseType.containsKey(allInOneName)) {
				dbType = Consts.databaseType.get(allInOneName);
			} else {
				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);
			}

			if (dbType.equals("Microsoft SQL Server")) {

				String sql = "select Name from sysobjects where xtype  = 'u' and status>=0 and Name=?";
				PreparedStatement statement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, tableName);
				rs = statement.executeQuery();
				result = rs.next();
				
			} else {
				rs = connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"});
				result = rs.next();
			}
		} catch (SQLException e) {
			log.error(String.format("get table exists error: [allInOneName=%s;tableName=%s]", 
					allInOneName, tableName), e);
		} catch (Exception e) {
			log.error(String.format("get table exists error: [allInOneName=%s;tableName=%s]", 
					allInOneName, tableName), e);
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
		}

		return result;
	}

	/**
	 * 获取所有表名
	 * 
	 * @param server
	 * @param allInOneName
	 * @return
	 * @throws Exception
	 */
	public static List<String> getAllTableNames(String allInOneName) throws Exception {
		List<String> results = new ArrayList<String>();
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			String dbType = null;
			if (Consts.databaseType.containsKey(allInOneName)) {
				dbType = Consts.databaseType.get(allInOneName);
			} else {
				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);
			}

			// 如果是Sql Server，通过Sql语句获取所有视图的名称
			/*if (dbType.equals("Microsoft SQL Server")) {
				JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

				String sql = String
						.format("select Name from sysobjects where xtype ='u' and status>=0",
								dbName);
				results = jdbcTemplate.query(sql, new RowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return rs.getString(1);
					}
				});
			} else {*/

				rs = connection.getMetaData().getTables(null, "dbo", "%", new String[]{"TABLE"});
				String tableName = null;
				while (rs.next()) {
					tableName = rs.getString("TABLE_NAME");
					if(tableName.toLowerCase().equals("sysdiagrams")){
						continue;
					}
					results.add(rs.getString("TABLE_NAME"));
				}
			//}
		} catch (SQLException e) {
//			log.error(String.format("get all table names error: [dbName=%s]", 
//					dbName), e);
			throw e;
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
		}

		return results;
	}

	public static boolean viewExists(String allInOneName, String viewName) {
		boolean result = false;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			String dbType = null;
			if (Consts.databaseType.containsKey(allInOneName)) {
				dbType = Consts.databaseType.get(allInOneName);
			} else {
				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);
			}

			if (dbType.equals("Microsoft SQL Server")) {

				String sql = "select Name from sysobjects where xtype ='v' and status>=0 and Name = ?";
				PreparedStatement statement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, viewName);
				rs = statement.executeQuery();
				result = rs.next();
				
			} else {
				rs = connection.getMetaData().getTables(null, null, viewName, new String[]{ "VIEW" });
				result = rs.next();
			}
		} catch (SQLException e) {
			log.error(String.format("get view exists error: [allInOneName=%s;viewName=%s]", 
					allInOneName, viewName), e);
		} catch (Exception e) {
			log.error(String.format("get view exists error: [allInOneName=%s;viewName=%s]", 
					allInOneName, viewName), e);
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
		}

		return result;
	}

	/**
	 * 获取所有视图
	 * 
	 * @param server
	 * @param allInOneName
	 * @return
	 * @throws Exception
	 */
	public static List<String> getAllViewNames(String allInOneName) throws Exception {

		List<String> results = new ArrayList<String>();
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			String dbType = null;
			if (Consts.databaseType.containsKey(allInOneName)) {
				dbType = Consts.databaseType.get(allInOneName);
			} else {
				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);
			}

			// 如果是Sql Server，通过Sql语句获取所有视图的名称
			/*if (dbType.equals("Microsoft SQL Server")) {
				JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

				String sql = String
						.format("select Name from sysobjects where xtype ='v' and status>=0",
								dbName);
				results = jdbcTemplate.query(sql, new RowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return rs.getString(1);
					}
				});
			} else {*/

				rs = connection.getMetaData().getTables(null, "dbo", "%", new String[]{"VIEW"});
				while (rs.next()) {
					results.add(rs.getString("TABLE_NAME"));
				}
			//}
		} catch (SQLException e) {
//			log.error(String.format("get all view names error: [dbName=%s]", 
//					dbName), e);
			throw e;
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
		}

		return results;
	}

	public static boolean spExists(String allInOneName, final StoredProcedure sp) {

		boolean result = false;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			String dbType = null;
			if (Consts.databaseType.containsKey(allInOneName)) {
				dbType = Consts.databaseType.get(allInOneName);
			} else {
				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);
			}
			// 如果是Sql Server，通过Sql语句获取所有表和视图的名称
			if (dbType.equals("Microsoft SQL Server")) {

				String sql = "select SPECIFIC_SCHEMA,SPECIFIC_NAME from information_schema.routines where routine_type = 'PROCEDURE' and SPECIFIC_SCHEMA=? and SPECIFIC_NAME=?";
				PreparedStatement statement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, sp.getSchema());
				statement.setString(2, sp.getName());
				rs = statement.executeQuery();
				result = rs.next();
				
			}
		} catch (Exception e) {
			log.error(String.format("get sp exists error: [allInOneName=%s;spName=%s]", 
					allInOneName, sp.getName()), e);
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
		}

		return result;
	}

	/**
	 * 
	 * @param server
	 * @param allInOneName
	 * @return
	 * @throws Exception
	 */
	public static List<StoredProcedure> getAllSpNames(String allInOneName)
			throws Exception {

		List<StoredProcedure> results = new ArrayList<StoredProcedure>();
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			String dbType = null;
			if (Consts.databaseType.containsKey(allInOneName)) {
				dbType = Consts.databaseType.get(allInOneName);
			} else {
				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);
			}

			// 如果是Sql Server，通过Sql语句获取所有视图的名称
			if (dbType.equals("Microsoft SQL Server")) {

				String sql = "select SPECIFIC_SCHEMA,SPECIFIC_NAME from information_schema.routines where routine_type = 'PROCEDURE'";
				PreparedStatement statement = connection.prepareStatement(sql,
						ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				rs = statement.executeQuery();
				while(rs.next()){
					StoredProcedure sp = new StoredProcedure();
					sp.setSchema(rs.getString(1));
					sp.setName(rs.getString(2));
					results.add(sp);
				}
				
			}
		} catch (SQLException e) {
			log.error(String.format("get all sp names error: [allInOneName=%s]", allInOneName), e);
			throw e;
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
		}
		return results;
	}

	/**
	 * 获取存储过程的所有参数
	 * 
	 * @param server
	 * @param allInOneName
	 * @param sp
	 * @return
	 */
	public static List<AbstractParameterHost> getSpParams(String allInOneName,
			StoredProcedure sp, CurrentLanguage language) {
		Connection connection = null;
		List<AbstractParameterHost> parameters = new ArrayList<AbstractParameterHost>();
		try {
			connection = DataSourceUtil.getConnection(allInOneName);

			ResultSet spParams = connection.getMetaData().getProcedureColumns(null, sp.getSchema(), sp.getName(), null);
			boolean terminal = false;
			if (language == CurrentLanguage.CSharp) {
				while (spParams.next()) {
					int paramMode = spParams.getShort("COLUMN_TYPE");

					if (!validMode.contains(paramMode)) {
						continue;
					}

					CSharpParameterHost host = new CSharpParameterHost();
					DbType dbType =getDotNetDbType(spParams.getString("TYPE_NAME"), spParams
							.getInt("DATA_TYPE"), spParams.getInt("LENGTH"));
					host.setDbType(dbType);
					host.setNullable(spParams.getShort("NULLABLE") == DatabaseMetaData.columnNullable);

					if (paramMode == DatabaseMetaData.procedureColumnIn) {
						host.setDirection(ParameterDirection.Input);
					} else if (paramMode == DatabaseMetaData.procedureColumnInOut) {
						host.setDirection(ParameterDirection.InputOutput);
					} else {
						host.setDirection(ParameterDirection.Output);
					}

					host.setName(spParams.getString("COLUMN_NAME"));
					host.setType(DbType.getCSharpType(host.getDbType()));
					host.setNullable(spParams.getShort("NULLABLE") == DatabaseMetaData.columnNullable);

					if (host.getType() == null) {
						host.setType("string");
						host.setDbType(DbType.AnsiString);
					}

					parameters.add(host);
				}
			} else if (language == CurrentLanguage.Java) {
				while (spParams.next()) {
					int paramMode = spParams.getShort("COLUMN_TYPE");
					// For My Sql, there is no ORDINAL_POSITION
					// int paramIndex = spParams.getInt("ORDINAL_POSITION");
					if (!validMode.contains(paramMode)) {
						continue;
					}

					JavaParameterHost host = new JavaParameterHost();
					// host.setIndex(paramIndex);
					host.setSqlType(spParams.getInt("DATA_TYPE"));

					if (paramMode == DatabaseMetaData.procedureColumnIn) {
						host.setDirection(ParameterDirection.Input);
					} else if (paramMode == DatabaseMetaData.procedureColumnInOut) {
						host.setDirection(ParameterDirection.InputOutput);
					} else {
						host.setDirection(ParameterDirection.Output);
					}

					host.setName(spParams.getString("COLUMN_NAME").replace("@",""));
					Class<?> javaClass = Consts.jdbcSqlTypeToJavaClass.get(host.getSqlType());
					if(null == javaClass){
						if(-153 == host.getSqlType()){
							log.error(String.format("The Table-Valued Parameters is not support for JDBC. [%s, %s]", 
									allInOneName, sp.getName()));
							terminal = true;
							break;
						}else{
							log.fatal(String.format("The java type cant be mapped.[%s, %s, %s, %s, %s]", 
									host.getName(), allInOneName, sp.getName(), host.getSqlType(), javaClass));
							terminal = true;
							break;
						}
					}
					host.setJavaClass(javaClass);

					parameters.add(host);
				}
			}
			return terminal ? null : parameters;
		} catch (SQLException e) {
			log.error(String.format("get sp params error: [allInOneName=%s;spName=%s;language=%s]", 
					allInOneName,sp.getName(), language.name()), e);
		} catch (Exception e) {
			log.error(String.format("get sp params error: [allInOneName=%s;spName=%s;language=%s]", 
					allInOneName,sp.getName(), language.name()), e);
		} finally {
			JdbcUtils.closeConnection(connection);
		}

		return null;

	}

	/**
	 * 由调用者负责Connection的生命周期！！！！
	 * 
	 * @param connection
	 * @return
	 */
	public static List<String> getPrimaryKeyNames(String allInOneName,
			String tableName) {

		Connection connection = null;
		// 获取所有主键
		ResultSet primaryKeyRs = null;
		List<String> primaryKeys = new ArrayList<String>();
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			primaryKeyRs = connection.getMetaData().getPrimaryKeys(null, null, tableName);

			while (primaryKeyRs.next()) {
				primaryKeys.add(primaryKeyRs.getString("COLUMN_NAME"));
			}
		} catch (SQLException e) {
			log.error(String.format("get primary key names error: [allInOneName=%s;tableName=%s]", 
					allInOneName, tableName), e);
		} catch (Exception e) {
			log.error(String.format("get primary key names error: [allInOneName=%s;tableName=%s]", 
					allInOneName, tableName), e);
		} finally {
			JdbcUtils.closeResultSet(primaryKeyRs);
			JdbcUtils.closeConnection(connection);
		}

		return primaryKeys;
	}
	
	private static DbType getDotNetDbType(String typeName, int dataType, int length){
		DbType dbType;
		if(null != typeName && typeName.equalsIgnoreCase("year")){
			dbType = DbType.Int16;
		}else if(typeName.equalsIgnoreCase("uniqueidentifier")){
			dbType = DbType.Guid;
		}else if(null != typeName && typeName.equalsIgnoreCase("sql_variant")){
			dbType = DbType.Object;
		}else if (dataType == 1 && length > 1){
			dbType = DbType.AnsiString;
		}else if(-155 == dataType){
			dbType = DbType.DateTimeOffset;
		}else if(-7 == dataType && length > 1){
			dbType = DbType.UInt64;
		}else {
			dbType =DbType.getDbTypeFromJdbcType(dataType);
		}
		return dbType;
	}

	/**
	 * 由调用者负责Connection的生命周期！！！！
	 * 
	 * @param connection
	 * @return
	 */
	public static List<AbstractParameterHost> getAllColumnNames(String allInOneName,
			String tableName, CurrentLanguage language) {

		Connection connection = null;
		ResultSet allColumnsRs = null;
		List<AbstractParameterHost> allColumns = new ArrayList<AbstractParameterHost>();
		try {
			connection = DataSourceUtil.getConnection(allInOneName);

			allColumnsRs = connection.getMetaData().getColumns(null, null, tableName, null);
			boolean terminal = false;
			if (language == CurrentLanguage.CSharp) {
				Map<String,String> columnComment = getSqlserverColumnComment(allInOneName, tableName);
				while (allColumnsRs.next()) {
					CSharpParameterHost host = new CSharpParameterHost();
					String typeName = allColumnsRs.getString("TYPE_NAME");
					int dataType = allColumnsRs.getInt("DATA_TYPE");
					int length = allColumnsRs.getInt("COLUMN_SIZE");
					
					//特殊处理
					host.setDbType(getDotNetDbType(typeName, dataType, length));
					//host.setName(CommonUtils.normalizeVariable(allColumnsRs.getString("COLUMN_NAME")));
					host.setName(allColumnsRs.getString("COLUMN_NAME"));
					String remark = allColumnsRs.getString("REMARKS");
					if(remark == null){
						String description = columnComment.get(allColumnsRs.getString("COLUMN_NAME").toLowerCase());
						remark = description==null?"":description;
					}
					host.setComment(remark.replace("\n", " "));
					host.setType(DbType.getCSharpType(host.getDbType()));
					host.setIdentity(allColumnsRs.getString("IS_AUTOINCREMENT").equalsIgnoreCase("YES"));
					host.setNullable(allColumnsRs.getShort("NULLABLE") == DatabaseMetaData.columnNullable);
					host.setValueType(Consts.CSharpValueTypes.contains(host.getType()));
					// 仅获取String类型的长度
					if ("string".equalsIgnoreCase(host.getType()))
						host.setLength(length);

					// COLUMN_SIZE
					allColumns.add(host);
				}
			} else if (language == CurrentLanguage.Java) {
				Map<String, Integer> columnSqlType = getColumnSqlType(allInOneName, tableName);
				Map<String, Class<?>> typeMapper = getSqlType2JavaTypeMaper(allInOneName, tableName);
				while (allColumnsRs.next()) {
					JavaParameterHost host = new JavaParameterHost();
					String typeName = allColumnsRs.getString("TYPE_NAME");
					host.setName(allColumnsRs.getString("COLUMN_NAME"));
//					host.setSqlType(allColumnsRs.getInt("DATA_TYPE"));
					host.setSqlType(columnSqlType.get(host.getName()));
					Class<?> javaClass = null;
					if(null != typeMapper && typeMapper.containsKey(host.getName()) ){
						javaClass = typeMapper.get(host.getName());
					}else{
						javaClass = Consts.jdbcSqlTypeToJavaClass.get(host.getSqlType());
					}
					if(null == javaClass){
						if(null != typeName && typeName.equalsIgnoreCase("sql_variant")){
							log.fatal(String.format("The sql_variant is not support by java.[%s, %s, %s, %s, %s]", 
									host.getName(), allInOneName, tableName, host.getSqlType(), javaClass));
							terminal = true;
							break;
						}
						else if(null != typeName && typeName.equalsIgnoreCase("datetimeoffset")){
							javaClass = DateTimeOffset.class;
						}
						else{
							log.fatal(String.format("The java type cant be mapped.[%s, %s, %s, %s, %s]", 
									host.getName(), allInOneName, tableName, host.getSqlType(), javaClass));
							terminal = true;
							break;
						}
					}
					host.setJavaClass(javaClass);
					host.setIndex(allColumnsRs.getInt("ORDINAL_POSITION"));
					host.setIdentity(allColumnsRs.getString("IS_AUTOINCREMENT").equalsIgnoreCase("YES"));
					allColumns.add(host);
				}
			}

			return terminal ? null : allColumns;
		} catch (SQLException e) {
			log.error(String.format("get all column names error: [allInOneName=%s;tableName=%s;language=%s]", 
					allInOneName, tableName, language), e);
		} catch (Exception e) {
			log.error(String.format("get all column names error: [allInOneName=%s;tableName=%s;language=%s]", 
					allInOneName, tableName, language), e);
		} finally {
			JdbcUtils.closeResultSet(allColumnsRs);
			JdbcUtils.closeConnection(connection);
		}

		return null;
	}

	private static Map<String, Class<?>> getSqlType2JavaTypeMaper(String allInOneName, String tableViewName) {
		Map<String, Class<?>> map = new HashMap<String, Class<?>>();
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			String dbType = null;
			if (Consts.databaseType.containsKey(allInOneName)) {
				dbType = Consts.databaseType.get(allInOneName);
			} else {
				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);
			}
			
			String sql = "select * from %s %s";
			if(dbType.equalsIgnoreCase("Microsoft SQL Server")){
				sql = "select top 1 * from " + tableViewName;
			} else {
				sql = "select * from " + tableViewName + " limit 1";
			}
			PreparedStatement ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			ResultSetMetaData rsMeta = rs.getMetaData();
			for(int i=1;i<=rsMeta.getColumnCount();i++){
				String columnName = rsMeta.getColumnName(i);
				Integer sqlType = rsMeta.getColumnType(i);
				Class<?> javaType = null;
				try {
					javaType = Class.forName(rsMeta.getColumnClassName(i));
				} catch (Exception e) {
					e.printStackTrace();
					javaType = Consts.jdbcSqlTypeToJavaClass.get(sqlType);
				}
				if(!map.containsKey(columnName) && null != javaType) {
					map.put(columnName, javaType);
				}
			}
			
		} catch (SQLException e) {
			log.error(String.format("get sql-type to java-type maper error: [allInOneName=%s;tableViewName=%s]",
					allInOneName, tableViewName), e);
		} catch(Exception e){
			log.error(String.format("get sql-type to java-type maper error: [allInOneName=%s;tableViewName=%s]",
					allInOneName, tableViewName), e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
			
		}
		return map;
	}
	
	private static Map<String, Integer> getColumnSqlType(String allInOneName, String tableViewName) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			String dbType = null;
			if (Consts.databaseType.containsKey(allInOneName)) {
				dbType = Consts.databaseType.get(allInOneName);
			} else {
				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);
			}
			
			String sql = "select * from %s %s";
			if(dbType.equalsIgnoreCase("Microsoft SQL Server")){
				sql = "select top 1 * from " + tableViewName;
			} else {
				sql = "select * from " + tableViewName + " limit 1";
			}
			PreparedStatement ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			ResultSetMetaData rsMeta = rs.getMetaData();
			for(int i=1;i<=rsMeta.getColumnCount();i++){
				String columnName = rsMeta.getColumnName(i);
				Integer sqlType = rsMeta.getColumnType(i);
				if(!map.containsKey(columnName) && null != sqlType) {
					map.put(columnName, sqlType);
				}
			}
			
		} catch (SQLException e) {
			log.error(String.format("get sql-type to java-type maper error: [allInOneName=%s;tableViewName=%s]",
					allInOneName, tableViewName), e);
		} catch(Exception e){
			log.error(String.format("get sql-type to java-type maper error: [allInOneName=%s;tableViewName=%s]",
					allInOneName, tableViewName), e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
			
		}
		return map;
	}
	
	
	public static List<AbstractParameterHost> getSelectFieldHosts(String allInOneName, String sql, CurrentLanguage language){
		List<AbstractParameterHost> hosts = new ArrayList<AbstractParameterHost>();
		String testSql = sql;
		int whereIndex = StringUtils.indexOfIgnoreCase(testSql, "where");
		if(whereIndex > 0)
			testSql = sql.substring(0, whereIndex);

		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = DataSourceUtil.getConnection(allInOneName);
			DatabaseCategory dbCategory = DatabaseCategory.SqlServer;
			String dbType = DbUtils.getDbType(allInOneName);
			if (null != dbType && !dbType.equalsIgnoreCase("Microsoft SQL Server")) {
				dbCategory = DatabaseCategory.MySql;
			}
			
			if(dbCategory.equals(DatabaseCategory.MySql)){
				testSql = testSql + " limit 1";
			}
			else{
				testSql = testSql.replace("select", "select top(1)");
			}
			PreparedStatement ps = connection.prepareStatement(testSql);
			rs = ps.executeQuery();
			ResultSetMetaData rsMeta = rs.getMetaData();
			
			if(language == CurrentLanguage.CSharp){
				for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
					CSharpParameterHost pHost = new CSharpParameterHost();
					pHost.setName(rsMeta.getColumnLabel(i));
					pHost.setDbType(DbType.getDbTypeFromJdbcType(rsMeta.getColumnType(i)));
					pHost.setType(DbType.getCSharpType(pHost.getDbType()));
					pHost.setIdentity(false);
					pHost.setNullable(true);
					pHost.setValueType(Consts.CSharpValueTypes.contains(pHost.getType()));
					pHost.setPrimary(false);
					pHost.setLength(rsMeta.getColumnDisplaySize(i));
					hosts.add(pHost);
				}
			}else{
				for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
					JavaParameterHost paramHost = new JavaParameterHost();
					paramHost.setName(rsMeta.getColumnLabel(i));
					paramHost.setSqlType(rsMeta.getColumnType(i));
					Class<?> javaClass = null;
					try {
						javaClass = Class.forName(rsMeta.getColumnClassName(i));
					} catch (Exception e) {
						e.printStackTrace();
						javaClass = Consts.jdbcSqlTypeToJavaClass.get(paramHost.getSqlType());
					}
					paramHost.setJavaClass(javaClass);
					paramHost.setIdentity(false);
					paramHost.setNullable(false);
					paramHost.setPrimary(false);
					paramHost.setLength(rsMeta.getColumnDisplaySize(i));
					hosts.add(paramHost);
				}
			}
		}catch (SQLException e) {
			log.error(String.format("get select field error: [allInOneName=%s;sql=%s;language=%s]", 
					allInOneName, sql, language), e);
		} catch (Exception e) {
			log.error(String.format("get select field error: [allInOneName=%s;sql=%s;language=%s]", 
					allInOneName, sql, language), e);
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
		}
	
		return hosts;
	}
	
	/**
	 * 测试查询SQL是否合法
	 * 
	 * @param server
	 * @param allInOneName
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public static List<AbstractParameterHost> testAQuerySql(String allInOneName, String sql,
			String params,CurrentLanguage language , boolean justTest) throws Exception {
		String[] parameters = params.split(";");

		Connection connection = null;
		ResultSet rs = null;
		try {
			
			Matcher m = inRegxPattern.matcher(sql);
			String temp=sql;
			while(m.find()) {
				temp = temp.replace(m.group(1), String.format("(?) "));
	    	}
			
			String replacedSql = temp.replaceAll("[@:]\\w+", "?");

			connection = DataSourceUtil.getConnection(allInOneName);

			PreparedStatement ps = connection.prepareStatement(replacedSql);

			int index = 0;
			for (String param : parameters) {
				if (param != null && !param.isEmpty()) {
					String[] tuple = param.split(",");

					try {
						index = Integer.valueOf(tuple[0]);
					} catch (NumberFormatException ex) {
						index++;
					}
					ps.setObject(index, mockATest(Integer.valueOf(tuple[1])),
							Integer.valueOf(tuple[1]));
				}
			}

			rs = ps.executeQuery();
			
			if(justTest){
				return new ArrayList<AbstractParameterHost>();
			}

			ResultSetMetaData rsMeta = rs.getMetaData();
			if(language == CurrentLanguage.CSharp){
				List<AbstractParameterHost> pHosts = new ArrayList<AbstractParameterHost>();
				for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
					CSharpParameterHost pHost = new CSharpParameterHost();
					pHost.setName(rsMeta.getColumnLabel(i));
					String typename = rsMeta.getColumnTypeName(i);
					int dataType = rsMeta.getColumnType(i);
					
					DbType dbType;

					if(null != typename && typename.equalsIgnoreCase("year"))
						dbType = DbType.Int16;
					else if(typename.equalsIgnoreCase("uniqueidentifier")){
						dbType = DbType.Guid;
					}else if(null != typename && typename.equalsIgnoreCase("sql_variant"))
						dbType = DbType.Object;
					else if(-155 == dataType){
						dbType = DbType.DateTimeOffset;
					}
					else
						dbType =DbType.getDbTypeFromJdbcType(dataType);
					
					pHost.setDbType(dbType);
					pHost.setType(DbType.getCSharpType(pHost.getDbType()));
					pHost.setIdentity(false);
					pHost.setNullable(false);
					pHost.setPrimary(false);
					pHost.setLength(rsMeta.getColumnDisplaySize(i));
					pHosts.add(pHost);
				}
				return pHosts;
			}else{
				List<AbstractParameterHost> paramHosts = new ArrayList<AbstractParameterHost>();
				for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
					JavaParameterHost paramHost = new JavaParameterHost();
					paramHost.setName(rsMeta.getColumnLabel(i));
					paramHost.setSqlType(rsMeta.getColumnType(i));
					//paramHost.setJavaClass(Consts.jdbcSqlTypeToJavaClass.get(paramHost.getSqlType()));
					Class<?> javaClass = null;
					try {
						javaClass = Class.forName(rsMeta.getColumnClassName(i));
					} catch (Exception e) {
						e.printStackTrace();
						javaClass = Consts.jdbcSqlTypeToJavaClass.get(paramHost.getSqlType());
					}
					paramHost.setJavaClass(javaClass);
					paramHost.setIdentity(false);
					paramHost.setNullable(false);
					paramHost.setPrimary(false);
					paramHost.setLength(rsMeta.getColumnDisplaySize(i));
					paramHosts.add(paramHost);
				}
				
				return paramHosts;
			}
			
			
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeConnection(connection);
		}
		
	}
	
	public static Object mockATest(int javaSqlTypes) {
		switch (javaSqlTypes) {
			case java.sql.Types.BIT:
				return true;
			case java.sql.Types.TINYINT:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.INTEGER:
			case java.sql.Types.BIGINT:
				return 1;
			case java.sql.Types.REAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.DECIMAL:
				return 1.0;
			case java.sql.Types.CHAR:
				return 't';
			case java.sql.Types.DATE:
				return "2012-01-01";
			case java.sql.Types.TIME:
				return "10:00:00";
			case java.sql.Types.TIMESTAMP:
				return "2012-01-01 10:00:00";
			default:
				return "test";
		}
	}

	public static String getDbType(String allInOneName) throws Exception {

		String dbType = null;
		if (Consts.databaseType.containsKey(allInOneName)) {
			dbType = Consts.databaseType.get(allInOneName);
		} else {
			Connection connection = null;
			try {
				connection = DataSourceUtil.getConnection(allInOneName);

				dbType = connection.getMetaData().getDatabaseProductName();
				Consts.databaseType.put(allInOneName, dbType);

			} catch (Exception ex) {
//				log.error(String.format("get db type error: [dbName=%s]", dbName), ex);
				throw ex;
			} finally {
				JdbcUtils.closeConnection(connection);
			}
		}
		return dbType;
	}
	
	public static DatabaseCategory getDatabaseCategory(String allInOneName)
			throws Exception {

		DatabaseCategory dbCategory = DatabaseCategory.SqlServer;

		String dbType = DbUtils.getDbType(allInOneName);

		if (null != dbType && !dbType.equalsIgnoreCase("Microsoft SQL Server")) {

			dbCategory = DatabaseCategory.MySql;

		}

		return dbCategory;

	}
	
	private static Map<String,String> getSqlserverColumnComment(String allInOneName, String tableName) throws Exception{
		Map<String,String> map = new HashMap<String,String>();
		if(getDatabaseCategory(allInOneName)==DatabaseCategory.MySql){
			return map;
		}
		String sql = ""
				+ "SELECT sys.columns.name as name, "
				+ "       CONVERT(VARCHAR(1000), (SELECT VALUE "
				+ "                              FROM   sys.extended_properties "
				+ "                              WHERE  sys.extended_properties.major_id = sys.columns.object_id "
				+ "                                     AND sys.extended_properties.minor_id = sys.columns.column_id)) AS description "
				+ "FROM   sys.columns, "
				+ "       sys.tables "
				+ "WHERE  sys.columns.object_id = sys.tables.object_id "
				+ "       AND sys.tables.name = ? "
				+ "ORDER  BY sys.columns.column_id ";
		Connection connection = DataSourceUtil.getConnection(allInOneName);
		PreparedStatement stmt = connection.prepareStatement(sql);
		stmt.setObject(1, tableName);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			map.put(rs.getString("name").toLowerCase(), rs.getString("description"));
		}
		JdbcUtils.closeResultSet(rs);
		JdbcUtils.closeConnection(connection);
		return map;
	}
	
	public static void main(String[] args){
		List<AbstractParameterHost> hosts = getSelectFieldHosts("dao_test", "select name from person where  age = ?", CurrentLanguage.Java);
		System.out.println(hosts.size());
	}
}
