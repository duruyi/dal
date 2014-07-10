package com.ctrip.platform.dal.daogen.generator.processor.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ctrip.platform.dal.daogen.CodeGenContext;
import com.ctrip.platform.dal.daogen.DalProcessor;
import com.ctrip.platform.dal.daogen.dao.DaoBySqlBuilder;
import com.ctrip.platform.dal.daogen.dao.DaoByTableViewSp;
import com.ctrip.platform.dal.daogen.domain.StoredProcedure;
import com.ctrip.platform.dal.daogen.entity.ExecuteResult;
import com.ctrip.platform.dal.daogen.entity.GenTaskBySqlBuilder;
import com.ctrip.platform.dal.daogen.entity.GenTaskByTableViewSp;
import com.ctrip.platform.dal.daogen.entity.Progress;
import com.ctrip.platform.dal.daogen.enums.CurrentLanguage;
import com.ctrip.platform.dal.daogen.generator.java.JavaCodeGenContext;
import com.ctrip.platform.dal.daogen.host.AbstractParameterHost;
import com.ctrip.platform.dal.daogen.host.java.JavaParameterHost;
import com.ctrip.platform.dal.daogen.host.java.JavaTableHost;
import com.ctrip.platform.dal.daogen.host.java.SpDbHost;
import com.ctrip.platform.dal.daogen.host.java.SpHost;
import com.ctrip.platform.dal.daogen.host.java.ViewHost;
import com.ctrip.platform.dal.daogen.utils.DbUtils;
import com.ctrip.platform.dal.daogen.utils.SpringBeanGetter;
import com.ctrip.platform.dal.daogen.utils.TaskUtils;

public class JavaDataPreparerOfTableViewSpProcessor extends AbstractJavaDataPreparer implements DalProcessor {

	private static Logger log = Logger.getLogger(JavaDataPreparerOfTableViewSpProcessor.class);
	
	private static DaoBySqlBuilder daoBySqlBuilder;
	private static DaoByTableViewSp daoByTableViewSp;
	
	static {
		daoBySqlBuilder = SpringBeanGetter.getDaoBySqlBuilder();
		daoByTableViewSp = SpringBeanGetter.getDaoByTableViewSp();
	}
	
	@Override
	public void process(CodeGenContext context) throws Exception {
		List<Callable<ExecuteResult>> _tableViewSpCallables = prepareTableViewSp(context);
		TaskUtils.invokeBatch(log, _tableViewSpCallables);
	}

	private List<Callable<ExecuteResult>> prepareTableViewSp(CodeGenContext codeGenCtx) throws Exception {
		final JavaCodeGenContext ctx = (JavaCodeGenContext)codeGenCtx;
		int projectId = ctx.getProjectId();
		boolean regenerate = ctx.isRegenerate();
		final Progress progress = ctx.getProgress();
		List<GenTaskByTableViewSp> _tableViewSps;
		List<GenTaskBySqlBuilder> _tempSqlBuilders;
		if (regenerate) {
			_tableViewSps = daoByTableViewSp.updateAndGetAllTasks(projectId);
			_tempSqlBuilders = daoBySqlBuilder.updateAndGetAllTasks(projectId);
			prepareDbFromTableViewSp(ctx, _tableViewSps, _tempSqlBuilders);
		} else {
			_tableViewSps = daoByTableViewSp.updateAndGetTasks(projectId);
			_tempSqlBuilders = daoBySqlBuilder.updateAndGetTasks(projectId);
			prepareDbFromTableViewSp(ctx,
					daoByTableViewSp.getTasksByProjectId(projectId),
					daoBySqlBuilder.getTasksByProjectId(projectId));
		}
		Queue<GenTaskBySqlBuilder> _sqlBuilders = ctx.get_sqlBuilders();
		for (GenTaskBySqlBuilder _t : _tempSqlBuilders) {
			_sqlBuilders.add(_t);
		}

		final Queue<JavaTableHost> _tableHosts = ctx.get_tableHosts();
		final Queue<ViewHost> _viewHosts = ctx.get_viewHosts();
		final Queue<SpHost> _spHosts = ctx.get_spHosts();
		final Map<String, SpDbHost> _spHostMaps = ctx.get_spHostMaps();
		List<Callable<ExecuteResult>> results = new ArrayList<Callable<ExecuteResult>>();
		for (final GenTaskByTableViewSp tableViewSp : _tableViewSps) {
			final String[] viewNames = StringUtils.split(
					tableViewSp.getView_names(), ",");
			final String[] tableNames = StringUtils.split(
					tableViewSp.getTable_names(), ",");
			final String[] spNames = StringUtils.split(
					tableViewSp.getSp_names(), ",");

			for (final String table : tableNames) {
				Callable<ExecuteResult> worker = new Callable<ExecuteResult>() {
					@Override
					public ExecuteResult call() throws Exception {
						/*progress.setOtherMessage("正在为所有表/存储过程生成DAO准备数据.<br/>buildTable:"
								+ table);*/
						ExecuteResult result = new ExecuteResult("Build Table[" + tableViewSp.getDb_name() + "." + table + "] Host");
						progress.setOtherMessage(result.getTaskName());
						try{
							JavaTableHost tableHost = buildTableHost(ctx, tableViewSp, table);
							result.setSuccessal(true);
						if (null != tableHost)
							_tableHosts.add(tableHost);
						result.setSuccessal(true);
						} catch (Exception e) {
							log.error(result.getTaskName() + " exception.", e);
						}
						return result;
					}
				};
				results.add(worker);
			}

			for (final String view : viewNames) {
				Callable<ExecuteResult> viewWorker = new Callable<ExecuteResult>() {
					@Override
					public ExecuteResult call() throws Exception {
						/*progress.setOtherMessage("正在为所有表/存储过程生成DAO准备数据.<br/>buildView:"
								+ view);*/
						ExecuteResult result = new ExecuteResult("Build View[" + tableViewSp.getDb_name() + "." + view + "] Host");
						progress.setOtherMessage(result.getTaskName());
						try{
							ViewHost vhost = buildViewHost(ctx, tableViewSp, view);
							if (null != vhost)
								_viewHosts.add(vhost);
							result.setSuccessal(true);
						}catch(Exception e){
							log.error(result.getTaskName() + " exception.", e);
						}
						return result;
					}
				};
				results.add(viewWorker);
			}

			for (final String spName : spNames) {
				Callable<ExecuteResult> spWorker = new Callable<ExecuteResult>() {
					@Override
					public ExecuteResult call() throws Exception {
					/*	progress.setOtherMessage("正在为所有表/存储过程生成DAO准备数据.<br/>buildSp:"
								+ spName);*/
						ExecuteResult result = new ExecuteResult("Build SP[" + tableViewSp.getDb_name() + "." + spName + "] Host");
						progress.setOtherMessage(result.getTaskName());
						try{
							SpHost spHost = buildSpHost(ctx, tableViewSp, spName);
							if (null != spHost) {
								if (!_spHostMaps.containsKey(spHost.getDbName())) {
									SpDbHost spDbHost = new SpDbHost(
											spHost.getDbName(),
											spHost.getPackageName());
									_spHostMaps.put(spHost.getDbName(), spDbHost);
								}
								_spHostMaps.get(spHost.getDbName()).addSpHost(
										spHost);
								_spHosts.add(spHost);
							}
							result.setSuccessal(true);
						}catch(Exception e){
							log.error(result.getTaskName() + " exception.", e);
							progress.setOtherMessage(e.getMessage());
						}
						return result;
					}
				};
				results.add(spWorker);
			}
		}

		return results;
	}
	
	private void prepareDbFromTableViewSp(CodeGenContext codeGenCtx, 
			List<GenTaskByTableViewSp> tableViewSps, List<GenTaskBySqlBuilder> sqlBuilders) {
		for (GenTaskByTableViewSp task : tableViewSps) {
			addDatabaseSet(codeGenCtx, task.getDatabaseSetName());
		}
		for (GenTaskBySqlBuilder task : sqlBuilders) {
			addDatabaseSet(codeGenCtx, task.getDatabaseSetName());
		}
	}
	
	private ViewHost buildViewHost(CodeGenContext codeGenCtx, GenTaskByTableViewSp tableViewSp,
			String viewName) throws Exception {
		JavaCodeGenContext ctx = (JavaCodeGenContext)codeGenCtx;
		if (!DbUtils.viewExists(tableViewSp.getDb_name(), viewName)) {
			log.error(String.format(
					"The view[%s] doesn't exist, pls check", viewName));
			return null;
		}

		ViewHost vhost = new ViewHost();
		String className = viewName.replace("_", "");
		className = getPojoClassName(tableViewSp.getPrefix(),
				tableViewSp.getSuffix(), className);

		vhost.setPackageName(ctx.getNamespace());
		vhost.setDatabaseCategory(getDatabaseCategory(tableViewSp));
		vhost.setDbName(tableViewSp.getDatabaseSetName());
		vhost.setPojoClassName(className);
		vhost.setViewName(viewName);

		List<String> primaryKeyNames = DbUtils.getPrimaryKeyNames(
				tableViewSp.getDb_name(), viewName);
		List<AbstractParameterHost> params = DbUtils.getAllColumnNames(
				tableViewSp.getDb_name(), viewName, CurrentLanguage.Java);
		List<JavaParameterHost> realParams = new ArrayList<JavaParameterHost>();
		if(null == params){
			throw new Exception(String.format("The column names of view[%s, %s] is null", 
					tableViewSp.getDb_name(), viewName));
		}
		for (AbstractParameterHost p : params) {
			JavaParameterHost jHost = (JavaParameterHost) p;
			if (primaryKeyNames.contains(jHost.getName())) {
				jHost.setPrimary(true);
			}
			realParams.add(jHost);
		}

		vhost.setFields(realParams);
		return vhost;
	}
	
	private SpHost buildSpHost(CodeGenContext codeGenCtx, GenTaskByTableViewSp tableViewSp, String spName)
			throws Exception {
		JavaCodeGenContext ctx = (JavaCodeGenContext)codeGenCtx;
		String schema = "dbo";
		String realSpName = spName;
		if (spName.contains(".")) {
			String[] splitSp = StringUtils.split(spName, '.');
			schema = splitSp[0];
			realSpName = splitSp[1];
		}

		StoredProcedure currentSp = new StoredProcedure();
		currentSp.setSchema(schema);
		currentSp.setName(realSpName);

		if (!DbUtils.spExists(tableViewSp.getDb_name(), currentSp)) {
			throw new Exception(String.format("The store procedure[%s, %s] doesn't exist, pls check", 
					tableViewSp.getDb_name(),  currentSp.getName()));
		}
		
		SpHost spHost = new SpHost();
		String className = realSpName.replace("_", "");
		className = getPojoClassName(tableViewSp.getPrefix(),
				tableViewSp.getSuffix(), className);

		spHost.setPackageName(ctx.getNamespace());
		spHost.setDatabaseCategory(getDatabaseCategory(tableViewSp));
		spHost.setDbName(tableViewSp.getDatabaseSetName());
		spHost.setPojoClassName(className);
		spHost.setSpName(spName);
		List<AbstractParameterHost> params = DbUtils.getSpParams(
				tableViewSp.getDb_name(), currentSp, CurrentLanguage.Java);
		List<JavaParameterHost> realParams = new ArrayList<JavaParameterHost>();
		String callParams = "";
		if(null == params){
			throw new Exception(String.format("The sp[%s, %s] parameters is null", 
					tableViewSp.getDb_name(), currentSp.getName()));
		}
		for (AbstractParameterHost p : params) {
			callParams += "?,";
			realParams.add((JavaParameterHost) p);
		}
		spHost.setCallParameters(StringUtils.removeEnd(callParams, ","));
		spHost.setFields(realParams);

		return spHost;
	}
	

}
