package com.ctrip.framework.dal.dbconfig.plugin.service;

import com.ctrip.framework.dal.dbconfig.plugin.entity.*;
import com.ctrip.framework.dal.dbconfig.plugin.util.ConfigUtils;
import com.ctrip.framework.dal.dbconfig.plugin.util.Utils;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * Created by shenjie on 2019/5/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MongClusterTest {
    public static final String FAT_ENV = "fat";
    public static final String UAT_ENV = "uat";
    public static final String PRO_ENV = "pro";
    public static final String OPERATOR = "mongoTest";
    public static final String UPDATE_SSL_CODE = "HN00000000002356";
    public static final String MONGO_CLUSTER_NAME = "diuserprofile-diuserprofiledb";
    public static final String NEW_USER_ID = "newUserId";

    @Autowired
    private MongoPluginService mongoPluginService;

    @Test
    public void addExistMongoCluster() throws Exception {
        MongoClusterEntity mongoCluster = generateMongoClusterEntity(MONGO_CLUSTER_NAME);
        PluginResponse response = mongoPluginService.addMongoCluster(mongoCluster, FAT_ENV, OPERATOR);
        assert response != null;
        assert response.getStatus() != 0;

        // get client config from fat16, need add vm option.
        addLocalVmOptions();
//        addFat16VmOptions();
        String content = ConfigUtils.getMongoFileResult(mongoCluster.getClusterName());
        assert Strings.isNotBlank(content);
    }

    @Test
    public void addAbsentMongoCluster() throws Exception {
        MongoClusterEntity mongoCluster = generateMongoClusterEntity(UUID.randomUUID().toString());
        PluginResponse response = mongoPluginService.addMongoCluster(mongoCluster, FAT_ENV, OPERATOR);
        assert response != null;
        assert response.getStatus() == 0;

        // get client config from fat16, need add vm option.
        addLocalVmOptions();
//        addFat16VmOptions();
        String content = ConfigUtils.getMongoFileResult(mongoCluster.getClusterName());
        assert Strings.isNotBlank(content);
    }

    @Test
    public void update() {
        // add cluster
        MongoClusterEntity mongoCluster = generateMongoClusterEntity(UUID.randomUUID().toString());
        String clusterName = mongoCluster.getClusterName();
        String userId = mongoCluster.getUserId();
        assert mongoCluster.getEnabled();
        PluginResponse response = mongoPluginService.addMongoCluster(mongoCluster, FAT_ENV, OPERATOR);
        assert response != null;
        assert response.getStatus() == 0;

        // update cluster
        mongoCluster.setEnabled(false);
        mongoCluster.setUserId(NEW_USER_ID);
        PluginResponse updateResponse = mongoPluginService.updateMongoCluster(mongoCluster, FAT_ENV, OPERATOR);
        assert updateResponse != null;
        assert updateResponse.getStatus() == 0;

        // get cluster
        MongoClusterGetResponse getResponse = mongoPluginService.getMongoCluster(clusterName, FAT_ENV);
        assert getResponse != null;
        assert getResponse.getStatus() == 0;
        MongoClusterGetOutputEntity data = getResponse.getData();
        assert data != null;
        assert data.getClusterName().equalsIgnoreCase(clusterName);
        assert data.getUserId().equalsIgnoreCase(NEW_USER_ID);
        assert !data.getEnabled();

        // get client config, need add vm option.
        addLocalVmOptions();
//        addFat16VmOptions();
        String content = ConfigUtils.getMongoFileResult(clusterName);
        assert Strings.isNotBlank(content);
        System.out.println("---------------------------mongo cluster client config begin----------------------------");
        System.out.println(content);
        System.out.println("---------------------------mongo cluster client config end----------------------------");
        MongoClusterEntity clientConfig = Utils.gson.fromJson(content, MongoClusterEntity.class);
        assert clientConfig != null;
        assert clientConfig.getClusterName().equalsIgnoreCase(clusterName);
        assert clientConfig.getUserId().equalsIgnoreCase(NEW_USER_ID);
        assert !clientConfig.getEnabled();
    }

    @Test
    public void get() {
        MongoClusterGetResponse response = mongoPluginService.getMongoCluster(MONGO_CLUSTER_NAME, FAT_ENV);
        assert response != null;
        assert response.getStatus() == 0;
        assert response.getData() != null;
    }

    private MongoClusterEntity generateMongoClusterEntity(String clusterName) {

        if (!MONGO_CLUSTER_NAME.equalsIgnoreCase(clusterName)) {
            clusterName = clusterName + "-test";
        }

        Node node = Node.builder()
                .host("bridge.soa.uat.qa.nt.ctripcorp.com")
                .port(65535)
                .build();

        MongoClusterEntity mongoCluster = MongoClusterEntity.builder()
                .clusterName(clusterName)
                .clusterType("REPLICATION")
                .dbName("testDBName")
                .userId("testName")
                .password("qwe123")
                .nodes(Lists.newArrayList(node))
                .enabled(true)
                .version(1)
                .extraProperties(null)
                .build();

        return mongoCluster;
    }

    private void addFat16VmOptions() {
        System.setProperty("qconfig.admin", "qconfig.fat16.qa.nt.ctripcorp.com");
        System.setProperty("qserver.http.urls", "10.5.80.175:8080");
        System.setProperty("qserver.https.urls", "10.5.80.175:8443");
    }

    private void addLocalVmOptions() {
        // get client config from local, need add vm option.
        System.setProperty("qconfig.admin", "localhost:8082");
        System.setProperty("qserver.http.urls", "localhost:8080");
        System.setProperty("qserver.https.urls", "localhost:8443");
    }
}
