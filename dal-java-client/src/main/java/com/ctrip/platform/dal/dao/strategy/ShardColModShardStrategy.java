package com.ctrip.platform.dal.dao.strategy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ctrip.platform.dal.common.enums.ParameterDirection;
import com.ctrip.platform.dal.dao.DalHintEnum;
import com.ctrip.platform.dal.dao.DalHints;
import com.ctrip.platform.dal.dao.StatementParameter;
import com.ctrip.platform.dal.dao.StatementParameters;
import com.ctrip.platform.dal.dao.configure.DalConfigure;

public class ShardColModShardStrategy extends AbstractRWSeparationStrategy implements DalShardStrategy {
	public static final String COLUMNS = "columns";
	public static final String MOD = "mod";
	
	private String[] columns;
	private Integer mod;
	/**
	 * columns are separated by ','
	 * @Override
	 */
	public void initialize(Map<String, String> settings) {
		columns = settings.get(COLUMNS).split(",");
		mod = Integer.parseInt(settings.get(MOD));
	}

	@Override
	public String locateShard(DalConfigure configure, String logicDbName,
			DalHints hints) {
		if(columns.length == 0)
			return null;
		
		String shard = locateByParameters(hints);
		if(shard != null)
			return shard;
		
		shard = locateByShardCol(hints);
		if(shard != null)
			return shard;
		
		if(hints.is(DalHintEnum.shardValue)) {
			Integer id = (Integer)hints.get(DalHintEnum.shardValue);
			return String.valueOf(id%mod);
		}
		
		throw new RuntimeException("Can not locate shard for " + logicDbName);
	}
	
	private String locateByParameters(DalHints hints) {
		StatementParameters parameters = (StatementParameters)hints.get(DalHintEnum.parameters);
		if(parameters != null) {
			for(String column: columns) {
				StatementParameter param = parameters.get(column, ParameterDirection.Input);
				if(param != null && param.getValue() != null) {
					Integer id = (Integer)param.getValue();
					return String.valueOf(id%mod);
				}
			}
		}
		return null;
	}
	
	private String locateByShardCol(DalHints hints) {
		Map<String, Integer> shardColValues = (Map<String, Integer>)hints.get(DalHintEnum.shardColValues);
		
		if(shardColValues != null) {
			for(String column: columns) {
				Integer id = shardColValues.get(column);
				if(id != null) {
					return String.valueOf(id%mod);
				}
			}
		}
		return null;
	}
}
