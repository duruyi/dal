package com.ctrip.platform.dal.daogen.generator.processor.csharp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.ctrip.platform.dal.common.enums.DbType;
import com.ctrip.platform.dal.daogen.CodeGenContext;
import com.ctrip.platform.dal.daogen.DalProcessor;
import com.ctrip.platform.dal.daogen.dao.DaoByFreeSql;
import com.ctrip.platform.dal.daogen.entity.ExecuteResult;
import com.ctrip.platform.dal.daogen.entity.GenTaskByFreeSql;
import com.ctrip.platform.dal.daogen.entity.Progress;
import com.ctrip.platform.dal.daogen.enums.CurrentLanguage;
import com.ctrip.platform.dal.daogen.generator.csharp.CSharpCodeGenContext;
import com.ctrip.platform.dal.daogen.host.AbstractParameterHost;
import com.ctrip.platform.dal.daogen.host.csharp.CSharpFreeSqlHost;
import com.ctrip.platform.dal.daogen.host.csharp.CSharpFreeSqlPojoHost;
import com.ctrip.platform.dal.daogen.host.csharp.CSharpMethodHost;
import com.ctrip.platform.dal.daogen.host.csharp.CSharpParameterHost;
import com.ctrip.platform.dal.daogen.host.csharp.DatabaseHost;
import com.ctrip.platform.dal.daogen.utils.CommonUtils;
import com.ctrip.platform.dal.daogen.utils.DbUtils;
import com.ctrip.platform.dal.daogen.utils.SpringBeanGetter;
import com.ctrip.platform.dal.daogen.utils.SqlBuilder;
import com.ctrip.platform.dal.daogen.utils.TaskUtils;

public class CSharpDataPreparerOfFreeSqlProcessor extends AbstractCSharpDataPreparer implements DalProcessor {

	private static Logger log = Logger.getLogger(CSharpDataPreparerOfFreeSqlProcessor.class);
	
	private static DaoByFreeSql daoByFreeSql;
	
	static {
		daoByFreeSql = SpringBeanGetter.getDaoByFreeSql();
	}
	
	@Override
	public void process(CodeGenContext context) throws Exception {
		
		List<Callable<ExecuteResult>> _freeSqlCallables;
		try {
			_freeSqlCallables = prepareFreeSql((CodeGenContext)context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		TaskUtils.invokeBatch(log, _freeSqlCallables);
		
	}
	
	private List<Callable<ExecuteResult>> prepareFreeSql(CodeGenContext codeGenCtx) throws Exception {
		final CSharpCodeGenContext ctx = (CSharpCodeGenContext)codeGenCtx;
		int projectId = ctx.getProjectId();
		boolean regenerate = ctx.isRegenerate();
		final Progress progress = ctx.getProgress();
		final String namespace = ctx.getNamespace();
		List<GenTaskByFreeSql> _freeSqls;
		if (regenerate) {
			_freeSqls = daoByFreeSql.updateAndGetAllTasks(projectId);
			prepareDbFromFreeSql(ctx, _freeSqls);
		} else {
			_freeSqls = daoByFreeSql.updateAndGetTasks(projectId);
			prepareDbFromFreeSql(ctx, daoByFreeSql.getTasksByProjectId(projectId));
		}

		// 首先按照DbName以及ClassName做一次GroupBy，且ClassName不区分大小写
		final Map<String, List<GenTaskByFreeSql>> groupBy = freeSqlGroupBy(_freeSqls);

		List<Callable<ExecuteResult>> results = new ArrayList<Callable<ExecuteResult>>();
		final Map<String, CSharpFreeSqlPojoHost> _freeSqlPojoHosts = ctx.getFreeSqlPojoHosts();
		final Queue<CSharpFreeSqlHost> _freeSqlHosts = ctx.getFreeSqlHosts();
		// 随后，以DbName以及ClassName为维度，为每个维度生成一个DAO类
		for (final Map.Entry<String, List<GenTaskByFreeSql>> entry : groupBy.entrySet()) {
			Callable<ExecuteResult> worker = new Callable<ExecuteResult>() {
				@Override
				public ExecuteResult call() throws Exception {
					ExecuteResult result  = new ExecuteResult("Build  Free SQL[" + entry.getKey() + "] Host");				
					progress.setOtherMessage(result.getTaskName());
					
					try{
						List<GenTaskByFreeSql> currentTasks = entry.getValue();
						if (currentTasks.size() < 1)
							return result;
	
						CSharpFreeSqlHost host = new CSharpFreeSqlHost();
						host.setDbSetName(currentTasks.get(0).getDatabaseSetName());
						host.setClassName(CommonUtils.normalizeVariable(WordUtils
								.capitalize(currentTasks.get(0).getClass_name())));
						host.setNameSpace(namespace);
						host.setDatabaseCategory(getDatabaseCategory(currentTasks.get(0).getAllInOneName()));
	
						List<CSharpMethodHost> methods = new ArrayList<CSharpMethodHost>();
						// 每个Method可能就有一个Pojo
						for (GenTaskByFreeSql task : currentTasks) {
							CSharpMethodHost method = buildFreeSqlMethodHost(ctx, task);
							if (!_freeSqlPojoHosts.containsKey(task.getPojo_name()) && 
									method.getPojoName()!=null && !method.getPojoName().isEmpty() &&
									(!method.isFirstOrSingle() || !method.isSampleType())) {
								CSharpFreeSqlPojoHost freeSqlPojoHost = buildFreeSqlPojoHost(ctx, task);
								if (null != freeSqlPojoHost) {
									_freeSqlPojoHosts.put(task.getPojo_name(), freeSqlPojoHost);
								}
							}
							methods.add(method);						
						}
						host.setMethods(methods);
						_freeSqlHosts.add(host);
						result.setSuccessal(true);
					}catch(Exception e){
						log.error(result.getTaskName() + "exception", e);
					}
					return result;
				}
			};
			results.add(worker);
		}

		return results;
	}
	
	private CSharpMethodHost buildFreeSqlMethodHost(CSharpCodeGenContext ctx, GenTaskByFreeSql task) throws Exception {
		CSharpMethodHost method = new CSharpMethodHost();
		List<String> inParams = new ArrayList<String>();
		Matcher m = CSharpCodeGenContext.inRegxPattern.matcher(task.getSql_content());
		String temp= task.getSql_content();
		int index = 0;
		if(task.isPagination()){
			temp = SqlBuilder.pagingQuerySql(temp, getDatabaseCategory(task.getAllInOneName()), CurrentLanguage.CSharp);
			index += 2;
		}
		while(m.find())
    	{
			String paramName = m.group(1);
			inParams.add(paramName.replaceAll("[\\(|\\)|@]", ""));
			temp = temp.replace(paramName, String.format("({%d}) ", index++));
    	}
		method.setSql(temp);
		method.setName(task.getMethod_name());
		method.setPaging(task.isPagination());
		String pojoName = task.getPojo_name();
		if(pojoName.equalsIgnoreCase("简单类型")){
			method.setPojoName(method.getName().substring(0,1).toUpperCase() + method.getName().substring(1));
		}else{
			method.setPojoName(CommonUtils.normalizeVariable(WordUtils
				.capitalize(task.getPojo_name())));
		}
		method.setScalarType(task.getScalarType());
		method.setPojoType(task.getPojoType());
		List<CSharpParameterHost> params = new ArrayList<CSharpParameterHost>();
		for (String param : StringUtils.split(task.getParameters(), ";")) {
			String[] splitedParam = StringUtils.split(param, ",");
			CSharpParameterHost p = new CSharpParameterHost();
			p.setName(splitedParam[0]);
			p.setInParameter(inParams.contains(p.getName()));
			p.setDbType(DbType.getDbTypeFromJdbcType(Integer.valueOf(splitedParam[1])));
			p.setType(DbType.getCSharpType(p.getDbType()));
			Object mockValue = DbUtils.mockATest(Integer.valueOf(splitedParam[1]));
			if (p.getType().equals("string") || p.getType().equals("DateTime")) {
				p.setValue("\"" + mockValue + "\"");
			} else {
				p.setValue(mockValue);
			}
			params.add(p);
		}
		method.setParameters(params);
		
		if(ctx.getFreeSqlPojoHosts().containsKey(method.getPojoName())){
			method.setPojohost(ctx.getFreeSqlPojoHosts().get(method.getPojoName()));
		}
		
		return method;
	}
	
	private void prepareDbFromFreeSql(CodeGenContext codeGenCtx, List<GenTaskByFreeSql> freeSqls) throws Exception {
		CSharpCodeGenContext ctx = (CSharpCodeGenContext)codeGenCtx;
		Map<String, DatabaseHost> _dbHosts = ctx.getDbHosts();
		Set<String> _freeDaos = ctx.getFreeDaos();
		for (GenTaskByFreeSql task : freeSqls) {
			addDatabaseSet(ctx, task.getDatabaseSetName());
			_freeDaos.add(WordUtils.capitalize(task.getClass_name()));
			if (!_dbHosts.containsKey(task.getAllInOneName())) {

				String provider = "sqlProvider";
				String dbType = DbUtils.getDbType(task.getAllInOneName());
				if (null != dbType && !dbType.equalsIgnoreCase("Microsoft SQL Server")) {
					provider = "mySqlProvider";
				}
				DatabaseHost host = new DatabaseHost();
				host.setAllInOneName(task.getAllInOneName());
				host.setProviderType(provider);
				host.setDatasetName(task.getDatabaseSetName());
				_dbHosts.put(task.getAllInOneName(), host);
			}
		}
	}
	
	private Map<String, List<GenTaskByFreeSql>> freeSqlGroupBy(List<GenTaskByFreeSql> tasks) {
		Map<String, List<GenTaskByFreeSql>> groupBy = new HashMap<String, List<GenTaskByFreeSql>>();

		for (GenTaskByFreeSql task : tasks) {
			String key = String.format("%s_%s", task.getAllInOneName(), task.getClass_name().toLowerCase());
			if (groupBy.containsKey(key)) {
				groupBy.get(key).add(task);
			} else {
				groupBy.put(key, new ArrayList<GenTaskByFreeSql>());
				groupBy.get(key).add(task);
			}
		}
		return groupBy;
	}
	
	private CSharpFreeSqlPojoHost buildFreeSqlPojoHost(CSharpCodeGenContext codeGenCtx, GenTaskByFreeSql task) throws Exception {

		CSharpFreeSqlPojoHost freeSqlHost = new CSharpFreeSqlPojoHost();

		List<CSharpParameterHost> pHosts = new ArrayList<CSharpParameterHost>();

		for (AbstractParameterHost _ahost : DbUtils.testAQuerySql(
				task.getAllInOneName(), task.getSql_content(), task.getParameters(),
				CurrentLanguage.CSharp, false)) {
			pHosts.add((CSharpParameterHost) _ahost);
		}

		freeSqlHost.setColumns(pHosts);
		freeSqlHost.setTableName("");
		String className = task.getPojo_name();
		if(className.equalsIgnoreCase("简单类型")){
			freeSqlHost.setClassName(task.getMethod_name().substring(0,1).toUpperCase() + task.getMethod_name().substring(1));
		}else{
			freeSqlHost.setClassName(CommonUtils.normalizeVariable(WordUtils
					.capitalize(task.getPojo_name())));
		}
		
		freeSqlHost.setNameSpace(codeGenCtx.getNamespace());

		return freeSqlHost;
	}
	
}
