package com.ctrip.platform.dal.dao.sqlbuilder;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.ctrip.platform.dal.common.enums.DatabaseCategory;

public class UpdateSqlBuilderTest {
	
	@Test
	public void test1() throws SQLException {
		List<String> in = new ArrayList<String>();
		in.add("12");
		in.add("12");
		
		UpdateSqlBuilder builder = new UpdateSqlBuilder("Person", DatabaseCategory.MySql);
		
		builder.update("name", "value", Types.VARCHAR);
		builder.update("age", 52, Types.INTEGER);
		builder.update("addr", "china", Types.VARCHAR);
		
		builder.equal("age", 123, Types.INTEGER);
		builder.and().in("b", in, Types.INTEGER);
		builder.and().like("b", "in", Types.INTEGER);
		builder.and().betweenNullable("c", 20, 100, Types.INTEGER);
		builder.and().betweenNullable("d", null, "paramValue2", Types.VARCHAR);
		builder.and().isNull("e");
		
		String sql = builder.build();
		
		String expect_sql = "UPDATE Person SET `name` = ?, `age` = ?, `addr` = ? "
				+ "WHERE age = ? AND  b in ( ?, ? ) AND  b LIKE ? "
				+ "AND  c BETWEEN ? AND ? AND  e IS NULL";

		Assert.assertEquals(expect_sql, sql);
		builder.buildParameters();
		Assert.assertEquals(10, builder.getStatementParameterIndex());
	}

}
