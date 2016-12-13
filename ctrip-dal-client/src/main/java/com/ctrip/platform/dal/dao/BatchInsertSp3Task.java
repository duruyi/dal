package com.ctrip.platform.dal.dao;

import java.sql.SQLException;
import java.util.Map;

import com.ctrip.platform.dal.dao.task.AbstractIntArrayBulkTask;

public class BatchInsertSp3Task<T> extends AbstractIntArrayBulkTask<T> {
	private static final String INSERT_SP3_TPL = "sp3_%s_i";

	@Override
	public int[] execute(DalHints hints, Map<Integer, Map<String, ?>> daoPojos) throws SQLException {
		String insertSP3 = String.format(INSERT_SP3_TPL, getTableName(hints));

		String callSql = buildCallSql(insertSP3, parser.getColumnNames().length);
		StatementParameters[] parametersList = new StatementParameters[daoPojos.size()];
		
		int i = 0;
		for (Integer index :daoPojos.keySet()) {
			StatementParameters parameters = new StatementParameters();
			addParametersByName(parameters, daoPojos.get(index));
			parametersList[i++] = parameters;
		}
		
		int[] result = client.batchCall(callSql, parametersList, hints);
		return result; 
	}
}
