package com.ctrip.platform.dal.application.service;

import com.ctrip.datasource.configure.DalDataSourceFactory;
import com.ctrip.framework.dal.cluster.client.util.StringUtils;
import com.ctrip.platform.dal.application.Application;
import com.ctrip.platform.dal.application.Config.DalApplicationConfig;
import com.ctrip.platform.dal.application.dao.DALServiceDao;
import com.ctrip.platform.dal.exceptions.DalRuntimeException;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
public class MgrRequestTask {
    private static final String selectSQL = "select * from dalservicetable limit 1;";
    private static final String updateSQL = "update dalservicetable set Age=11 where ID=1;";

    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private static Logger log = LoggerFactory.getLogger(Application.class);
    private int qps = 100;
    private int delay = 40;
    private SQLThread selectSQLThread;
    private SQLThread insertSQLThread;
    private SQLThread updateSQLThread;
    private SQLThread deleteSQLThread;
    private String clusterName = "dalservice2db_dalcluster";


    @Autowired
    private DalApplicationConfig dalApplicationConfig;


    @PostConstruct
    private void init() throws Exception {
        try {
            String qpsCfg = dalApplicationConfig.getQPS();
            if (qpsCfg != null)
                qps = Integer.parseInt(qpsCfg);
            String cluster = dalApplicationConfig.getClusterName();
            if (!StringUtils.isTrimmedEmpty(cluster))
                this.clusterName = cluster;
            delay = (1000 / qps) * 4;
        } catch (Exception e) {
            Cat.logError("get qps or clusterName from QConfig error", e);
        }

        DataSource dataSource = new DalDataSourceFactory().getOrCreateDataSource(clusterName);

        try {
            selectSQLThread = new SQLThread(delay, dataSource) {
                @Override
                void execute(Statement statement) throws SQLException {
                    Cat.logEvent("DalApplication", "mgrTestSelect", Message.SUCCESS, "execute select");
                    statement.executeQuery(selectSQL);
                }
            };
            insertSQLThread = new SQLThread(delay, dataSource) {
                @Override
                void execute(Statement statement) throws SQLException {
                    Cat.logEvent("DalApplication", "mgrTestInsert", Message.SUCCESS, "execute insert");
                    statement.execute("insert into dalservicetable values (" + (int)System.currentTimeMillis() + ",'insert', 10);");
                }
            };
            updateSQLThread = new SQLThread(delay, dataSource) {
                @Override
                void execute(Statement statement) throws SQLException {
                    Cat.logEvent("DalApplication", "mgrTestUpdate", Message.SUCCESS, "execute update");
                    statement.execute(updateSQL);
                }
            };
            deleteSQLThread = new SQLThread(delay, dataSource) {
                @Override
                void execute(Statement statement) throws SQLException {
                    Cat.logEvent("DalApplication", "mgrTestDelete", Message.SUCCESS, "execute delete");
                    statement.execute("delete from dalservicetable where ID=" + (int)System.currentTimeMillis());
                }
            };
            startTasks();
        } catch (Exception e) {
            log.error("DALRequestTask init error", e);
        }
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        executor.shutdownNow();
    }

    public void cancelTasks() {
        selectSQLThread.exit = true;
        updateSQLThread.exit = true;
        insertSQLThread.exit = true;
        deleteSQLThread.exit = true;
    }

    private void startTasks() {
        executor.submit(selectSQLThread);
        executor.submit(updateSQLThread);
        executor.submit(insertSQLThread);
        executor.submit(deleteSQLThread);
    }

    public void restart() throws Exception {
        cancelTasks();
        init();
    }

    public int getQps() {
        return qps;
    }

    private static abstract class SQLThread extends Thread {
        public volatile boolean exit = false;
        private final long delay;
        private DataSource dataSource;

        public SQLThread(long delay, DataSource dataSource) {
            this.delay = delay;
            this.dataSource = dataSource;
        }

        @Override
        public void run() {
            while (!exit) {
                Transaction out = Cat.newTransaction("DAL.App.Task", "DalMgrTest");
                try (Connection connection = getConnection()){
                    try (Statement statement = connection.createStatement()){
                        statement.setQueryTimeout(1);
                        execute(statement);
                    }
                    out.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    log.error("DalMgrTest error", e);
                    Cat.logError("DalMgrTest error", e);
                    out.setStatus(e);
                } finally {
                    out.complete();
                    try {
                        Thread.sleep(delay);
                    } catch (Exception e) {
                    }
                }
            }
        }

        private Connection getConnection() throws SQLException {
            Transaction in = Cat.newTransaction("DAL.App.Task.in", "getConnection");
            try {
                Connection connection = dataSource.getConnection();
                Cat.logEvent("MGR.getConnection", connection.getMetaData().getURL(), Message.SUCCESS, "getConnectionEnd");
                in.setStatus(Transaction.SUCCESS);
                return connection;
            } catch (SQLException e) {
                in.setStatus(e);
                throw e;
            } finally {
                in.complete();
            }
        }

        abstract void execute(Statement statement) throws SQLException;
    }

}
