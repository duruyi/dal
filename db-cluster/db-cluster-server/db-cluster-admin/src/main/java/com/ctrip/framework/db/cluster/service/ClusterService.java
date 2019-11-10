package com.ctrip.framework.db.cluster.service;

import com.alibaba.fastjson.JSON;
import com.ctrip.framework.db.cluster.entity.ClusterListResponse;
import com.ctrip.framework.db.cluster.entity.ClusterResponse;
import com.ctrip.framework.db.cluster.entity.HttpMethod;
import com.ctrip.framework.db.cluster.utils.EnvUtil;
import com.ctrip.framework.db.cluster.utils.HttpUtil;
import com.dianping.cat.Cat;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by taochen on 2019/11/6.
 */
@Service
public class ClusterService {
    private static final String DB_CLUSTER_GET_CLUSTERS_FAT = "http://service.dbcluster.fat-1.qa.nt.ctripcorp.com/api/dal/v1/clusters/?operator=%s&effective=%s";

    private static final String DB_CLUSTER_GET_CLUSTERS_UAT = "http://service.dbcluster.uat.qa.nt.ctripcorp.com/api/dal/v1/clusters/?operator=%s&effective=%s";

    private static final String DB_CLUSTER_GET_CLUSTERS_PRO = "http://service.dbcluster.ctripcorp.com/api/dal/v1/clusters/?operator=%s&effective=%s";

    private static final String ADMIN_USER = "chentao";
    public ClusterResponse getCluster() {
        String clusters = "{\n" +
                "    \"status\": 200,\n" +
                "    \"message\": \"Query cluster success\",\n" +
                "    \"result\": {\n" +
                "        \"clusterName\": \"mockcluster1\",\n" +
                "        \"dbCategory\": \"mysql\",\n" +
                "        \"enabled\": true,\n" +
                "        \"zones\": [\n" +
                "            {\n" +
                "                \"zoneId\": \"shajq\",\n" +
                "                \"enabled\": true,\n" +
                "                \"shards\": [\n" +
                "                    {\n" +
                "                        \"shardIndex\": 0,\n" +
                "                        \"dbName\": \"mockdb00\",\n" +
                "                        \"master\": {\n" +
                "                            \"domain\": \"switchedmasterdomain\",\n" +
                "                            \"port\": 12345,\n" +
                "                            \"instance\": {\n" +
                "                                \"ip\": \"100.100.100.100\",\n" +
                "                                \"port\": 11223,\n" +
                "                                \"readWeight\": 1,\n" +
                "                                \"tags\": \"\",\n" +
                "                                \"memberStatus\": true,\n" +
                "                                \"healthStatus\": true\n" +
                "                            },\n" +
                "                            \"instances\": null\n" +
                "                        },\n" +
                "                        \"slave\": {\n" +
                "                            \"domain\": \"mockSlaveDomain\",\n" +
                "                            \"port\": 12345,\n" +
                "                            \"instance\": null,\n" +
                "                            \"instances\": [\n" +
                "                                {\n" +
                "                                    \"ip\": \"10.2.2.2\",\n" +
                "                                    \"port\": 12345,\n" +
                "                                    \"readWeight\": 1,\n" +
                "                                    \"tags\": \"\",\n" +
                "                                    \"memberStatus\": true,\n" +
                "                                    \"healthStatus\": true\n" +
                "                                }\n" +
                "                            ]\n" +
                "                        },\n" +
                "                        \"read\": {\n" +
                "                            \"domain\": \"mockReadDomain\",\n" +
                "                            \"port\": 12345,\n" +
                "                            \"instance\": null,\n" +
                "                            \"instances\": [\n" +
                "                                {\n" +
                "                                    \"ip\": \"10.3.3.3\",\n" +
                "                                    \"port\": 12345,\n" +
                "                                    \"readWeight\": 1,\n" +
                "                                    \"tags\": \"\",\n" +
                "                                    \"memberStatus\": true,\n" +
                "                                    \"healthStatus\": true\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"ip\": \"10.4.4.4\",\n" +
                "                                    \"port\": 12345,\n" +
                "                                    \"readWeight\": 1,\n" +
                "                                    \"tags\": \"\",\n" +
                "                                    \"memberStatus\": true,\n" +
                "                                    \"healthStatus\": true\n" +
                "                                }\n" +
                "                            ]\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        ClusterResponse clusterResponse = JSON.parseObject(clusters, ClusterResponse.class);
        return clusterResponse;
    }

    public ClusterListResponse getAllClusters() {
        //String clusters = "{ \"status\": 200, \"message\": \"Query cluster success\", \"result\": [\"cluster1\", \"cluster2\", \"cluster3\"]}";
        String env = EnvUtil.getEnv();
        String url = "FAT".equalsIgnoreCase(env) ? DB_CLUSTER_GET_CLUSTERS_FAT : "UAT".equalsIgnoreCase(env) ?
                DB_CLUSTER_GET_CLUSTERS_UAT : DB_CLUSTER_GET_CLUSTERS_PRO;
        String formatUrl = String.format(url, ADMIN_USER, "true");
        ClusterListResponse clusterListResponse = null;
        try {
            Map<String, String> parameters = new HashMap<>();
            clusterListResponse = HttpUtil.getJSONEntity(ClusterListResponse.class, formatUrl, parameters, HttpMethod.HttpGet);
        } catch (Exception e) {
            Cat.logError("get all cluster fail!", e);
        }
        //ClusterListResponse clusterListResponse = JSON.parseObject(clusters, ClusterListResponse.class);
        return clusterListResponse;
    }
}
