package com.ctrip.platform.dal.daogen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.ctrip.platform.dal.daogen.entity.LoginUser;

public class DaoOfLoginUser {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public List<LoginUser> getAllUsers() {

		return this.jdbcTemplate.query(
				"select id, user_no, user_name, user_email,dal_group_id from login_users",
				new RowMapper<LoginUser>() {
					public LoginUser mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return LoginUser.visitRow(rs);
					}
				});
	}
	
	public LoginUser getUserById(int userId) {
		try {
			return this.jdbcTemplate
					.queryForObject(
							"select id, user_no, user_name, user_email,dal_group_id from login_users where id = ?",
							new Object[] { userId },
							new RowMapper<LoginUser>() {
								public LoginUser mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									return LoginUser.visitRow(rs);
								}
							});
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public LoginUser getUserByNo(String userNo) {

		try {
			return this.jdbcTemplate
					.queryForObject(
							"select id, user_no, user_name, user_email,dal_group_id from login_users where user_no = ?",
							new Object[] { userNo },
							new RowMapper<LoginUser>() {
								public LoginUser mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									return LoginUser.visitRow(rs);
								}
							});
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public List<LoginUser> getUserByGroupId(int groupId) {

		try {
			return this.jdbcTemplate
					.query(
							"select id, user_no, user_name, user_email,dal_group_id from login_users where dal_group_id = ?",
							new Object[] { groupId },
							new RowMapper<LoginUser>() {
								public LoginUser mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									return LoginUser.visitRow(rs);
								}
							});
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public int insertUser(final LoginUser data) {

		KeyHolder holder = new GeneratedKeyHolder();

		this.jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(
					Connection connection) throws SQLException {
				PreparedStatement ps = connection
						.prepareStatement(
								"insert into login_users ( user_no, user_name, user_email,dal_group_id ) values (?,?,?,?) ON DUPLICATE KEY UPDATE user_no = ?",
								Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, data.getUserNo());
				ps.setString(2, data.getUserName());
				ps.setString(3, data.getUserEmail());
				ps.setInt(4, data.getGroupId());
				ps.setString(5, data.getUserNo());
				return ps;
			}
		}, holder);

		return holder.getKey().intValue();

	}
	
	public int updateUserGroup(int userId,Integer groupId){
		return this.jdbcTemplate
				.update("update login_users set dal_group_id=?"
						+ " where id=?",
						groupId,
						userId);
	}

}
