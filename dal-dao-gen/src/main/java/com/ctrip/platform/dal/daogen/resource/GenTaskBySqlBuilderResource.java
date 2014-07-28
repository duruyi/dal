
package com.ctrip.platform.dal.daogen.resource;

import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.jasig.cas.client.util.AssertionHolder;

import com.ctrip.platform.dal.common.enums.DatabaseCategory;
import com.ctrip.platform.dal.daogen.dao.DaoBySqlBuilder;
import com.ctrip.platform.dal.daogen.domain.Status;
import com.ctrip.platform.dal.daogen.entity.DatabaseSetEntry;
import com.ctrip.platform.dal.daogen.entity.GenTaskBySqlBuilder;
import com.ctrip.platform.dal.daogen.entity.LoginUser;
import com.ctrip.platform.dal.daogen.enums.CurrentLanguage;
import com.ctrip.platform.dal.daogen.utils.DbUtils;
import com.ctrip.platform.dal.daogen.utils.SpringBeanGetter;
import com.ctrip.platform.dal.daogen.utils.SqlBuilder;

/**
 * 构建SQL（生成的代码绑定到模板）
 * @author gzxia
 *
 */
@Resource
@Singleton
@Path("task/auto")
public class GenTaskBySqlBuilderResource {
	
	private static DaoBySqlBuilder daoBySqlBuilder;

	static {
		daoBySqlBuilder = SpringBeanGetter.getDaoBySqlBuilder();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Status addTask(@FormParam("id") int id,
			@FormParam("project_id") int project_id,
			@FormParam("db_name") String set_name,
			@FormParam("table_name") String table_name,
			@FormParam("method_name") String method_name,
			@FormParam("sql_style") String sql_style, // C#风格或者Java风格
			@FormParam("crud_type") String crud_type,
			@FormParam("fields") String fields,
			@FormParam("condition") String condition,
			@FormParam("sql_content") String sql_content,
			@FormParam("version") int version,
			@FormParam("action") String action,
			@FormParam("params") String params,
			@FormParam("comment") String comment,
			@FormParam("scalarType") String scalarType,
			@FormParam("pagination") boolean pagination,
			@FormParam("orderby") String orderby) {
		
		GenTaskBySqlBuilder task = new GenTaskBySqlBuilder();

		if (action.equalsIgnoreCase("delete")) {
			task.setId(id);
			if (0 >= daoBySqlBuilder.deleteTask(task)) {
				return Status.ERROR;
			}	
		}else{
			String userNo = AssertionHolder.getAssertion().getPrincipal()
					.getAttributes().get("employee").toString();
			LoginUser user = SpringBeanGetter.getDaoOfLoginUser().getUserByNo(userNo);
			
			task.setProject_id(project_id);
			task.setDatabaseSetName(set_name);
			task.setTable_name(table_name);
			task.setMethod_name(method_name);
			task.setSql_style(sql_style);
			task.setCrud_type(crud_type);
			task.setFields(fields);
			task.setCondition(condition);
			task.setUpdate_user_no(user.getUserName()+"("+userNo+")");
			task.setUpdate_time(new Timestamp(System.currentTimeMillis()));
			task.setComment(comment);
			task.setScalarType(scalarType);
			task.setPagination(pagination);
			task.setOrderby(orderby);
			
			if(action.equalsIgnoreCase("update")){
				task.setId(id);
				task.setVersion(daoBySqlBuilder.getVersionById(id));
				task.setSql_content(sql_content);
				if (0 >= daoBySqlBuilder.updateTask(task)) {
					Status status = Status.ERROR;
					status.setInfo("更新出错，数据是否合法？或者已经有同名方法？");
					return status;
				}
			}else{
				task.setGenerated(false);
				task.setVersion(1);
				task.setSql_content(sql_content);
				if (0 >= daoBySqlBuilder.insertTask(task)) {
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
			CurrentLanguage lang = "java".equals(sql_style)?CurrentLanguage.Java:CurrentLanguage.CSharp;
			String pagingSQL = SqlBuilder.pagingQuerySql(sql_content, DbUtils.getDatabaseCategory(databaseSetEntry.getConnectionString()), lang);
			status.setInfo(pagingSQL);
		} catch (Exception e) {
			status = Status.ERROR;
			status.setInfo(e.getMessage());
			return status;
		}
		
		return status;
	}
	
	@POST
	@Path("getDatabaseCategory")
	public Status getDatabaseCategory(@FormParam("db_set_name") String db_set_name){
		Status status = Status.OK;
		DatabaseSetEntry databaseSetEntry = SpringBeanGetter.getDaoOfDatabaseSet().getMasterDatabaseSetEntryByDatabaseSetName(db_set_name);
		try {
			DatabaseCategory category = DbUtils.getDatabaseCategory(databaseSetEntry.getConnectionString());
			if(DatabaseCategory.MySql==category){
				status.setInfo("MySql");
			}else{
				status.setInfo("SqlServer");
			}
		} catch (Exception e) {
			status = Status.ERROR;
			status.setInfo(e.getMessage());
			return status;
		}
		return status;
	}


}
