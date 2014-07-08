
package com.ctrip.platform.dal.daogen;

import java.util.Map;

import com.ctrip.platform.dal.daogen.entity.Progress;

public interface Generator {
	
	public boolean prepareDirectory(int projectId, boolean regenerate) throws Exception;
	
	public boolean prepareData(int projectId, boolean regenerate, Progress progress) throws Exception;

	public boolean generateCode(int projectId, Progress progress, Map<String,?> hints);

	public boolean clearResource();

}

