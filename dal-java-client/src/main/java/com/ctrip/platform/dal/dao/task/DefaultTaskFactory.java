package com.ctrip.platform.dal.dao.task;

import java.util.Map;

import com.ctrip.platform.dal.dao.DalParser;

public class DefaultTaskFactory implements DalTaskFactory {

	@Override
	public void initialize(Map<String, ?> settings) {
		//Do noting for now
	}

	@Override
	public <T> SingleTask<T> createSingleInsertTask(DalParser<T> parser) {
		SingleInsertTask<T> singleInsertTask = new SingleInsertTask<T>();
		singleInsertTask.initialize(parser);
		return singleInsertTask;
	}

	@Override
	public <T> SingleTask<T> createSingleDeleteTask(DalParser<T> parser) {
		SingleDeleteTask<T> singleDeleteTask = new SingleDeleteTask<T>();
		singleDeleteTask.initialize(parser);
		return singleDeleteTask;
	}

	@Override
	public <T> SingleTask<T> createSingleUpdateTask(DalParser<T> parser) {
		SingleUpdateTask<T> singleUpdateTask = new SingleUpdateTask<T>();
		singleUpdateTask.initialize(parser);
		return singleUpdateTask;
	}

	@Override
	public <T> BulkTask<Integer, T> createCombinedInsertTask(DalParser<T> parser) {
		CombinedInsertTask<T> combinedInsertTask = new CombinedInsertTask<T>();
		combinedInsertTask.initialize(parser);
		return combinedInsertTask;
	}

	@Override
	public <T> BulkTask<int[], T> createBatchInsertTask(DalParser<T> parser) {
		BatchInsertTask<T> batchInsertTask = new BatchInsertTask<T>();
		batchInsertTask.initialize(parser);
		return batchInsertTask;
	}

	@Override
	public <T> BulkTask<int[], T> createBatchDeleteTask(DalParser<T> parser) {
		BatchDeleteTask<T> batchDeleteTask = new BatchDeleteTask<T>();
		batchDeleteTask.initialize(parser);
		return batchDeleteTask;
	}

	@Override
	public <T> BulkTask<int[], T> createBatchUpdateTask(DalParser<T> parser) {
		BatchUpdateTask<T> batchUpdateTask = new BatchUpdateTask<T>();
		batchUpdateTask.initialize(parser);
		return batchUpdateTask;
	}
}
