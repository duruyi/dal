package com.ctrip.datasource.util;

import com.alibaba.fastjson.JSONObject;
import com.ctrip.datasource.net.HttpExecutor;
import com.ctrip.datasource.util.entity.MysqlApiConnectionStringInfo;
import com.ctrip.datasource.util.entity.MysqlApiConnectionStringInfoResponse;
import com.ctrip.framework.dal.cluster.client.util.StringUtils;
import com.dianping.cat.Cat;

public class MysqlApiConnectionStringUtils {

    private static final int DEFAULT_HTTP_TIMEOUT_MS = 1800;

    private static final String DB_MYSQL_API_PRO = "http://mysqlapi.db.ctripcorp.com:8080/database/getdbconninfo";

    private static final String DB_MYSQL_API_PRO_SHAJQ = "http://mysqlapi.jq.db.ctripcorp.com:8080/database/getdbconninfo";

    private static final String DB_MYSQL_API_PRO_SHAOY = "http://mysqlapi.oy.db.ctripcorp.com:8080/database/getdbconninfo";

    private static final String DB_MYSQL_API_PRO_SHARB = "http://mysqlapi.rb.db.ctripcorp.com:8080/database/getdbconninfo";

    private static final String DB_MYSQL_API_PRO_SHAFQ = "http://mysqlapi.fq.db.ctripcorp.com:8080/database/getdbconninfo";

    private static final String DB_MYSQL_API_UAT = "http://mysqlapi.db.uat.qa.nt.ctripcorp.com:8080/database/getdbconninfo";

    private static final String DB_MYSQL_API_FAT = "http://mysqlapi.db.fat.qa.nt.ctripcorp.com:8080/database/getdbconninfo";

    public static MysqlApiConnectionStringInfo getConnectionStringFromMysqlApi(String mysqlApiUrl, String dbName, String env) throws Exception {
        MysqlApiConnectionStringInfo info = null;
        String url = !StringUtils.isEmpty(mysqlApiUrl) ? mysqlApiUrl : "FAT".equalsIgnoreCase(env) ? DB_MYSQL_API_FAT : "UAT".equalsIgnoreCase(env) ?
                DB_MYSQL_API_UAT : DB_MYSQL_API_PRO;

        JSONObject json = new JSONObject();
        json.put("env", env);
        json.put("dbname", dbName);

        MysqlApiConnectionStringInfoResponse response = null;
        HttpExecutor executor = HttpExecutor.getInstance();
        try {
            String responseStr = executor.executePost(url, null, json.toString(), DEFAULT_HTTP_TIMEOUT_MS);
            response = GsonUtils.json2T(responseStr, MysqlApiConnectionStringInfoResponse.class);
        } catch (Exception e) {
            Cat.logError("get mgr info from db api fail, [dbName:" + dbName + "]", e);
            throw e;
        }
        if (response != null && "ok".equalsIgnoreCase(response.getMessage())) {
            info = response.getData();
        }
        return info;
    }
}
