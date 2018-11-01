package com.ctrip.platform.dal.dao.helper;

import com.ctrip.platform.dal.dao.DalClient;
import com.ctrip.platform.dal.dao.DalHints;
import com.ctrip.platform.dal.dao.DalResultSetExtractor;
import com.ctrip.platform.dal.dao.StatementParameters;
import com.ctrip.platform.dal.dao.log.ILogger;
import com.ctrip.platform.dal.dao.tvp.TVPMetaInfo;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TVPHelper {
    private static final ILogger LOGGER = DalElementFactory.DEFAULT.getILogger();
    private static final String DAL = "DAL";
    private String GET_TVP_COLUMNS_FORMAT = "TVP::getTVPColumns:%s";

    private ConcurrentMap<String, TVPMetaInfo> tvpColumnsMap = new ConcurrentHashMap<>();
    private final Object LOCK = new Object();

    private final int FIRST_COLUMN_INDEX = 1;
    private String tvpColumnSql = "SELECT clmns.NAME AS [Name] FROM sys.table_types AS tt "
            + " INNER JOIN sys.schemas AS stt ON stt.schema_id = tt.schema_id "
            + " INNER JOIN sys.all_columns AS clmns ON clmns.object_id = tt.type_table_object_id "
            + " WHERE (tt.NAME = ? AND SCHEMA_NAME(tt.schema_id) = 'dbo') " + " ORDER BY clmns.column_id ASC";

    public List<String> getTVPColumns(String logicDbName, String tvpName, DalClient client) {
        if (logicDbName == null || logicDbName.isEmpty())
            return null;

        if (tvpName == null || tvpName.isEmpty())
            return null;

        if (client == null)
            return null;

        TVPMetaInfo metaInfo = tvpColumnsMap.get(logicDbName);
        if (metaInfo == null) {
            tvpColumnsMap.putIfAbsent(logicDbName, new TVPMetaInfo());
            metaInfo = tvpColumnsMap.get(logicDbName);
        }

        if (metaInfo.getTVPColumns(tvpName) == null) {
            synchronized (LOCK) {
                if (metaInfo.getTVPColumns(tvpName) == null) {
                    if (!metaInfo.getMetaInfoFetched()) {
                        List<String> list = fetchTVPColumnsBySql(tvpName, client);
                        metaInfo.setMetaInfoFetched();
                        if (list != null) {
                            metaInfo.setTVPColumns(tvpName, list);
                        }
                    }
                }
            }
        }

        return metaInfo.getTVPColumns(tvpName);
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty())
            return "";

        return StringUtils.join(list, ",");
    }

    private List<String> fetchTVPColumnsBySql(String tvpName, DalClient client) {
        StatementParameters parameters = new StatementParameters();
        int index = 1;
        parameters.set(index++, Types.NVARCHAR, tvpName);
        List<String> list = null;
        Transaction t = Cat.newTransaction(DAL, String.format(GET_TVP_COLUMNS_FORMAT, tvpName));

        try {
            list = client.query(tvpColumnSql, parameters, new DalHints(), new DalResultSetExtractor<List<String>>() {
                @Override
                public List<String> extract(ResultSet rs) throws SQLException {
                    List<String> result = new ArrayList<>();
                    if (rs != null) {
                        while (rs.next()) {
                            result.add(rs.getString(FIRST_COLUMN_INDEX)); // 1 indicate first column
                        }
                    }
                    return result;
                }
            });

            t.addData(listToString(list));
            t.setStatus(Transaction.SUCCESS);
        } catch (Throwable e) {
            t.setStatus(e);
            LOGGER.error(e.getMessage(), e);
        } finally {
            t.complete();
        }
        return list;
    }

}
