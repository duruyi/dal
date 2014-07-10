package com.ctrip.platform.dal.daogen.generator.processor.java;

import java.io.File;

import org.apache.velocity.VelocityContext;

import com.ctrip.platform.dal.daogen.CodeGenContext;
import com.ctrip.platform.dal.daogen.DalProcessor;
import com.ctrip.platform.dal.daogen.generator.java.JavaCodeGenContext;
import com.ctrip.platform.dal.daogen.utils.GenUtils;

public class JavaCodeGeneratorOfOthersProcessor implements DalProcessor {
	
	@Override
	public void process(CodeGenContext context) throws Exception {
		
		JavaCodeGenContext ctx = (JavaCodeGenContext)context;
		String generatePath = CodeGenContext.generatePath;
		int projectId = ctx.getProjectId();
		File dir = new File(String.format("%s/%s/java", generatePath, projectId));
		
		VelocityContext vltCcontext = GenUtils.buildDefaultVelocityContext();
		vltCcontext.put("host", ctx.getDalConfigHost());
		GenUtils.mergeVelocityContext(vltCcontext,
				String.format("%s/Dal.config", dir.getAbsolutePath()),
				"templates/java/DalConfig.java.tpl");
		
		vltCcontext.put("host", ctx.getContextHost());
		GenUtils.mergeVelocityContext(vltCcontext,
				String.format("%s/Context.xml", dir.getAbsolutePath()),
				"templates/java/DalContext.java.tpl");
		
	}


}
