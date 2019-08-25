package com.ctrip.datasource.datasource;

import com.ctrip.platform.dal.dao.configure.DataSourceConfigure;
import com.ctrip.platform.dal.dao.datasource.DefaultDataSourceTerminateTask;
import com.ctrip.platform.dal.dao.datasource.SingleDataSource;
import com.ctrip.platform.dal.dao.log.DalLogTypes;
import com.ctrip.platform.dal.exceptions.DalException;
import com.dianping.cat.Cat;
import com.dianping.cat.CatHelper;
import com.dianping.cat.message.Transaction;

import javax.sql.DataSource;

public class CtripDataSourceTerminateTask extends DefaultDataSourceTerminateTask {
    private static final String DATASOURCE_CLOSE_DATASOURCE = "DataSource::closeDataSource";

    public CtripDataSourceTerminateTask(SingleDataSource oldDataSource) {
        super(oldDataSource);
    }

    public CtripDataSourceTerminateTask(String name, DataSource ds, DataSourceConfigure configure) {
        super(name, ds, configure);
    }

    @Override
    public void log(String dataSourceName, boolean isForceClosing, long startTimeMilliseconds) {
        String transactionName = String.format("%s:%s", DATASOURCE_CLOSE_DATASOURCE, dataSourceName);
        Transaction transaction = Cat.newTransaction(DalLogTypes.DAL_DATASOURCE, transactionName);
        if (isForceClosing) {
            String msg = String.format("DataSource %s has been forced closed.", dataSourceName);
            transaction.addData(msg);
            transaction.setStatus(new DalException(msg));
        }

        CatHelper.completeTransaction(transaction, startTimeMilliseconds);
    }

}
