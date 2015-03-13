package com.ctrip.platform.dal.daogen.host.java;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ctrip.platform.dal.daogen.Consts;
import com.ctrip.platform.dal.daogen.host.AbstractParameterHost;
import com.ctrip.platform.dal.daogen.utils.ResultSetExtractor;

public class JavaSelectFieldResultSetExtractor implements ResultSetExtractor<List<AbstractParameterHost>> {

	private static Logger log = Logger.getLogger(JavaSelectFieldResultSetExtractor.class);
	
	@Override
	public List<AbstractParameterHost> extract(ResultSet rs) throws SQLException {
		ResultSetMetaData rsMeta = rs.getMetaData();
		List<AbstractParameterHost> hosts = new ArrayList<AbstractParameterHost>();
		for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
			JavaParameterHost paramHost = new JavaParameterHost();
			paramHost.setName(rsMeta.getColumnLabel(i));
			paramHost.setSqlType(rsMeta.getColumnType(i));
			Class<?> javaClass = null;
			try {
				javaClass = Class.forName(rsMeta.getColumnClassName(i));
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
				javaClass = Consts.jdbcSqlTypeToJavaClass.get(paramHost.getSqlType());
			}
			paramHost.setJavaClass(javaClass);
			paramHost.setIdentity(false);
			paramHost.setNullable(false);
			paramHost.setPrimary(false);
			paramHost.setLength(rsMeta.getColumnDisplaySize(i));
			hosts.add(paramHost);
		}
		return hosts;
	}

}
