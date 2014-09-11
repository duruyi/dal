
package com.ctrip.platform.dal.daogen.resource;

import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jasig.cas.client.util.AssertionHolder;

import com.ctrip.platform.dal.daogen.dao.DaoByFreeSql;
import com.ctrip.platform.dal.daogen.domain.Status;
import com.ctrip.platform.dal.daogen.entity.DatabaseSetEntry;
import com.ctrip.platform.dal.daogen.entity.GenTaskByFreeSql;
import com.ctrip.platform.dal.daogen.entity.LoginUser;
import com.ctrip.platform.dal.daogen.enums.CurrentLanguage;
import com.ctrip.platform.dal.daogen.sql.validate.SQLValidation;
import com.ctrip.platform.dal.daogen.sql.validate.ValidateResult;
import com.ctrip.platform.dal.daogen.utils.DbUtils;
import com.ctrip.platform.dal.daogen.utils.SpringBeanGetter;
import com.ctrip.platform.dal.daogen.utils.SqlBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 复杂查询（额外生成实体类）
 * @author gzxia
 *
 */
@Resource
@Singleton
@Path("task/sql")
public class GenTaskByFreeSqlResource {
	
	private static DaoByFreeSql daoByFreeSql;
	
	private static ObjectMapper mapper = new ObjectMapper();

	static {
		daoByFreeSql = SpringBeanGetter.getDaoByFreeSql();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Status addTask(@FormParam("id") int id,
			@FormParam("project_id") int project_id,
			@FormParam("db_name") String set_name,
			@FormParam("class_name") String class_name,
			@FormParam("pojo_name") String pojo_name,
			@FormParam("method_name") String method_name,
			@FormParam("crud_type") String crud_type,
			@FormParam("sql_content") String sql_content,
			@FormParam("params") String params,
			@FormParam("version") int version,
			@FormParam("action") String action,
			@FormParam("comment") String comment,
			@FormParam("scalarType") String scalarType,
			@FormParam("pagination") boolean pagination,
			@FormParam("sql_style") String sql_style// C#风格或者Java风格
			) {
		
		GenTaskByFreeSql task = new GenTaskByFreeSql();

		if (action.equalsIgnoreCase("delete")) {
			task.setId(id);
			if (0 >= daoByFreeSql.deleteTask(task)) {
				return Status.ERROR;
			}	
		}else{
			String userNo = AssertionHolder.getAssertion().getPrincipal()
					.getAttributes().get("employee").toString();
			LoginUser user = SpringBeanGetter.getDaoOfLoginUser().getUserByNo(userNo);
		
			task.setProject_id(project_id);
			task.setDatabaseSetName(set_name);
			task.setClass_name(class_name);
			task.setPojo_name(pojo_name);
			task.setMethod_name(method_name);
			task.setCrud_type(crud_type);
			task.setSql_content(sql_content);
			task.setParameters(params);
			task.setUpdate_user_no(user.getUserName()+"("+userNo+")");
			task.setUpdate_time(new Timestamp(System.currentTimeMillis()));
			task.setComment(comment);
			task.setScalarType(scalarType);
			task.setPagination(pagination);
			task.setSql_style(sql_style);
			
			if("简单类型".equals(pojo_name)){
				task.setPojoType("SimpleType");
			}else{
				task.setPojoType("EntityType");
			}
			
			if(action.equalsIgnoreCase("update")){
				task.setId(id);
				task.setVersion(daoByFreeSql.getVersionById(id));
				if (0 >= daoByFreeSql.updateTask(task)) {
					Status status = Status.ERROR;
					status.setInfo("更新出错，数据是否合法？或者已经有同名方法？");
					return status;
				}
			}else{
				task.setGenerated(false);
				task.setVersion(1);
				if (0 >= daoByFreeSql.insertTask(task)) {
					Status status = Status.ERROR;
					status.setInfo("新增出错，数据是否合法？或者已经有同名方法？");
					return status;
				}
			}
		}

		return Status.OK;
	}
	
	@POST
	@Path("buildPagingSQL")
	public Status buildPagingSQL(@FormParam("db_name") String db_set_name,//dbset name
			@FormParam("sql_style") String sql_style, // C#风格或者Java风格
			@FormParam("sql_content") String sql_content){
		Status status = Status.OK;
		try {
			DatabaseSetEntry databaseSetEntry = SpringBeanGetter.getDaoOfDatabaseSet().getMasterDatabaseSetEntryByDatabaseSetName(db_set_name);
			CurrentLanguage lang = sql_content.contains("@")?CurrentLanguage.CSharp:CurrentLanguage.Java;
			String pagingSQL = SqlBuilder.pagingQuerySql(sql_content, DbUtils.getDatabaseCategory(databaseSetEntry.getConnectionString()), lang);
			status.setInfo(pagingSQL);
		} catch (Exception e) {
			status = Status.ERROR;
			status.setInfo(e.getMessage()==null?e.getCause().getMessage():e.getMessage());
			return status;
		}
		
		return status;
	}
	
	@POST
	@Path("getMockValue")
	public Status getMockValue(@FormParam("params") String params){
		Status status = Status.OK;
		int []sqlTypes = getSqlTypes(params);
		Object []values = SQLValidation.mockStringValues(sqlTypes);
		try {
			status.setInfo(mapper.writeValueAsString(values));
		} catch (JsonProcessingException e) {
			status = Status.ERROR;
			status.setInfo("获取mock value异常.");
		}
		return status;
	}
	
	private int[] getSqlTypes(String params){
		if(params==null || "".equalsIgnoreCase(params)){
			return new int[0];
		}
		String []parameters = params.split(";");
		int []sqlTypes = new int[parameters.length];
		int i=0;
		for(String param : parameters){
			if (param != null && !param.isEmpty()){
				sqlTypes[i++] = Integer.valueOf(param.split(",")[1]);
			}
		}
		return sqlTypes;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("sqlValidate")
	public Status validateSQL(@FormParam("db_name") String set_name,
			@FormParam("crud_type") String crud_type,
			@FormParam("sql_content") String sql_content,
			@FormParam("params") String params,
			@FormParam("pagination") boolean pagination,
			@FormParam("mockValues") String mockValues) {

		Status status = Status.OK;
		
		try {
			sql_content = sql_content.replaceAll("[@:]\\w+", "?");
			int []sqlTypes = getSqlTypes(params);
			String []vals = mockValues.split(";");
			
			DatabaseSetEntry databaseSetEntry = SpringBeanGetter.getDaoOfDatabaseSet().getMasterDatabaseSetEntryByDatabaseSetName(set_name);
			String dbName = databaseSetEntry.getConnectionString();
			
			ValidateResult validResult = null;
			if (pagination && "select".equalsIgnoreCase(crud_type)) {
				sql_content = SqlBuilder.pagingQuerySql(sql_content, DbUtils.getDatabaseCategory(dbName), CurrentLanguage.Java);
				sql_content = String.format(sql_content, 1, 2);
			}
			
			if("select".equalsIgnoreCase(crud_type)){
				validResult = SQLValidation.queryValidate(dbName, sql_content, sqlTypes, vals);
			}else{
				validResult = SQLValidation.updateValidate(dbName, sql_content, sqlTypes, vals);
			}
			
			if(validResult!=null && validResult.isPassed()){
				status.setInfo(validResult.getMessage());
			}else{
				status = Status.ERROR;
				status.setInfo(validResult.getMessage());
			}
		} catch (Exception e) {
			status = Status.ERROR;
			status.setInfo(e.getMessage());
		}
		
		return status;

	}
	
//	@POST
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("test_sql")
//	public Status verifyQuery(@FormParam("db_name") String set_name,
//			@FormParam("sql_content") String sql,
//			@FormParam("params") String params) {
//
//		Status status = Status.OK;
//		
//		DatabaseSetEntry databaseSetEntry = SpringBeanGetter.getDaoOfDatabaseSet().getMasterDatabaseSetEntryByDatabaseSetName(set_name);
//		String dbName = databaseSetEntry.getConnectionString();
//		
//		try {
//			DbUtils.testAQuerySql(dbName, sql, params, CurrentLanguage.CSharp, true);
//		} catch (Exception e) {
//			status = Status.ERROR;
//			status.setInfo(e.getMessage());
//			return status;
//		}
//		
//		return status;
//
//	}

}
