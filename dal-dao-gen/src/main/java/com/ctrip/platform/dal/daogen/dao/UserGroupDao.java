package com.ctrip.platform.dal.daogen.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ctrip.platform.dal.daogen.entity.UserGroup;

public class UserGroupDao {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public List<UserGroup> getAllUserGroup() {
		return this.jdbcTemplate.query(
				"select id, user_id, group_id, role, adduser from user_group",
				new RowMapper<UserGroup>() {
					public UserGroup mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return UserGroup.visitRow(rs);
					}
				});
	}
	
	public List<UserGroup> getUserGroupById(Integer id){
		return this.jdbcTemplate.query(
				"select id, user_id, group_id, role, adduser from user_group where id = ?",
				new Object[]{id},
				new RowMapper<UserGroup>() {
					public UserGroup mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return UserGroup.visitRow(rs);
					}
				});
	}
	
	public List<UserGroup> getUserGroupByUserId(Integer userId){
		return this.jdbcTemplate.query(
				"select id, user_id, group_id, role, adduser from user_group where user_id = ?",
				new Object[]{userId},
				new RowMapper<UserGroup>() {
					public UserGroup mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return UserGroup.visitRow(rs);
					}
				});
	}
	
	public List<UserGroup> getUserGroupByGroupId(Integer groupId){
		return this.jdbcTemplate.query(
				"select id, user_id, group_id, role, adduser from user_group where group_id = ?",
				new Object[]{groupId},
				new RowMapper<UserGroup>() {
					public UserGroup mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return UserGroup.visitRow(rs);
					}
				});
	}
	
	public List<UserGroup> getUserGroupByGroupIdAndUserId(Integer groupId, Integer userId){
		return this.jdbcTemplate.query(
				"select id, user_id, group_id, role, adduser from user_group where group_id = ? and user_id=?",
				new Object[]{groupId, userId},
				new RowMapper<UserGroup>() {
					public UserGroup mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return UserGroup.visitRow(rs);
					}
				});
	}
	
	public int insertUserGroup(UserGroup ug) {
		return this.jdbcTemplate.update(
				"insert into user_group(id, user_id, group_id, role, adduser) value(?,?,?,?,?)",
				ug.getId(), 
				ug.getUser_id(), 
				ug.getGroup_id(),
				ug.getRole(),
				ug.getAdduser());
	}
	
	public int insertUserGroup(Integer user_id, Integer group_id, Integer role, Integer adduser) {
		return this.jdbcTemplate.update(
				"insert into user_group(user_id, group_id, role, adduser) value(?,?,?,?)",
				user_id, 
				group_id,
				role,
				adduser);
	}
	
	public int deleteUserFromGroup(Integer user_id, Integer group_id){
		try {
			return this.jdbcTemplate.update("delete from user_group where user_id=? and group_id=?",
					user_id,
					group_id);
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	
	public int deleteUserFromGroup(Integer ugId){
		try {
			return this.jdbcTemplate.update("delete from user_group where id=?", ugId);
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	
	public int updateUserPersimion(Integer userId, Integer groupId, Integer role, Integer adduser) {
		try {
			return this.jdbcTemplate.update("update user_group set role=?, adduser=?  where user_id=? and group_id=?", 
					role,
					adduser,
					userId,
					groupId);
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return -1;
		}
	}

}
