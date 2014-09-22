package com.ctrip.platform.dal.tester;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ctrip.platform.dal.dao.StatementParameter;
import com.ctrip.platform.dal.dao.client.proxy.AbstractDAO;


public class DalClient extends AbstractDAO {
	private List<StatementParameter> parameters = Collections.EMPTY_LIST;
	private Map keywordParameters = Collections.EMPTY_MAP;
	
	public DalClient() {
		logicDbName = "SysDalTest";
		servicePort = 9000;
		credentialId = "30303";
		super.init();
	}
	
	public DalClient(String host, String logicDbName, int port) {
		this.logicDbName = logicDbName;
		servicePort = port;
		credentialId = "30303";
		System.out.println(String.format("Host: %s Port: %s", host, port));
		super.init(host);
	}
	
	public DalClient(String logicDbName, int port) {
		this.logicDbName = logicDbName;
		servicePort = port;
		credentialId = "30303";
		super.init();
	}
	
	public void executeQuery(String sql) {
		// read result set
		ResultSet rs = this.fetch(sql, parameters, keywordParameters);
		try {
			while(rs.next()){
				rs.getString(1);
			}
			
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		
		try {
			rs.close();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

	}
	
	public void executeUpdate(String sql) {
		this.execute(sql, parameters, keywordParameters);
	}
}
