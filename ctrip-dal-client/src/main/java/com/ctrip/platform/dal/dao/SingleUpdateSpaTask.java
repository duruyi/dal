package com.ctrip.platform.dal.dao;

import java.sql.SQLException;
import java.util.Map;

public class SingleUpdateSpaTask<T> extends CtripSpaTask<T> {
	private static final String UPDATE_SPA_TPL = "spA_%s_u";
	
	@Override
	public int execute(DalHints hints, Map<String, ?> fields) throws SQLException {
		if (null == fields) return 0;
		
		hints = DalHints.createIfAbsent(hints);

		String updateSPA = String.format(UPDATE_SPA_TPL, getTableName(hints, fields));
		
		StatementParameters parameters = new StatementParameters();
		String callSql = prepareSpCall(updateSPA, parameters, fields);

		Map<String, ?> results = client.call(callSql, parameters, hints);

		return (Integer) results.get(RET_CODE);
	}
}
