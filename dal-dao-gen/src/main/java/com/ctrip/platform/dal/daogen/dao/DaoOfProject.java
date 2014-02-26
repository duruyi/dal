
package com.ctrip.platform.dal.daogen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.ctrip.platform.dal.daogen.pojo.Project;

public class DaoOfProject {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public List<Project> getAllProjects() {
		try {
			return this.jdbcTemplate.query(
					"select id, name, namespace from project",
					new RowMapper<Project>() {
						public Project mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return Project.visitRow(rs);
						}
					});
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public List<Project> getProjectByIDS(Object[] iD) {
		try {
			return this.jdbcTemplate
					.query(
							String.format("select id, name, namespace from project where id in (%s) ", StringUtils.join(iD, ","))
							, new RowMapper<Project>() {
								public Project mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									return Project.visitRow(rs);
								}
							});
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Project getProjectByID(int iD) {
		try {
			return this.jdbcTemplate
					.queryForObject(
							"select id, name, namespace from project where id=?",
							new Object[] { iD }, new RowMapper<Project>() {
								public Project mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									return Project.visitRow(rs);
								}
							});
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public int insertProject(final Project project) {
		
		KeyHolder holder = new GeneratedKeyHolder();

		this.jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(
					Connection connection) throws SQLException {
				PreparedStatement ps = connection
						.prepareStatement(
								"insert into project ( name, namespace) values (?,?)",
								Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, project.getName());
				ps.setString(2, project.getNamespace());
				return ps;
			}
		}, holder);

		return holder.getKey().intValue();
		
	}

	public int updateProject(Project project) {
		try {
			return this.jdbcTemplate
					.update("update project set name=?, namespace=? where id=?",
							project.getName(),
							project.getNamespace(), project.getId());
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return -1;
		}
	}

	public int deleteProject(Project project) {
		try {
			return this.jdbcTemplate.update("delete from project where id=?",
					project.getId());
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return -1;
		}
	}
}