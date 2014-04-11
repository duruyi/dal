
package com.ctrip.platform.dal.daogen.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jasig.cas.client.util.AssertionHolder;

import com.ctrip.platform.dal.daogen.entity.Progress;

/**
 *
 * @author gzxia
 * 
 */
@Resource
@Singleton
@Path("progress")
public class ProgressResource {
	
	/**
	 * key: userNo + # + project_id
	 * value:Progress
	 */
	public static Map<String,Progress> progresses = new ConcurrentHashMap<String,Progress>(); 
	
	private final static int MAX_PROGRESSES = 2000;
	
	private final static long MAX_ALIVE_TIME = 60*60*1000;//单个Progress最大生命时间，单位毫秒
	
	public final static String FINISH = "finish";
	
	public final static String ISDOING = "isDoing";
	
	public final static String INIT_MESSAGE = "正在初始化...";
	public final static String SUCCESS_MESSAGE = "生成完毕";

	@Path("/poll")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Progress poll(@QueryParam("project_id") int project_id,
			@QueryParam("regenerate") boolean regen,
			@QueryParam("language") String language,
			@QueryParam("random") String random) {
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String userNo = AssertionHolder.getAssertion().getPrincipal()
				.getAttributes().get("employee").toString();
		Progress progress = getProgress(userNo,project_id,random);
		if(FINISH.equals(progress.getStatus())){
			release(progress);
			Progress success = new Progress();
			success.setPercent(100);
			success.setStatus(FINISH);
			success.setOtherMessage(SUCCESS_MESSAGE);
			return success;
		}
		updatePercent(progress);
		return progress;
	}
	
	public static synchronized Progress getProgress(String userNo,int project_id,String random){
		String key = getKey(userNo,project_id,random);
		Progress pro = progresses.get(key);
		if(pro == null){
			pro = new Progress();
			pro.setUserNo(userNo);
			pro.setProject_id(project_id);
			pro.setOtherMessage(INIT_MESSAGE);
			pro.setRandom(random);
			progresses.put(key, pro);
		}
		return pro;
	}
	
	public static void addTotalFiles(Progress progress,int newTotalFiles){
		if(progress == null){
			return;
		}
		progress.setTotalFiles(progress.getTotalFiles()+newTotalFiles);
	}
	
	public static void addDoneFiles(Progress progress,int newDoneFiles){
		if(progress == null){
			return;
		}
		progress.setDoneFiles(progress.getDoneFiles()+newDoneFiles);
		updatePercent(progress);
	}
	
	private static String getKey(String userNo,int project_id,String random){
		return userNo+"#"+project_id+"#"+random;
	}
	
	private static void updatePercent(Progress progress){
		if(progress == null){
			return;
		}
		try {
			progress.setPercent((int) Math.floor(progress.getDoneFiles() * 100
					/ progress.getTotalFiles()));
			if(!FINISH.equals(progress.getStatus()) && progress.getPercent()>=100){
				progress.setPercent(85);
			}
		} catch (Exception e) {
			progress.setPercent(0);
		}
	}
	
	private static void release(Progress progress){
		String key = getKey(progress.getUserNo(),progress.getProject_id(),progress.getRandom());
		progress = null;
		progresses.remove(key);
		if(progresses.size()>MAX_PROGRESSES){
			cleanProgresses();
		}
	}
	
	private static synchronized void cleanProgresses(){
		for(Map.Entry<String,Progress> entry : progresses.entrySet()){
			String key = entry.getKey();
			Progress pro = entry.getValue();
			if(System.currentTimeMillis() - pro.getTime() > MAX_ALIVE_TIME){
				progresses.remove(key);
			}
		}
	}
	
}













