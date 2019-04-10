package dao.shard.newVersionCode;


import com.ctrip.platform.dal.common.enums.DatabaseCategory;
import com.ctrip.platform.dal.dao.*;
import com.ctrip.platform.dal.dao.helper.*;
import com.ctrip.platform.dal.dao.sqlbuilder.*;
import entity.SqlServerPeopleTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;


public class PeopleShardColModShardByDBOnSqlServerDao {
	private static final boolean ASC = true;
	private DalTableDao<SqlServerPeopleTable> client;
	private static final String DATA_BASE = "ShardColModShardByDBOnSqlServer";
	private static final DatabaseCategory dbCategory = DatabaseCategory.SqlServer;
	private DalQueryDao queryDao = null;

	private DalRowMapper<SqlServerPeopleTable> peopleShardColModShardByDBOnSqlServerRowMapper = null;

	private static final String TABLE_NAME="People";

	private String sqlList = "select * from " + TABLE_NAME;
	private String sqlFieldList = "select Name from " + TABLE_NAME;
	private String sqlCount = "select count(*) from " + TABLE_NAME;
	private String sqlObject = "select * from " + TABLE_NAME + " where PeopleID = ? and CityID=21";
	private String sqlFirst = "select * from " + TABLE_NAME + " where PeopleID = ?";
	private String sqlNoResult = "select * from " + TABLE_NAME + " where PeopleID = 7";
	private String sqlInParam = "select * from " + TABLE_NAME + " where CityID in (?)";

	public PeopleShardColModShardByDBOnSqlServerDao() throws SQLException {
		this.client = new DalTableDao<>(SqlServerPeopleTable.class,DATA_BASE);
		this.peopleShardColModShardByDBOnSqlServerRowMapper = new DalDefaultJpaMapper<>(SqlServerPeopleTable.class);
		this.queryDao = new DalQueryDao(DATA_BASE);
	}

	public PeopleShardColModShardByDBOnSqlServerDao(String DATA_BASE) throws SQLException {
		this.client = new DalTableDao<>(SqlServerPeopleTable.class,DATA_BASE);
		this.peopleShardColModShardByDBOnSqlServerRowMapper = new DalDefaultJpaMapper<>(SqlServerPeopleTable.class);
		this.queryDao = new DalQueryDao(DATA_BASE);
	}

	/**
	 * Query ignoreMissingFieldsAndAllowPartialTestOnSqlServer by the specified ID
	 * The ID must be a number
	 **/
	public SqlServerPeopleTable queryByPk(Number id, DalHints hints)
			throws SQLException {
		hints = DalHints.createIfAbsent(hints);
		return client.queryByPk(id, hints);
	}

	/**
	 * Query SqlServerPeopleTableWithDiffColumns by SqlServerPeopleTableWithDiffColumns instance which the primary key is set
	 **/
	public SqlServerPeopleTable queryByPk(SqlServerPeopleTable pk, DalHints hints)
			throws SQLException {
		hints = DalHints.createIfAbsent(hints);
		return client.queryByPk(pk, hints);
	}

	/**
	 * Query against sample pojo. All not null attributes of the passed in pojo
	 * will be used as search criteria.
	 **/
	public List<SqlServerPeopleTable> queryLike(SqlServerPeopleTable sample, DalHints hints)
			throws SQLException {
		hints = DalHints.createIfAbsent(hints);
		return client.queryLike(sample, hints);
	}

	/**
	 * Get the all records count
	 */
	public int count(DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);
		SelectSqlBuilder builder = new SelectSqlBuilder().selectCount();
		return client.count(builder, hints).intValue();
	}

	/**
	 * Query ignoreMissingFieldsAndAllowPartialTestOnSqlServer with paging function
	 * The pageSize and pageNo must be greater than zero.
	 */
	public List<SqlServerPeopleTable> queryAllByPage(int pageNo, int pageSize, DalHints hints)  throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.selectAllColumns().atPage(pageNo, pageSize).orderBy("PeopleID", ASC);

		return client.query(builder, hints);
	}

	/**
	 * Get all records from table
	 */
	public List<SqlServerPeopleTable> queryAll(DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder().selectAllColumns().orderBy("PeopleID", ASC);

		return client.query(builder, hints);
	}

	/**
	 * Insert pojo and get the generated PK back in keyHolder.
	 * If the "set no count on" for MS SqlServer is set(currently set in Ctrip), the operation may fail.
	 * Please don't pass keyholder for MS SqlServer to avoid the failure.
	 *
	 * @param hints
	 *            Additional parameters that instruct how DAL Client perform database operation.
	 * @param daoPojo
	 *            pojo to be inserted
	 * @return how many rows been affected
	 * @throws SQLException
	 */
	public int insert(DalHints hints, SqlServerPeopleTable daoPojo) throws SQLException {
		if(null == daoPojo)
			return 0;
		hints = DalHints.createIfAbsent(hints);
		return client.insert(hints, daoPojo);
	}

	/**
	 * Insert pojos one by one. If you want to inert them in the batch mode,
	 * user batchInsert instead. You can also use the combinedInsert.
	 *
	 * @param hints
	 *            Additional parameters that instruct how DAL Client perform database operation.
	 *            DalHintEnum.continueOnError can be used
	 *            to indicate that the inserting can be go on if there is any
	 *            failure.
	 * @param daoPojos
	 *            list of pojos to be inserted
	 * @return how many rows been affected
	 */
	public int[] insert(DalHints hints, List<SqlServerPeopleTable> daoPojos) throws SQLException {
		if(null == daoPojos || daoPojos.size() <= 0)
			return new int[0];
		hints = DalHints.createIfAbsent(hints);
		return client.insert(hints, daoPojos);
	}

	/**
	 * Insert pojo and get the generated PK back in keyHolder.
	 * If the "set no count on" for MS SqlServer is set(currently set in Ctrip), the operation may fail.
	 * Please don't pass keyholder for MS SqlServer to avoid the failure.
	 *
	 * @param hints
	 *            Additional parameters that instruct how DAL Client perform database operation.
	 * @param keyHolder
	 *            holder for generated primary keys
	 * @param daoPojo
	 *            pojo to be inserted
	 * @return how many rows been affected
	 * @throws SQLException
	 */
	public int insert(DalHints hints, KeyHolder keyHolder, SqlServerPeopleTable daoPojo) throws SQLException {
		if(null == daoPojo)
			return 0;
		hints = DalHints.createIfAbsent(hints);
		return client.insert(hints, keyHolder, daoPojo);
	}

	/**
	 * Insert pojos and get the generated PK back in keyHolder.
	 * If the "set no count on" for MS SqlServer is set(currently set in Ctrip), the operation may fail.
	 * Please don't pass keyholder for MS SqlServer to avoid the failure.
	 *
	 * @param hints
	 *            Additional parameters that instruct how DAL Client perform database operation.
	 *            DalHintEnum.continueOnError can be used
	 *            to indicate that the inserting can be go on if there is any
	 *            failure.
	 * @param keyHolder
	 *            holder for generated primary keys
	 * @param daoPojos
	 *            list of pojos to be inserted
	 * @return how many rows been affected
	 * @throws SQLException
	 */
	public int[] insert(DalHints hints, KeyHolder keyHolder, List<SqlServerPeopleTable> daoPojos) throws SQLException {
		if(null == daoPojos || daoPojos.size() <= 0)
			return new int[0];
		hints = DalHints.createIfAbsent(hints);
		return client.insert(hints, keyHolder, daoPojos);
	}

	/**
	 * Insert pojos in batch mode.
	 * The DalDetailResults will be set in hints to allow client know how the operation performed in each of the dao.noshardtest.shardtest.
	 *
	 * @param hints Additional parameters that instruct how DAL Client perform database operation.
	 * @param daoPojos list of pojos to be inserted
	 * @return how many rows been affected for inserting each of the pojo
	 * @throws SQLException
	 */
	public int[] batchInsert(DalHints hints, List<SqlServerPeopleTable> daoPojos) throws SQLException {
		if(null == daoPojos || daoPojos.size() <= 0)
			return new int[0];
		hints = DalHints.createIfAbsent(hints);
		return client.batchInsert(hints, daoPojos);
	}

	/**
	 * Delete the given pojo.
	 *
	 * @param hints Additional parameters that instruct how DAL Client perform database operation.
	 * @param daoPojo pojo to be deleted
	 * @return how many rows been affected
	 * @throws SQLException
	 */
	public int delete(DalHints hints, SqlServerPeopleTable daoPojo) throws SQLException {
		if(null == daoPojo)
			return 0;
		hints = DalHints.createIfAbsent(hints);
		return client.delete(hints, daoPojo);
	}

	/**
	 * Delete the given pojos list one by one.
	 *
	 * @param hints Additional parameters that instruct how DAL Client perform database operation.
	 * @param daoPojos list of pojos to be deleted
	 * @return how many rows been affected
	 * @throws SQLException
	 */
	public int[] delete(DalHints hints, List<SqlServerPeopleTable> daoPojos) throws SQLException {
		if(null == daoPojos || daoPojos.size() <= 0)
			return new int[0];
		hints = DalHints.createIfAbsent(hints);
		return client.delete(hints, daoPojos);
	}

	/**
	 * Delete the given pojo list in batch.
	 * The DalDetailResults will be set in hints to allow client know how the operation performed in each of the dao.noshardtest.shardtest.
	 *
	 * @param hints Additional parameters that instruct how DAL Client perform database operation.
	 * @param daoPojos list of pojos to be deleted
	 * @return how many rows been affected for deleting each of the pojo
	 * @throws SQLException
	 */
	public int[] batchDelete(DalHints hints, List<SqlServerPeopleTable> daoPojos) throws SQLException {
		if(null == daoPojos || daoPojos.size() <= 0)
			return new int[0];
		hints = DalHints.createIfAbsent(hints);
		return client.batchDelete(hints, daoPojos);
	}

	/**
	 * Update the given pojo . By default, if a field of pojo is null value,
	 * that field will be ignored, so that it will not be updated. You can
	 * overwrite this by set updateNullField in hints.
	 *
	 * @param hints
	 * 			Additional parameters that instruct how DAL Client perform database operation.
	 *          DalHintEnum.updateNullField can be used
	 *          to indicate that the field of pojo is null value will be update.
	 * @param daoPojo pojo to be updated
	 * @return how many rows been affected
	 * @throws SQLException
	 */
	public int update(DalHints hints, SqlServerPeopleTable daoPojo) throws SQLException {
		if(null == daoPojo)
			return 0;
		hints = DalHints.createIfAbsent(hints);
		return client.update(hints, daoPojo);
	}

	/**
	 * Update the given pojo list one by one. By default, if a field of pojo is null value,
	 * that field will be ignored, so that it will not be updated. You can
	 * overwrite this by set updateNullField in hints.
	 *
	 * @param hints
	 * 			Additional parameters that instruct how DAL Client perform database operation.
	 *          DalHintEnum.updateNullField can be used
	 *          to indicate that the field of pojo is null value will be update.
	 * @param daoPojos list of pojos to be updated
	 * @return how many rows been affected
	 * @throws SQLException
	 */
	public int[] update(DalHints hints, List<SqlServerPeopleTable> daoPojos) throws SQLException {
		if(null == daoPojos || daoPojos.size() <= 0)
			return new int[0];
		hints = DalHints.createIfAbsent(hints);
		return client.update(hints, daoPojos);
	}

	/**
	 * Update the given pojo list in batch.
	 *
	 * @return how many rows been affected
	 * @throws SQLException
	 */
	public int[] batchUpdate(DalHints hints, List<SqlServerPeopleTable> daoPojos) throws SQLException {
		if(null == daoPojos || daoPojos.size() <= 0)
			return new int[0];
		hints = DalHints.createIfAbsent(hints);
		return client.batchUpdate(hints, daoPojos);
	}

	/**
	 * 构建，查询
	 **/
	public List<SqlServerPeopleTable> testBuildQueryLikeWithMatchPattern(String Name, MatchPattern pattern, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
//		builder.like("Name",Name,pattern,Types.VARCHAR,false);
		builder.like("Name",Name,pattern,Types.VARCHAR);
		builder.orderBy("PeopleID", true);

		return client.query(builder, hints);

	}

	/**
	 * 构建，查询
	 **/
	public List<SqlServerPeopleTable> testBuildQueryLikeNullableWithMatchPattern(String Name,MatchPattern pattern, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
//		builder.likeNullable("Name",Name,pattern,Types.VARCHAR,false);
		builder.likeNullable("Name",Name,pattern,Types.VARCHAR);
		builder.orderBy("PeopleID", true);

		return client.query(builder, hints);

	}

	/**
	 * testEqual
	 **/
	public List<SqlServerPeopleTable> test(Integer param1, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.selectAllColumns();
//		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
		builder.equal("CityID", param1, Types.INTEGER, false);
		builder.orderBy("ProvinceID", true);

		return client.query(builder, hints);
	}

	/**
	 * 构建，查询
	 **/
	public List<String> test_build_query_fieldList(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("Name");
		builder.in("CityID", CityID, Types.INTEGER, false);

		return client.query(builder, hints.sortBy(new StringComparator()), String.class);
	}

	/**
	 * 构建，查询
	 **/
	public List<String> test_build_query_fieldListByPage(List<Integer> CityID, int pageNo, int pageSize, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("Name");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		builder.orderBy("PeopleID", false);
		builder.atPage(pageNo, pageSize);

		return client.query(builder, hints.sortBy(new StringComparator()), String.class);
	}

	/**
	 * 构建，查询
	 **/
	public String test_build_query_fieldSingle(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("Name");
		builder.in("CityID", CityID, Types.INTEGER, false);
		builder.requireSingle();

		return client.queryObject(builder, hints, String.class);
	}

	/**
	 * 构建，查询
	 **/
	public String test_build_query_fieldFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("Name");
		builder.in("CityID", CityID, Types.INTEGER, false);
		builder.requireFirst();

		return client.queryObject(builder, hints.sortBy(new StringComparator()), String.class);
	}

	/**
	 * 构建，查询
	 **/
	public List<SqlServerPeopleTable> test_build_query_list(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
		builder.in("CityID", CityID, Types.INTEGER, false);

		return client.query(builder, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 构建，查询
	 **/
	public List<SqlServerPeopleTable> test_build_queryPartialFieldsByHints_list(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);

		return client.query(builder, hints.partialQuery("Name","CityID").sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 构建，查询部分字段
	 **/
	public List<SqlServerPeopleTable> test_build_queryPartial_list(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name");
		builder.in("CityID", CityID, Types.INTEGER, false);

		return client.query(builder, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 构建，查询
	 **/
	public List<SqlServerPeopleTable> test_build_query_listByPage(List<Integer> CityID, int pageNo, int pageSize, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		builder.orderBy("PeopleID", true);
		builder.atPage(pageNo, pageSize);

		return client.query(builder, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 构建，查询部分字段
	 **/
	public List<SqlServerPeopleTable> test_build_queryPartial_listByPage(List<Integer> CityID, int pageNo, int pageSize, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		builder.orderBy("PeopleID", true);
		builder.atPage(pageNo, pageSize);

		return client.query(builder, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 构建，查询
	 **/
	public SqlServerPeopleTable test_build_query_single(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		builder.requireSingle();

		return client.queryObject(builder, hints);
	}

	/**
	 * 构建，查询部分字段
	 **/
	public SqlServerPeopleTable test_build_queryPartial_single(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		builder.requireSingle();

		return client.queryObject(builder, hints);
	}

	/**
	 * 构建，查询
	 **/
	public SqlServerPeopleTable test_build_query_first(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		builder.requireFirst();

		return client.queryObject(builder, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	public SqlServerPeopleTable test_query_first_with_requireFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		builder.requireFirst();

		return client.queryFirst(builder, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	public SqlServerPeopleTable test_query_first_without_requireFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		return client.queryFirst(builder, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 构建，查询部分字段
	 **/
	public SqlServerPeopleTable test_build_queryPartial_first(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		SelectSqlBuilder builder = new SelectSqlBuilder();
		builder.select("CityID","Name");
		builder.inNullable("CityID", CityID, Types.INTEGER, false);
		builder.requireFirst();

		return client.queryObject(builder, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 构建，更新
	 **/
	public int test_build_update(String Name, List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		UpdateSqlBuilder builder = new UpdateSqlBuilder();
		builder.update("Name", Name, Types.VARCHAR);
		builder.inNullable("CityID", CityID, Types.INTEGER, false);

		return client.update(builder, hints);
	}
	/**
	 * 构建，新增
	 **/
	public int test_build_insert(Integer CityID, String Name, Integer ProvinceID, Integer CountryID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		InsertSqlBuilder builder = new InsertSqlBuilder();
		builder.set("CityID", CityID, Types.INTEGER);
		builder.set("Name", Name, Types.VARCHAR);
		builder.set("ProvinceID", ProvinceID, Types.INTEGER);
		builder.set("CountryID", CountryID, Types.INTEGER);

		return client.insert(builder, hints);
	}

	/**
	 * 构建，删除
	 **/
	public int test_build_delete(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		DeleteSqlBuilder builder = new DeleteSqlBuilder();
		builder.inNullable("CityID", CityID, Types.INTEGER, false);

		return client.delete(builder, hints);
	}

	/**
	 * 自定义，查询
	 **/
	public List<String> test_def_query_fieldList(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<List<String>> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select Name from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.simpleType();

		return queryDao.query(builder, parameters, hints.sortBy(new StringComparator()));
	}

	/**
	 * 自定义，查询
	 **/
	public List<String> test_def_query_fieldListByPage(List<Integer> CityID, int pageNo, int pageSize, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<List<String>> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select Name from People with (nolock) where CityID in (?) order by PeopleID desc");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.simpleType().atPage(pageNo, pageSize);

		return queryDao.query(builder, parameters, hints.sortBy(new StringComparator()));
	}

	/**
	 * 自定义，查询
	 **/
	public String test_def_query_fieldSingle(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<String> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select Name from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.simpleType().requireSingle().nullable();

		return queryDao.query(builder, parameters, hints);
	}

	/**
	 * 自定义，查询
	 **/
	public String test_def_query_fieldFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<String> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select Name from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.simpleType().requireFirst().nullable();

		return queryDao.query(builder, parameters, hints.sortBy(new StringComparator()));
	}

	/**
	 * 构建，查询
	 **/
	public List<SqlServerPeopleTable> test_ClientQueryFrom_list(List<Integer> CityID, DalHints hints,int start,int count) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		SelectSqlBuilder builder = new SelectSqlBuilder();
//		builder.select("CityID","Name","ProvinceID","PeopleID","CountryID");
//		builder.in("CityID", CityID, Types.INTEGER, false);
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);

		return client.queryFrom("CityID in (?) order by CityID", parameters, hints.sortBy(new SqlServerPeopleTableComparator()), start, count);
//		return client.query(builder, hints);
	}


	/**
	 * 构建，查询
	 **/
	public List<SqlServerPeopleTable> test_ClientQueryFromPartialFieldsSet_list(List<Integer> CityID, DalHints hints,int start,int count) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);

		Set<String> columns = new HashSet<>();
		columns.add("CityID");
		columns.add("Name");

		return client.queryFrom("CityID in (?) order by CityID", parameters, hints.partialQuery(columns).sortBy(new SqlServerPeopleTableComparator()), start, count);

	}

	/**
	 * 构建，查询
	 **/
	public List<SqlServerPeopleTable> test_ClientQueryFromPartialFieldsStrings_list(List<Integer> CityID, DalHints hints,int start,int count) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);

//		Set<String> columns = new HashSet<>();
//		columns.add("CityID");
//		columns.add("Name");

		return client.queryFrom("CityID in (?) order by CityID", parameters, hints.partialQuery("CityID","Name").sortBy(new SqlServerPeopleTableComparator()), start, count);

	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_query(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?)";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.query(sql, parameters, hints,SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_partialQuery(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?)";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.query(sql, parameters, hints.partialQuery("Name"),SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_queryForObject(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryForObject(sql, parameters, hints,SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_partialQueryForObject(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryForObject(sql, parameters, hints.partialQuery("Name"),SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_queryForObjectNullable(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryForObjectNullable(sql, parameters, hints,SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_partialQueryForObjectNullable(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryForObjectNullable(sql, parameters, hints.partialQuery("Name"),SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_queryFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryFirst(sql, parameters, hints,SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_partialQueryFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryFirst(sql, parameters, hints.partialQuery("Name"),SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_queryFirstNullable(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryFirstNullable(sql, parameters, hints,SqlServerPeopleTable.class);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_partialQueryFirstNullable(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryFirstNullable(sql, parameters, hints.partialQuery("Name"),SqlServerPeopleTable.class);
	}


	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_queryTop(List<Integer> CityID, DalHints hints,int count) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryTop(sql, parameters, hints,SqlServerPeopleTable.class,count);
	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_partialQueryTop(List<Integer> CityID, DalHints hints,int count) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryTop(sql, parameters, hints.partialQuery("Name"),SqlServerPeopleTable.class,count);
	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_queryFrom(List<Integer> CityID, DalHints hints,int start,int count) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryFrom(sql, parameters, hints,SqlServerPeopleTable.class,start,count);
	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_partialQueryFrom(List<Integer> CityID, DalHints hints,int start,int count) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

//		FreeSelectSqlBuilder<List<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>> builder = new FreeSelectSqlBuilder<>(dbCategory);
//		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		String sql="select * from People with (nolock) where CityID in (?) order by PeopleID desc";
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
//		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.queryFrom(sql, parameters, hints.partialQuery("Name"),SqlServerPeopleTable.class, start,count);
	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_query_list(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<List<SqlServerPeopleTable>> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);

		return queryDao.query(builder, parameters, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，查询部分字段
	 **/
	public List<SqlServerPeopleTable> test_def_queryPartialSet_list(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<List<SqlServerPeopleTable>> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);
		Set<String> columns = new HashSet<>();
		columns.add("CityID");
		columns.add("Name");
		return queryDao.query(builder, parameters, hints.partialQuery(columns).sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，查询部分字段
	 **/
	public List<SqlServerPeopleTable> test_def_queryPartialStrings_list(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<List<SqlServerPeopleTable>> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper);
//		Set<String> columns = new HashSet<>();
//		columns.add("CityID");
//		columns.add("Name");
		return queryDao.query(builder, parameters, hints.partialQuery("CityID","Name").sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_query_listByPage(List<Integer> CityID, int pageNo, int pageSize, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<List<SqlServerPeopleTable>> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?) order by PeopleID");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).atPage(pageNo, pageSize);

		return queryDao.query(builder, parameters, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_queryPartialSet_listByPage(List<Integer> CityID, int pageNo, int pageSize, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<List<SqlServerPeopleTable>> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?) order by PeopleID");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).atPage(pageNo, pageSize);

		Set<String> columns = new HashSet<>();
		columns.add("CityID");
		columns.add("Name");
		return queryDao.query(builder, parameters, hints.partialQuery(columns).sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，查询
	 **/
	public List<SqlServerPeopleTable> test_def_queryPartialStrings_listByPage(List<Integer> CityID, int pageNo, int pageSize, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<List<SqlServerPeopleTable>> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?) order by PeopleID");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).atPage(pageNo, pageSize);

		return queryDao.query(builder, parameters, hints.partialQuery("CityID","Name").sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_query_listSingle(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<SqlServerPeopleTable> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).requireSingle().nullable();

		return (SqlServerPeopleTable)queryDao.query(builder, parameters, hints);
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_queryPartialSet_listSingle(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<SqlServerPeopleTable> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).requireSingle().nullable();
		Set<String> columns = new HashSet<>();
		columns.add("CityID");
		columns.add("Name");
		return (SqlServerPeopleTable)queryDao.query(builder, parameters, hints.partialQuery(columns));
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_queryPartialStrings_listSingle(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<SqlServerPeopleTable> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).requireSingle().nullable();

		return (SqlServerPeopleTable)queryDao.query(builder, parameters, hints.partialQuery("CityID","Name"));
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_query_listFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<SqlServerPeopleTable> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).requireFirst().nullable();

		return (SqlServerPeopleTable)queryDao.query(builder, parameters, hints.sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_queryPartialSet_listFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<SqlServerPeopleTable> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).requireFirst().nullable();
		Set<String> columns = new HashSet<>();
		columns.add("CityID");
		columns.add("Name");
		return (SqlServerPeopleTable)queryDao.query(builder, parameters, hints.partialQuery(columns).sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，查询
	 **/
	public SqlServerPeopleTable test_def_queryPartialStrings_listFirst(List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<SqlServerPeopleTable> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select * from People with (nolock) where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);
		builder.mapWith(peopleShardColModShardByDBOnSqlServerRowMapper).requireFirst().nullable();

		return (SqlServerPeopleTable)queryDao.query(builder, parameters, hints.partialQuery("CityID","Name").sortBy(new SqlServerPeopleTableComparator()));
	}

	/**
	 * 自定义，更新
	 **/
	public int test_def_update (String Name, List<Integer> CityID, DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeUpdateSqlBuilder builder = new FreeUpdateSqlBuilder(dbCategory);
		builder.setTemplate("update People set Name=? where CityID in (?)");
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		parameters.set(i++, "Name", Types.VARCHAR, Name);
		i = parameters.setInParameter(i, "CityID", Types.INTEGER, CityID);

		return queryDao.update(builder, parameters, hints);
	}

	/**
	 * 自定义，删除
	 **/
	public int test_def_truncate (DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeUpdateSqlBuilder builder = new FreeUpdateSqlBuilder(dbCategory);
		builder.setTemplate("truncate table People");
		StatementParameters parameters = new StatementParameters();
		int i = 1;

		return queryDao.update(builder, parameters, hints);
	}


	public List<SqlServerPeopleTable> test_def_top(DalHints hints) throws Exception {
		hints = DalHints.createIfAbsent(hints);
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		return	queryDao.query("select top 100 * from People", parameters, hints, SqlServerPeopleTable.class);
	}

	/**
	 * count
	 **/
	public Integer test_def_count(DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<Integer> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select count(*) from people with (nolock)");
		StatementParameters parameters = new StatementParameters();
		builder.simpleType().requireSingle();

		return queryDao.query(builder, parameters, hints);
	}

	/**
	 * max
	 **/
	public Integer test_def_queryMax(DalHints hints) throws SQLException {
		hints = DalHints.createIfAbsent(hints);

		FreeSelectSqlBuilder<Integer> builder = new FreeSelectSqlBuilder<>(dbCategory);
		builder.setTemplate("select max(CityID) from people with (nolock)");
		StatementParameters parameters = new StatementParameters();
		builder.simpleType().requireFirst().nullable();

		return queryDao.query(builder, parameters, hints);
	}

	private class SqlServerPeopleTableComparator implements Comparator<SqlServerPeopleTable>{
		@Override
		public int compare(SqlServerPeopleTable o1, SqlServerPeopleTable o2) {
			return new Integer(o1.getCityID()).compareTo(o2.getCityID());
		}
	}

	private class StringComparator implements Comparator<String>{
		@Override
		public int compare(String o1, String o2) {
			return new Integer( o1.compareTo(o2));
		}
	}

	private class IntegerComparator implements Comparator<Integer>{
		@Override
		public int compare(Integer o1, Integer o2) {
			return new Integer(o1.compareTo(o2));
		}
	}

	private class IntegerRowMapper implements DalRowMapper<Integer> {

		@Override
		public Integer map(ResultSet rs, int rowNum) throws SQLException {
			return rs.getInt(1);
		}
	}

	private class StringRowMapper implements DalRowMapper<String> {

		@Override
		public String map(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString(1);
		}
	}

	private class TestDalRowCallback implements DalRowCallback {

		@Override
		public void process(ResultSet rs) throws SQLException {
		}
	}


	public List queryListMultipleAllShards(DalHints hints) throws SQLException {

		MultipleSqlBuilder builder = new MultipleSqlBuilder();

		builder.addQuery(sqlList, new StatementParameters(),peopleShardColModShardByDBOnSqlServerRowMapper);//ListMapper
		builder.addQuery(sqlList, new StatementParameters(),peopleShardColModShardByDBOnSqlServerRowMapper,new DalListMerger<SqlServerPeopleTable>());//ListMerger
		builder.addQuery(sqlList, new StatementParameters(), SqlServerPeopleTable.class);
		builder.addQuery(sqlList, new StatementParameters(), SqlServerPeopleTable.class, new DalListMerger<SqlServerPeopleTable>());
//	    builder.addQuery(sqlList, new StatementParameters(), SqlServerPeopleTableWithDiffColumns.class, new DalRangedResultMerger<SqlServerPeopleTableWithDiffColumns>(1,2));
//	    builder.addQuery(sqlList, new StatementParameters(), new DalRowMapperExtractor(peopleShardColModShardByDBOnSqlServerRowMapper,1,1), new DalListMerger<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>());
		builder.addQuery(sqlList, new StatementParameters(), new DalSingleResultExtractor(peopleShardColModShardByDBOnSqlServerRowMapper,false), new DalFirstResultMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlList, new StatementParameters(), new TestDalRowCallback());
		builder.addQuery(sqlList, new StatementParameters(), SqlServerPeopleTable.class, new SqlServerPeopleTableComparator());
		builder.addQuery(sqlList, new StatementParameters(), peopleShardColModShardByDBOnSqlServerRowMapper, new SqlServerPeopleTableComparator());

		builder.addQuery(sqlCount, new StatementParameters(),new IntegerRowMapper());
		builder.addQuery(sqlCount, new StatementParameters(),new IntegerRowMapper(),new DalListMerger<Integer>());//ListMerger
		builder.addQuery(sqlCount, new StatementParameters(), Integer.class);
		builder.addQuery(sqlCount, new StatementParameters(), Integer.class, new DalListMerger<Integer>());
		builder.addQuery(sqlCount, new StatementParameters(), new DalRowMapperExtractor(new IntegerRowMapper()), new DalListMerger<Integer>());
		builder.addQuery(sqlCount, new StatementParameters(), new TestDalRowCallback());
//	    builder.addQuery(sqlCount, new StatementParameters(), Integer.class, new IntegerComparator());
		builder.addQuery(sqlCount, new StatementParameters(), new IntegerRowMapper(), new IntegerComparator());

		builder.addQuery(sqlFieldList, new StatementParameters(), String.class);
		builder.addQuery(sqlFieldList, new StatementParameters(),String.class,new DalListMerger<String>());
		builder.addQuery(sqlFieldList, new StatementParameters(), new StringRowMapper());
		builder.addQuery(sqlFieldList, new StatementParameters(), new StringRowMapper(),new DalListMerger<String>());
		builder.addQuery(sqlFieldList, new StatementParameters(), new DalSingleResultExtractor(new StringRowMapper(),false), new DalFirstResultMerger<String>());
		builder.addQuery(sqlFieldList, new StatementParameters(), new TestDalRowCallback());
		builder.addQuery(sqlFieldList, new StatementParameters(), String.class, new StringComparator());
		builder.addQuery(sqlFieldList, new StatementParameters(), new StringRowMapper(), new StringComparator());

		builder.addQuery(sqlFirst, new StatementParameters().setSensitive(1, "PeopleID", Types.INTEGER, 1),peopleShardColModShardByDBOnSqlServerRowMapper);//ListMapper
		builder.addQuery(sqlFirst, new StatementParameters().setSensitive(1, "PeopleID", Types.INTEGER, 1),peopleShardColModShardByDBOnSqlServerRowMapper,new DalListMerger<SqlServerPeopleTable>());//ListMerger
		builder.addQuery(sqlFirst, new StatementParameters().setSensitive(1, "PeopleID", Types.INTEGER, 1), SqlServerPeopleTable.class);
		builder.addQuery(sqlFirst, new StatementParameters().setSensitive(1, "PeopleID", Types.INTEGER, 1), SqlServerPeopleTable.class, new DalListMerger<SqlServerPeopleTable>());
//	    builder.addQuery(sqlList, new StatementParameters(), SqlServerPeopleTableWithDiffColumns.class, new DalRangedResultMerger<SqlServerPeopleTableWithDiffColumns>(1,2));
//	    builder.addQuery(sqlList, new StatementParameters(), new DalRowMapperExtractor(peopleShardColModShardByDBOnSqlServerRowMapper,1,1), new DalListMerger<ignoreMissingFieldsAndAllowPartialTestOnSqlServer>());
		builder.addQuery(sqlFirst, new StatementParameters().setSensitive(1, "PeopleID", Types.INTEGER, 1), new DalSingleResultExtractor(peopleShardColModShardByDBOnSqlServerRowMapper,false), new DalFirstResultMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlFirst, new StatementParameters().setSensitive(1, "PeopleID", Types.INTEGER, 1), new TestDalRowCallback());
		builder.addQuery(sqlFirst, new StatementParameters().setSensitive(1, "PeopleID", Types.INTEGER, 1), SqlServerPeopleTable.class, new SqlServerPeopleTableComparator());
		builder.addQuery(sqlFirst, new StatementParameters().setSensitive(1, "PeopleID", Types.INTEGER, 1), peopleShardColModShardByDBOnSqlServerRowMapper, new SqlServerPeopleTableComparator());


//	    builder.addQuery(sqlObject, parameters, Integer.class);
		builder.addQuery(sqlObject, new StatementParameters().set(1, "PeopleID", Types.INTEGER, 1), peopleShardColModShardByDBOnSqlServerRowMapper);
		builder.addQuery(sqlObject, new StatementParameters().set(1, "PeopleID", Types.INTEGER, 1),peopleShardColModShardByDBOnSqlServerRowMapper,new DalListMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlObject, new StatementParameters().set(1, "PeopleID", Types.INTEGER, 1), SqlServerPeopleTable.class);
		builder.addQuery(sqlObject, new StatementParameters().set(1, "PeopleID", Types.INTEGER, 1), SqlServerPeopleTable.class,new DalListMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlObject, new StatementParameters().set(1, "PeopleID", Types.INTEGER, 1), new DalSingleResultExtractor(peopleShardColModShardByDBOnSqlServerRowMapper,false), new DalFirstResultMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlObject, new StatementParameters().set(1, "PeopleID", Types.INTEGER, 1), new TestDalRowCallback());
		builder.addQuery(sqlObject, new StatementParameters().set(1, "PeopleID", Types.INTEGER, 1), SqlServerPeopleTable.class, new SqlServerPeopleTableComparator());
		builder.addQuery(sqlObject, new StatementParameters().set(1, "PeopleID", Types.INTEGER, 1), peopleShardColModShardByDBOnSqlServerRowMapper, new SqlServerPeopleTableComparator());
//	    builder.addQuery(sqlFieldList, new StatementParameters(),String.class, new DalListMerger<String>());//merger
//	    builder.addQuery(sqlListCount,new StatementParameters(), ignoreMissingFieldsAndAllowPartialTestOnSqlServer.class, new SqlServerPeopleTableComparator());//sorter
//	    builder.addQuery(sqlListCount, new StatementParameters(),Integer.class, new DalListMerger<Integer>());//merger
//	    builder.addQuery(sqlObject, parameters,Integer.class, new InteregrComparator());//soter
//	    builder.addQuery(sqlNoResult, new StatementParameters(),new TestDalRowCallback3());//callback
		StatementParameters parameters = new StatementParameters();
		int i = 1;
		i = parameters.setSensitiveInParameter(i, "CityID", Types.INTEGER, new ArrayList<Integer>(){{add(20); add(21);add(23);}});

		builder.addQuery(sqlInParam, parameters, peopleShardColModShardByDBOnSqlServerRowMapper);
		builder.addQuery(sqlInParam, parameters,peopleShardColModShardByDBOnSqlServerRowMapper,new DalListMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlInParam, parameters, SqlServerPeopleTable.class);
		builder.addQuery(sqlInParam, parameters, SqlServerPeopleTable.class,new DalListMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlInParam, parameters, new DalSingleResultExtractor(peopleShardColModShardByDBOnSqlServerRowMapper,false), new DalFirstResultMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlInParam, parameters, new TestDalRowCallback());
		builder.addQuery(sqlInParam, parameters, SqlServerPeopleTable.class, new SqlServerPeopleTableComparator());
		builder.addQuery(sqlInParam, parameters, peopleShardColModShardByDBOnSqlServerRowMapper, new SqlServerPeopleTableComparator());

		builder.addQuery(sqlNoResult, new StatementParameters(), peopleShardColModShardByDBOnSqlServerRowMapper);
		builder.addQuery(sqlNoResult, new StatementParameters(),peopleShardColModShardByDBOnSqlServerRowMapper,new DalListMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlNoResult, new StatementParameters(), SqlServerPeopleTable.class);
		builder.addQuery(sqlNoResult, new StatementParameters(), SqlServerPeopleTable.class,new DalListMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlNoResult, new StatementParameters(), new DalSingleResultExtractor(peopleShardColModShardByDBOnSqlServerRowMapper,false), new DalFirstResultMerger<SqlServerPeopleTable>());
		builder.addQuery(sqlNoResult, new StatementParameters(), new TestDalRowCallback());
		builder.addQuery(sqlNoResult, new StatementParameters(), SqlServerPeopleTable.class, new SqlServerPeopleTableComparator());
		builder.addQuery(sqlNoResult, new StatementParameters(), peopleShardColModShardByDBOnSqlServerRowMapper, new SqlServerPeopleTableComparator());

		builder.addQuery(sqlCount, new StatementParameters(), Integer.class, new IntegerComparator());
		return queryDao.query(builder, hints);
	}
}