package com.ctrip.platform.dal.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KeyHolder {
	private final List<Map<String, Object>> keyList = new LinkedList<Map<String, Object>>();;

	/**
	 * Get the generated Id. The type is one of the Number.
	 * @return null if no key found, or the id in number
	 * @throws SQLException if there is more than one generated key or the conversion is failed.
	 */
	public Number getKey() throws SQLException {
		if (this.keyList.size() == 0) {
			return null;
		}
		if (this.keyList.size() > 1 || this.keyList.get(0).size() > 1) {
			throw new SQLException(
					"The getKey method should only be used when a single key is returned.  " +
					"The current key entry contains multiple keys: " + this.keyList);
		}
		Iterator<Object> keyIter = this.keyList.get(0).values().iterator();
		if (keyIter.hasNext()) {
			Object key = keyIter.next();
			if (!(key instanceof Number)) {
				throw new SQLException(
						"The generated key is not of a supported numeric type. " +
						"Unable to cast [" + (key != null ? key.getClass().getName() : null) +
						"] to [" + Number.class.getName() + "]");
			}
			return (Number) key;
		}
		else {
			throw new SQLException("Unable to retrieve the generated key. " +
					"Check that the table has an identity column enabled.");
		}
	}

	/**
	 * Get the first generated key in map.
	 * @return null if no key found, or the keys in a map
	 * @throws SQLException
	 */
	public Map<String, Object> getKeys() throws SQLException {
		if (this.keyList.size() == 0) {
			return null;
		}
		if (this.keyList.size() > 1)
			throw new SQLException(
					"The getKeys method should only be used when keys for a single row are returned.  " +
					"The current key list contains keys for multiple rows: " + this.keyList);
		return this.keyList.get(0);
	}

	/**
	 * Get all the generated keys for multiple insert.
	 * @return all the generated keys
	 */
	public List<Map<String, Object>> getKeyList() {
		return this.keyList;
	}
	
	/**
	 * Convert generated keys to list of number. 
	 * @return
	 * @throws SQLException if the conversion fails
	 */
	public List<Number> getIdList() throws SQLException {
		List<Number> idList = new ArrayList<Number>();
		
		if (this.keyList.size() == 0) {
			return null;
		}
		
		try {
			for(Map<String, Object> key: keyList) {
				idList.add((Number)key.values().iterator().next());
			}
			return idList;
		} catch (Throwable e) {
			throw new SQLException("Can not convert generated keys to list of number.", e);
		}
	}
}
