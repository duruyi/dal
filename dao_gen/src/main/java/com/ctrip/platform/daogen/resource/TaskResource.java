package com.ctrip.platform.daogen.resource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;

import com.ctrip.platform.daogen.Consts;
import com.ctrip.platform.daogen.dao.AutoTaskDAO;
import com.ctrip.platform.daogen.dao.MasterDAO;
import com.ctrip.platform.daogen.dao.SPTaskDAO;
import com.ctrip.platform.daogen.dao.SqlTaskDAO;
import com.ctrip.platform.daogen.pojo.AutoTask;
import com.ctrip.platform.daogen.pojo.SpTask;
import com.ctrip.platform.daogen.pojo.SqlTask;
import com.ctrip.platform.daogen.pojo.Status;
import com.ctrip.platform.daogen.pojo.TaskAggeragation;

/**
 * The schema of {daogen.task} { "project_id": , "task_type": , "database" : ,
 * "table": , "dao_name": , "func_name": , "sql_spname": , "fields": ,
 * "condition": , "crud": } The schema of {daogen.task_meta} { "database" : ,
 * "table": , "primary_key": , "fields": }
 * 
 * @author gawu
 * 
 */
@Resource
@Singleton
@Path("task")
public class TaskResource {

	private static MasterDAO master;

	private static AutoTaskDAO autoTask;

	private static SPTaskDAO spTask;

	private static SqlTaskDAO sqlTask;

	static {
		master = new MasterDAO();
		autoTask = new AutoTaskDAO();
		spTask = new SPTaskDAO();
		sqlTask = new SqlTaskDAO();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TaskAggeragation getTasks(@QueryParam("project_id") String id) {

		ResultSet autoTaskResultSet = autoTask.getTasksByProjectId(Integer
				.valueOf(id));

		ResultSet spTaskResultSet = spTask.getTasksByProjectId(Integer
				.valueOf(id));

		ResultSet sqlTaskResultSet = sqlTask.getTasksByProjectId(Integer
				.valueOf(id));

		List<AutoTask> autoTasks = new ArrayList<AutoTask>();

		List<SpTask> spTasks = new ArrayList<SpTask>();

		List<SqlTask> sqlTasks = new ArrayList<SqlTask>();

		TaskAggeragation allTasks = new TaskAggeragation();

		try {
			while (autoTaskResultSet.next()) {
				AutoTask t = new AutoTask();
				t.setId(autoTaskResultSet.getInt(1));
				t.setProject_id(autoTaskResultSet.getInt(2));
				t.setDb_name(autoTaskResultSet.getString(3));
				t.setTable_name(autoTaskResultSet.getString(4));
				t.setClass_name(autoTaskResultSet.getString(5));
				t.setMethod_name(autoTaskResultSet.getString(6));
				t.setSql_style(autoTaskResultSet.getString(7));
				t.setSql_type(autoTaskResultSet.getString(8));
				t.setCrud_type(autoTaskResultSet.getString(9));
				t.setFields(autoTaskResultSet.getString(10));
				t.setCondition(autoTaskResultSet.getString(11));
				t.setSql_content(autoTaskResultSet.getString(12));
				autoTasks.add(t);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			while (spTaskResultSet.next()) {
				SpTask t = new SpTask();
				t.setId(spTaskResultSet.getInt(1));
				t.setProject_id(spTaskResultSet.getInt(2));
				t.setDb_name(spTaskResultSet.getString(3));
				t.setClass_name(spTaskResultSet.getString(4));
				t.setSp_schema(spTaskResultSet.getString(5));
				t.setSp_name(spTaskResultSet.getString(6));
				t.setSql_style(spTaskResultSet.getString(7));
				t.setCrud_type(spTaskResultSet.getString(8));
				t.setSp_content(spTaskResultSet.getString(9));
				spTasks.add(t);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			while (sqlTaskResultSet.next()) {
				SqlTask t = new SqlTask();
				t.setId(sqlTaskResultSet.getInt(1));
				t.setProject_id(sqlTaskResultSet.getInt(2));
				t.setDb_name(sqlTaskResultSet.getString(3));
				t.setClass_name(sqlTaskResultSet.getString(4));
				t.setMethod_name(sqlTaskResultSet.getString(5));
				t.setCrud_type(sqlTaskResultSet.getString(6));
				t.setSql_content(sqlTaskResultSet.getString(7));
				sqlTasks.add(t);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		allTasks.setAutoTasks(autoTasks);
		allTasks.setSpTasks(spTasks);
		allTasks.setSqlTasks(sqlTasks);

		return allTasks;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Status addTask(
			@FormParam("id") String id,
			@FormParam("project_id") String project_id,
			@FormParam("task_type") String task_type,
			@FormParam("db_name") String db_name,
			@FormParam("table_name") String table_name,
			@FormParam("class_name") String class_name,
			@FormParam("method_name") String method_name,
			@FormParam("sql_style") String sql_style, // C#风格或者Java风格
			@FormParam("sql_type") String sql_type, // SPA或SP3，或者是SQL
			@FormParam("sp_schema") String sp_schema,
			@FormParam("sp_name") String sp_name,
			@FormParam("crud_type") String crud_type,
			@FormParam("fields") String fields,
			@FormParam("condition") String condition,
			@FormParam("sql_content") String sql_content,
			@FormParam("action") String action) {

		if (task_type.equals("auto")) {
			if (action.equals("insert")) {
				AutoTask t = new AutoTask();
				t.setProject_id(Integer.valueOf(project_id));
				t.setDb_name(db_name);
				t.setTable_name(table_name);
				t.setClass_name(class_name);
				t.setMethod_name(method_name);
				t.setSql_style(sql_style);
				t.setSql_type(sql_type == null ? "sql" : sql_type);
				t.setCrud_type(crud_type);
				t.setFields(fields);
				t.setCondition(condition);
				t.setSql_content(formatSql(t));
				autoTask.insertTask(t);
			} else if (action.equals("update")) {
				AutoTask t = new AutoTask();
				t.setId(Integer.valueOf(id));
				t.setProject_id(Integer.valueOf(project_id));
				t.setDb_name(db_name);
				t.setTable_name(table_name);
				t.setClass_name(class_name);
				t.setMethod_name(method_name);
				t.setSql_style(sql_style);
				t.setSql_type(sql_type);
				t.setCrud_type(crud_type);
				t.setFields(fields);
				t.setCondition(condition);
				t.setSql_content(formatSql(t));
				autoTask.updateTask(t);
			} else if (action.equals("delete")) {
				AutoTask t = new AutoTask();
				t.setId(Integer.valueOf(id));
				autoTask.deleteTask(t);
			}
			return Status.OK;
		}

		if (task_type.equals("sp")) {
			// sp_schema = sp_name.split(".")[0];
			// sp_name = sp_name.split(".")[1];
			if(null != sp_name){
				String[] split_names = StringUtils.split(sp_name, ".");
				sp_schema = split_names[0];
				sp_name = split_names[1];
				class_name = sp_name;
			}
			if (action.equals("insert")) {
				SpTask t = new SpTask();
				t.setProject_id(Integer.valueOf(project_id));
				t.setDb_name(db_name);
				t.setClass_name(class_name);
				t.setSp_schema(sp_schema);
				t.setSp_name(sp_name);
				t.setSql_style(sql_style);
				t.setCrud_type(crud_type);
				t.setSp_content(formatSp(t));
				spTask.insertTask(t);
			} else if (action.equals("update")) {
				SpTask t = new SpTask();
				t.setId(Integer.valueOf(id));
				t.setProject_id(Integer.valueOf(project_id));
				t.setDb_name(db_name);
				t.setClass_name(class_name);
				t.setSp_schema(sp_schema);
				t.setSp_name(sp_name);
				t.setSql_style(sql_style);
				t.setCrud_type(crud_type);
				t.setSp_content(formatSp(t));
				spTask.updateTask(t);
			} else if (action.equals("delete")) {
				SpTask t = new SpTask();
				t.setId(Integer.valueOf(id));
				spTask.deleteTask(t);
			}
			return Status.OK;
		}

		if (task_type.equals("sql")) {
			if (action.equals("insert")) {
				SqlTask t = new SqlTask();
				t.setProject_id(Integer.valueOf(project_id));
				t.setDb_name(db_name);
				t.setClass_name(class_name);
				t.setMethod_name(method_name);
				t.setCrud_type(crud_type);
				t.setSql_content(sql_content);
				sqlTask.insertTask(t);
			} else if (action.equals("update")) {
				SqlTask t = new SqlTask();
				t.setId(Integer.valueOf(id));
				t.setProject_id(Integer.valueOf(project_id));
				t.setDb_name(db_name);
				t.setClass_name(class_name);
				t.setMethod_name(method_name);
				t.setCrud_type(crud_type);
				t.setSql_content(sql_content);
				sqlTask.updateTask(t);
			} else if (action.equals("delete")) {
				SqlTask t = new SqlTask();
				t.setId(Integer.valueOf(id));
				sqlTask.deleteTask(t);
			}
			return Status.OK;
		}

		return Status.ERROR;

	}

	private String formatSql(AutoTask task) {

		// 数据库中存储的模式： ID_0,Name_1 表示"ID = "以及"Name != "
		String[] conditions = task.getCondition().split(",");
		// 数据库中存储的模式： ID,Name
		String[] fields = task.getFields().split(",");

		if (null == task.getMethod_name() || task.getMethod_name().isEmpty()) {
			return "";
		}

		List<String> formatedConditions = new ArrayList<String>();
		// 将所有WHERE条件拼接，如ID_0,Name_1，for循环后将变为一个数组： [" ID = ", " Name != "]
		for (String con : conditions) {
			String[] keyValue = con.split("_");
			if (keyValue.length != 2) {
				continue;
			}
			// Between类型的操作符需要特殊处理
			if (keyValue[1].equals("6")) {
				if (task.getSql_style().equals("csharp")) {
					formatedConditions.add(String.format(
							" BETWEEN @%s_start AND @%s_end ", keyValue[0],
							keyValue[0]));
				} else {
					formatedConditions.add(" BETWEEN ? AND ? ");
				}
			} else {
				if (task.getSql_style().equals("csharp")) {
					formatedConditions.add(String.format(" %s %s @%s ",
							keyValue[0],
							Consts.WhereConditionMap.get(keyValue[1]),
							keyValue[0]));
				} else {
					formatedConditions.add(String.format(" %s %s ? ",
							keyValue[0],
							Consts.WhereConditionMap.get(keyValue[1])));
				}
			}
		}

		if (task.getCrud_type().equalsIgnoreCase("Select")) {
			if (formatedConditions.size() > 0) {
				return String
						.format("SELECT %s FROM %s WHERE %s", task.getFields(),
								task.getTable_name(), StringUtils.join(
										formatedConditions.toArray(), " AND "));
			} else {
				return String.format("SELECT %s FROM %s", task.getFields(),
						task.getTable_name());
			}
		} else if (task.getCrud_type().equalsIgnoreCase("Insert")) {

			if (task.getSql_type().equals("sql")) {
				List<String> placeHodler = new ArrayList<String>();
				for (String field : fields) {
					if (task.getSql_style().equals("csharp")) {
						placeHodler.add(String.format(" @%s ", field));
					} else {
						placeHodler.add(" ? ");
					}
				}
				return String.format("INSERT INTO %s (%s) VALUES (%s)",
						task.getTable_name(), task.getFields(),
						StringUtils.join(placeHodler.toArray(), ","));
			} else {
				return String.format("spa_%s_i", task.getTable_name());
			}

		} else if (task.getCrud_type().equalsIgnoreCase("Update")) {

			if (task.getSql_type().equals("sql")) {
				List<String> placeHodler = new ArrayList<String>();
				for (String field : fields) {
					if (task.getSql_style().equals("csharp")) {
						placeHodler.add(String.format(" %s = @%s ", field,
								field));
					} else {
						placeHodler.add(String.format(" %s = ? ", field));
					}
				}
				if (formatedConditions.size() > 0) {
					return String.format("UPDATE %s SET %s WHERE %s", task
							.getTable_name(), StringUtils.join(
							placeHodler.toArray(), ","), StringUtils.join(
							formatedConditions.toArray(), " AND "));
				} else {
					return String.format("UPDATE %s SET %s ",
							task.getTable_name(),
							StringUtils.join(placeHodler.toArray(), ","));
				}
			} else {
				return String.format("spa_%s_u", task.getTable_name());
			}

		} else if (task.getCrud_type().equalsIgnoreCase("Delete")) {
			if (task.getSql_type().equals("sql")) {
				if (formatedConditions.size() > 0) {
					return String.format("Delete FROM %s WHERE %s", task
							.getTable_name(), StringUtils.join(
							formatedConditions.toArray(), " AND "));
				} else {
					return String
							.format("Delete FROM %s", task.getTable_name());
				}
			} else {
				return String.format("spa_%s_d", task.getTable_name());
			}
		}

		return "";

	}
	
	private String formatSp(SpTask task){
		if(task.getCrud_type().equalsIgnoreCase("select")){
			return String.format(" Select * FROM EXEC %s.%s ", task.getSp_schema(), task.getSp_name());
		}else{
			return String.format(" EXEC %s.%s ", task.getSp_schema(), task.getSp_name());
		}
	}
	
}
