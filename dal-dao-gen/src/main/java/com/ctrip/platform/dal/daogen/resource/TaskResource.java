package com.ctrip.platform.dal.daogen.resource;

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

import com.ctrip.platform.dal.daogen.Consts;
import com.ctrip.platform.dal.daogen.dao.AutoTaskDAO;
import com.ctrip.platform.dal.daogen.dao.ServerDbMapDAO;
import com.ctrip.platform.dal.daogen.dao.SpTaskDAO;
import com.ctrip.platform.dal.daogen.dao.SqlTaskDAO;
import com.ctrip.platform.dal.daogen.pojo.AutoTask;
import com.ctrip.platform.dal.daogen.pojo.ServerDbMap;
import com.ctrip.platform.dal.daogen.pojo.SpTask;
import com.ctrip.platform.dal.daogen.pojo.SqlTask;
import com.ctrip.platform.dal.daogen.pojo.Status;
import com.ctrip.platform.dal.daogen.pojo.TaskAggeragation;
import com.ctrip.platform.dal.daogen.utils.SpringBeanGetter;

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

	private static AutoTaskDAO autoTask;

	private static SpTaskDAO spTask;

	private static SqlTaskDAO sqlTask;
	
	private static ServerDbMapDAO serverDbMapDao;

	static {
		autoTask = SpringBeanGetter.getAutoTaskDao();
		spTask = SpringBeanGetter.getSpTaskDao();
		sqlTask = SpringBeanGetter.getSqlTaskDao();
		serverDbMapDao = SpringBeanGetter.getServerDbMapDao();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TaskAggeragation getTasks(@QueryParam("project_id") String id) {

		List<AutoTask> autoTasks = autoTask.getTasksByProjectId(Integer
				.valueOf(id));
		
		List<ServerDbMap> serverDbMaps = serverDbMapDao.getServerDbs();
		
		for(AutoTask task : autoTasks){
			for(ServerDbMap map : serverDbMaps){
				if(map.getDb_name().equalsIgnoreCase(task.getDb_name())){
					task.setServer_id(map.getServer_id());
					break;
				}
			}
		}

		List<SpTask> spTasks = spTask.getTasksByProjectId(Integer.valueOf(id));

		List<SqlTask> sqlTasks = sqlTask.getTasksByProjectId(Integer
				.valueOf(id));

		TaskAggeragation allTasks = new TaskAggeragation();

		allTasks.setAutoTasks(autoTasks);
		allTasks.setSpTasks(spTasks);
		allTasks.setSqlTasks(sqlTasks);

		return allTasks;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Status addTask(
			@FormParam("id") String id,
			@FormParam("server") int server,
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
		
		ServerDbMap map = serverDbMapDao.getServerByDbname(db_name);
		
		if(map == null){
			map = new ServerDbMap();
			map.setServer_id(server);
			map.setDb_name(db_name);
			serverDbMapDao.insertServerDbMap(map);
		}

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
			if (null != sp_name) {
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
				if(sqlTask.updateTask(t) > 0)
					return Status.OK;
				else
					return Status.ERROR;
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

	private String formatSp(SpTask task) {
		if (task.getCrud_type().equalsIgnoreCase("select")) {
			return String.format(" Select * FROM EXEC %s.%s ",
					task.getSp_schema(), task.getSp_name());
		} else {
			return String.format(" EXEC %s.%s ", task.getSp_schema(),
					task.getSp_name());
		}
	}

}
