package com.ctrip.platform.dal.sql.logging;

import java.util.HashMap;
import java.util.Map;

public class SQLInfo {
	public static final String COUNT = "arch.dal.sql.count";
	public static final String COST = "arch.dal.sql.cost";
	public static final String DAL_COST = "fx.dal.request.cost";
	
	public static final String CLIENT = "Client";
	private String version;

	private static final String DAO = "DAO";
	private String dao;
	
	private static final String METHOD = "Method";
	private String method;
	
	private static final String SIZE = "Size";
	private Integer size;
	
	private static final String STATUS = "Status";
	private String status;

	private static final String DB = "DB";
	private String database;

	private static final String TABLES = "Tables";
	private String tables;

	private static final String OPTTYPE = "OperationType";
	private String operationType;

	private static final String CLUSTER = "Cluster";
	private String cluster;

	private static final String SHARD = "Shard";
	private String shard;

	private static final String CLIENT_ZONE = "ClientZone";
	private String clientZone;

	private static final String DB_ZONE = "DBZone";
	private String dbZone;

	private static final String UCS_VALIDATION = "UcsValidation";
	private String ucsValidation;

	private static final String DAL_VALIDATION = "DalValidation";
	private String dalValidation;

	public SQLInfo(String dao, String version, String method, int size, String status, String database, String tables, String optType) {
		this(dao, version, method, size, status, database, tables, optType,
				null, null, null, null, null, null);
	}

	public SQLInfo(String dao, String version, String method, String status, String database, String tables, String optType) {
		this.dao = dao;
		this.method = method;
		this.version = "Java " + version;
		this.status = status;
		this.database = database;
		this.tables = tables;
		this.operationType = optType;
	}

	public SQLInfo(String dao, String version, String method, int size,
				   String status, String database, String tables, String optType,
				   String cluster, String shard, String clientZone, String dbZone,
				   String ucsValidation, String dalValidation) {
		this.dao = dao;
		this.method = method;
		this.version = "Java " + version;
		if (size < 200) {
			this.size = 200;
		} else if (size < 1000) {
			this.size = 1000;
		} else if (size < 5000) {
			this.size = 5000;
		} else {
			this.size = 99999;
		}
		this.status = status;
		this.database = database;
		this.tables = tables;
		this.operationType = optType;
		this.cluster = cluster;
		this.shard = shard;
		this.clientZone = clientZone;
		this.dbZone = dbZone;
		this.ucsValidation = ucsValidation;
		this.dalValidation = dalValidation;
	}
	
	public String getDao() {
		return dao;
	}
	public void setDao(String dao) {
		this.dao = dao;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Map<String, String> toTag(){
		Map<String, String> tags = new HashMap<>();
		tags.put(DAO, this.dao);
		tags.put(METHOD, this.method);
		if (this.size != null) {
			tags.put(SIZE, this.size.toString());
		}
		tags.put(STATUS, this.status);
		tags.put(CLIENT, this.version);
		tags.put(DB,this.database);
		tags.put(TABLES, this.tables);
		tags.put(OPTTYPE,this.operationType);
		if (this.cluster != null) {
			tags.put(CLUSTER, this.cluster);
		}
		if (this.shard != null) {
			tags.put(SHARD, this.shard);
		}
		if (this.clientZone != null) {
			tags.put(CLIENT_ZONE, this.clientZone);
		}
		if (this.dbZone != null) {
			tags.put(DB_ZONE, this.dbZone);
		}
		if (this.ucsValidation != null) {
			tags.put(UCS_VALIDATION, this.ucsValidation);
		}
		if (this.dalValidation != null) {
			tags.put(DAL_VALIDATION, this.dalValidation);
		}
		return tags;
	}
}
