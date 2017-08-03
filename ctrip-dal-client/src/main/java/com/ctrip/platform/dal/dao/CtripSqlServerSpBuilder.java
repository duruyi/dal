package com.ctrip.platform.dal.dao;

public class CtripSqlServerSpBuilder {
    private static final String SQLSERVER_TMPL_CALL = "exec %s %s";
    private static final String SQLSERVER_TMPL_SET_VALUE = "@%s=?";
    private static final String COLUMN_SEPARATOR = ", ";

    public static String buildSqlServerCallSql(String spName, String[] columns) {
        StringBuilder valuesSb = new StringBuilder();
        int i = 0;
        for (String value : columns) {
            valuesSb.append(String.format(SQLSERVER_TMPL_SET_VALUE, value));
            if (++i < columns.length)
                valuesSb.append(COLUMN_SEPARATOR);
        }

        return String.format(SQLSERVER_TMPL_CALL, spName, valuesSb.toString());
    }
}
