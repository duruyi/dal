package com.ctrip.platform.dal.daogen.resource;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;

import com.ctrip.platform.dal.daogen.dao.DalGroupDao;
import com.ctrip.platform.dal.daogen.dao.DaoOfLoginUser;
import com.ctrip.platform.dal.daogen.dao.UserGroupDao;
import com.ctrip.platform.dal.daogen.domain.Status;
import com.ctrip.platform.dal.daogen.entity.DalGroup;
import com.ctrip.platform.dal.daogen.entity.DalGroupDB;
import com.ctrip.platform.dal.daogen.entity.DatabaseSet;
import com.ctrip.platform.dal.daogen.entity.LoginUser;
import com.ctrip.platform.dal.daogen.entity.Project;
import com.ctrip.platform.dal.daogen.entity.UserGroup;
import com.ctrip.platform.dal.daogen.utils.SpringBeanGetter;

@Resource
@Singleton
@Path("group")
public class DalGroupResource {
	private static Logger log = Logger.getLogger(DalGroupResource.class);
	public static final int SUPER_GROUP_ID = 1; // The default supper user group

	@GET
	@Path("get")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DalGroup> getAllGroup() {
		List<DalGroup> groups = SpringBeanGetter.getDaoOfDalGroup()
				.getAllGroups();
		return groups;
	}

	@GET
	@Path("onegroup")
	@Produces(MediaType.APPLICATION_JSON)
	public DalGroup getProject(@QueryParam("id") String id) {
		return SpringBeanGetter.getDaoOfDalGroup().getDalGroupById(
				Integer.valueOf(id));
	}

	@POST
	@Path("keepSession")
	@Produces(MediaType.APPLICATION_JSON)
	public String keepSession(@FormParam("id") String id) {
		return "true";
	}

	@POST
	@Path("add")
	public Status add(@FormParam("groupName") String groupName,
			@FormParam("groupComment") String groupComment) {

		String userNo = AssertionHolder.getAssertion().getPrincipal()
				.getAttributes().get("employee").toString();

		if (null == userNo || null == groupName || groupName.isEmpty()) {
			log.error(String.format(
					"Add dal group failed, caused by illegal parameters: "
							+ "[groupName=%s, groupComment=%s]", groupName,
					groupComment));
			Status status = Status.ERROR;
			status.setInfo("Illegal parameters.");
			return status;
		}

		if (!this.validate(userNo)) {
			Status status = Status.ERROR;
			status.setInfo("你没有当前DAL Team的操作权限.");
			return status;
		}

		DalGroup group = new DalGroup();
		group.setGroup_name(groupName);
		group.setGroup_comment(groupComment);
		group.setCreate_user_no(userNo);
		group.setCreate_time(new Timestamp(System.currentTimeMillis()));

		int ret = SpringBeanGetter.getDaoOfDalGroup().insertDalGroup(group);
		if (ret <= 0) {
			log.error("Add dal group failed, caused by db operation failed, pls check the spring log");
			Status status = Status.ERROR;
			status.setInfo("Add operation failed.");
			return status;
		}
		return Status.OK;
	}

	@POST
	@Path("delete")
	public Status delete(@FormParam("id") String id) {

		String userNo = AssertionHolder.getAssertion().getPrincipal()
				.getAttributes().get("employee").toString();

		if (null == userNo || null == id || id.isEmpty()) {
			log.error(String.format(
					"Delete dal group failed, caused by illegal parameters "
							+ "[ids=%s]", id));
			Status status = Status.ERROR;
			status.setInfo("Illegal parameters.");
			return status;
		}

		if (!this.validate(userNo)) {
			Status status = Status.ERROR;
			status.setInfo("你没有当前DAL Team的操作权限.");
			return status;
		}
		int groupId = -1;
		try {
			groupId = Integer.parseInt(id);
		} catch (NumberFormatException ex) {
			log.error("Delete dal group failed", ex);
			Status status = Status.ERROR;
			status.setInfo("Illegal group id");
			return status;
		}

		List<Project> prjs = SpringBeanGetter.getDaoOfProject()
				.getProjectByGroupId(groupId);
		if (prjs != null && prjs.size() > 0) {
			Status status = Status.ERROR;
			status.setInfo("当前DAL Team中还有Project，请清空Project后再操作！");
			return status;
		}
		List<DalGroupDB> dbs = SpringBeanGetter.getDaoOfDalGroupDB()
				.getGroupDBsByGroup(groupId);
		if (dbs != null && dbs.size() > 0) {
			Status status = Status.ERROR;
			status.setInfo("当前DAL Team中还有DataBase，请清空DataBase后再操作！");
			return status;
		}
		List<DatabaseSet> dbsets = SpringBeanGetter.getDaoOfDatabaseSet()
				.getAllDatabaseSetByGroupId(groupId);
		if (dbsets != null && dbsets.size() > 0) {
			Status status = Status.ERROR;
			status.setInfo("当前DAL Team中还有DataBaseSet，请清空DataBaseSet后再操作！");
			return status;
		}
		List<LoginUser> us = SpringBeanGetter.getDaoOfLoginUser()
				.getUserByGroupId(groupId);
		if (us != null && us.size() > 0) {
			Status status = Status.ERROR;
			status.setInfo("当前DAL Team中还有Member，请清空Member后再操作！");
			return status;
		}

		int ret = SpringBeanGetter.getDaoOfDalGroup().deleteDalGroup(groupId);
		if (ret <= 0) {
			log.error("Delete dal group failed, caused by db operation failed, pls check the spring log");
			Status status = Status.ERROR;
			status.setInfo("Delete operation failed.");
			return status;
		}
		return Status.OK;
	}

	@POST
	@Path("update")
	public Status update(@FormParam("groupId") String id,
			@FormParam("groupName") String groupName,
			@FormParam("groupComment") String groupComment) {

		String userNo = AssertionHolder.getAssertion().getPrincipal()
				.getAttributes().get("employee").toString();

		if (null == userNo || null == id || id.isEmpty()) {
			log.error(String.format(
					"Update dal group failed, caused by illegal parameters, "
							+ "[id=%s, groupName=%s, groupComment=%s]", id,
					groupName, groupComment));
			Status status = Status.ERROR;
			status.setInfo("Illegal parameters.");
			return status;
		}

		if (!this.validate(userNo)) {
			Status status = Status.ERROR;
			status.setInfo("你没有当前DAL Team的操作权限.");
			return status;
		}

		int groupId = -1;
		try {
			groupId = Integer.parseInt(id);
		} catch (NumberFormatException ex) {
			log.error("Update dal group failed", ex);
			Status status = Status.ERROR;
			status.setInfo("Illegal group id");
			return status;
		}

		DalGroup group = SpringBeanGetter.getDaoOfDalGroup().getDalGroupById(
				groupId);
		if (null == group) {
			log.error("Update dal group failed, caused by group_id specifed not existed.");
			Status status = Status.ERROR;
			status.setInfo("Group id not existed");
			return status;
		}
		if (null != groupName && !groupName.trim().isEmpty()) {
			group.setGroup_name(groupName);
		}
		if (null != groupComment && !groupComment.trim().isEmpty()) {
			group.setGroup_comment(groupComment);
		}

		group.setCreate_time(new Timestamp(System.currentTimeMillis()));

		int ret = SpringBeanGetter.getDaoOfDalGroup().updateDalGroup(group);

		if (ret <= 0) {
			log.error("Delete dal group failed, caused by db operation failed, pls check the spring log");
			Status status = Status.ERROR;
			status.setInfo("update operation failed.");
			return status;
		}
		return Status.OK;
	}

	@GET
	@Path("isSuperUser")
	@Produces(MediaType.APPLICATION_JSON)
	public Status isSuperUser() {
		Status status = Status.OK;
		String userNo = AssertionHolder.getAssertion().getPrincipal()
				.getAttributes().get("employee").toString();
		Boolean flag = validate(userNo);
		status.setInfo(flag.toString());
		return status;
	}

	private boolean validate(String userNo) {
		LoginUser user = SpringBeanGetter.getDaoOfLoginUser().getUserByNo(
				userNo);
		List<UserGroup> urGroups = SpringBeanGetter.getDalUserGroupDao()
				.getUserGroupByUserId(user.getId());
		if (urGroups != null && urGroups.size() > 0) {
			for (UserGroup urGroup : urGroups) {
				if (urGroup.getGroup_id() == SUPER_GROUP_ID) {
					return true;
				}
			}
		}
		return false;
	}
}
