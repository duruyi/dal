package com.ctrip.platform.dal.dao;

public enum DalHintEnum {
	/*  */
	timeout,
	
	/*  */
	fetchSize,
	
	skipResultsProcessing,
	
//	skipUndeclaredResults,
	
	/*  */
	maxRows,
	
	maxFieldSize,

	/*  */
	shardCol, 

	/*  */
	masterOnly, 

	/*  */
	startRow,
	
	/*  */
	rowCount,
	
	/*  */
	columns,
	// TODO do we need separate operation type like Dal Fx?
	
	/* for logging */
	callingUrl,
	
	/* For insert, delete, update multiple pojos */ 
	usingBatch,
	
	/* For insert, delete, update multiple pojos */ 
	stopOnError,
	
	/* Allow customization */
	userDefined,
}
