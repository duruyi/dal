package com.ctrip.framework.dal.dbconfig.plugin;

import com.ctrip.framework.dal.dbconfig.plugin.context.EnvProfile;
import com.ctrip.framework.dal.dbconfig.plugin.entity.mongo.MongoClusterEntity;
import com.ctrip.framework.dal.dbconfig.plugin.entity.mongo.Node;
import com.ctrip.framework.dal.dbconfig.plugin.util.CommonHelper;
import com.ctrip.framework.dal.dbconfig.plugin.util.GsonUtils;
import com.ctrip.framework.dal.dbconfig.plugin.util.MockQconfigService;
import com.google.common.collect.Lists;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.plugin.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.ctrip.framework.dal.dbconfig.plugin.constant.MongoConstants.*;
import static com.ctrip.framework.dal.dbconfig.plugin.constant.TitanConstants.HEADER_NET_TYPE;
import static com.ctrip.framework.dal.dbconfig.plugin.constant.TitanConstants.PRIVATE_NET_TYPE;


/**
 * Created by shenjie on 2019/4/4.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoServerPlugin.class})
public class MongoServerPluginTest {

    private MongoServerPlugin mongoServerPlugin = new MongoServerPlugin();
    private HttpServletRequest request;
    private String clusterName = "diuserprofile-diuserprofiledb";
    private String env = "fat";
    private String proEnv = "pro";
    private String awsProfile = "pro:fra-aws";
    private String privateNetIp = "10.5.156.193";

    @Before
    public void init() throws Exception {
        QconfigService qconfigService = new MockQconfigService();
        mongoServerPlugin.init(qconfigService);

        //创建request的Mock
        request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getParameter(Constants.GROUP_NAME)).andReturn(MONGO_CLIENT_APP_ID).anyTimes();

        initForGetConfig();
//        initForForceLoad();
    }


    @Test
    public void preHandle() throws Exception {
        request.setAttribute(EasyMock.eq(REQ_ATTR_CLUSTER_NAME), EasyMock.anyString());
        request.setAttribute(EasyMock.eq(REQ_ATTR_ENV_PROFILE), EasyMock.anyString());
        EasyMock.expect(request.getScheme()).andReturn(REQUEST_SCHEMA_HTTPS).anyTimes();
        EasyMock.replay(request);   //保存期望结果

        String groupId = MONGO_CLIENT_APP_ID;
        String dataId = clusterName;
        String profile = "fat:LPT10";
        ConfigDetail configDetail = new ConfigDetail(groupId, dataId, profile);
        WrappedRequest wrappedRequest = new WrappedRequest(request, configDetail);
        PluginResult pluginResult = mongoServerPlugin.preHandle(wrappedRequest);
        assert (pluginResult != null);
        System.out.println("pluginResult.code=" + pluginResult.getCode() + ", pluginResult.message=" + pluginResult.getMessage());
        assert (pluginResult.getCode() == PluginStatusCode.OK);
    }

    @Test
    public void postHandle() throws Exception {
        String profile = CommonHelper.formatProfileFromEnv(env);
        EasyMock.expect(request.getAttribute(REQ_ATTR_ENV_PROFILE)).andReturn(new EnvProfile(env)).anyTimes();
        EasyMock.expect(request.getHeader("X-Forwarded-For")).andReturn("10.5.1.174").anyTimes();
        EasyMock.replay(request);   //保存期望结果
        EasyMock.verify(request);

        String groupId = MONGO_CLIENT_APP_ID;
        String dataId = clusterName;
        //String profile = "fat:LPT10";
        long version = 1L;
        String content = buildMongoClusterContent(true);
        ConfigDetail configDetail = new ConfigDetail(groupId, dataId, profile, version, content);
        WrappedRequest wrappedRequest = new WrappedRequest(request, configDetail);
        PluginResult pluginResult = mongoServerPlugin.postHandle(wrappedRequest);
        assert (pluginResult != null);
        System.out.println("pluginResult.code=" + pluginResult.getCode() + ", pluginResult.message=" + pluginResult.getMessage());
        //assert(pluginResult.getAttribute() != null);
        System.out.println("pluginResult.attribute=" + GsonUtils.Object2Json(pluginResult.getAttribute()));
    }

    @Test
    public void postHandle2() throws Exception {
        String profile = CommonHelper.formatProfileFromEnv(env);
        EasyMock.expect(request.getAttribute(REQ_ATTR_ENV_PROFILE)).andReturn(new EnvProfile(env)).anyTimes();
        EasyMock.expect(request.getHeader("X-Forwarded-For")).andReturn("10.5.1.174").anyTimes();
        EasyMock.replay(request);   //保存期望结果
        EasyMock.verify(request);

        String groupId = MONGO_CLIENT_APP_ID;
        String dataId = clusterName;
        //String profile = "fat:LPT10";
        long version = 1L;
        String content = buildMongoClusterContent(false);
        ConfigDetail configDetail = new ConfigDetail(groupId, dataId, profile, version, content);
        WrappedRequest wrappedRequest = new WrappedRequest(request, configDetail);
        PluginResult pluginResult = mongoServerPlugin.postHandle(wrappedRequest);
        assert (pluginResult != null);
        assert pluginResult.getCode() != 0;
        System.out.println("pluginResult.code=" + pluginResult.getCode() + ", pluginResult.message=" + pluginResult.getMessage());
        //assert(pluginResult.getAttribute() != null);
        System.out.println("pluginResult.attribute=" + GsonUtils.Object2Json(pluginResult.getAttribute()));
    }

    //init for <>
    private void initForGetConfig() {
        EasyMock.expect(request.getRequestURI()).andReturn("/client/getconfigv2").anyTimes();
        EasyMock.expect(request.getMethod()).andReturn("GET").anyTimes();
    }

    //init for <>
    private void initForForceLoad() {
        EasyMock.expect(request.getRequestURI()).andReturn("/client/forceloadv2").anyTimes();
        EasyMock.expect(request.getMethod()).andReturn("GET").anyTimes();
    }

    //build test content
    private String buildMongoClusterContent(boolean enabled) {
        MongoClusterEntity mongoCluster = new MongoClusterEntity();
        Node node = new Node("mongo.node.host", 55944);

        mongoCluster.setNodes(Lists.newArrayList(node, node, node));
        mongoCluster.setClusterName(clusterName);
        mongoCluster.setClusterType("sharding");
        mongoCluster.setDbName("mongoDBName");
        mongoCluster.setUserId("35CC911241C1F1DD3241DA6FCB4B1A56");
        mongoCluster.setPassword("B66C59EC4E2A996F781594974B191279");
        mongoCluster.setVersion(1);
        mongoCluster.setOperator("testUser");
        mongoCluster.setSslCode("VZ00000000000441");
        mongoCluster.setEnabled(enabled);
        mongoCluster.setUpdateTime(new Date());

        String content = GsonUtils.t2Json(mongoCluster);

        return content;
    }

    @Test
    public void postHandleAwsTest() throws Exception {
        String profile = proEnv;
        EasyMock.expect(request.getAttribute(REQ_ATTR_ENV_PROFILE)).andReturn(new EnvProfile(awsProfile)).anyTimes();
        EasyMock.expect(request.getHeader("X-Forwarded-For")).andReturn(privateNetIp).anyTimes();
        EasyMock.expect(request.getHeader(HEADER_NET_TYPE)).andReturn(PRIVATE_NET_TYPE).anyTimes();
        EasyMock.replay(request);   //保存期望结果
        EasyMock.verify(request);

        String groupId = MONGO_CLIENT_APP_ID;
        String dataId = clusterName;
        //String profile = "fat:LPT10";
        long version = 1L;
        String content = buildMongoClusterContent(false);
        ConfigDetail configDetail = new ConfigDetail(groupId, dataId, profile, version, content);
        WrappedRequest wrappedRequest = new WrappedRequest(request, configDetail);
        PluginResult pluginResult = mongoServerPlugin.postHandle(wrappedRequest);
        assert (pluginResult != null);
        System.out.println("pluginResult.code=" + pluginResult.getCode() + ", pluginResult.message=" + pluginResult.getMessage());
        assert pluginResult.getCode() != PluginStatusCode.OK;
        System.out.println("pluginResult.message=" + pluginResult.getMessage());
    }

    @Test
    public void postHandleLptTest() throws Exception {
        String profile = "fat";
        EasyMock.expect(request.getAttribute(REQ_ATTR_ENV_PROFILE)).andReturn(new EnvProfile("fat:lpt")).anyTimes();
        EasyMock.expect(request.getHeader("X-Forwarded-For")).andReturn(privateNetIp).anyTimes();
        EasyMock.expect(request.getHeader(HEADER_NET_TYPE)).andReturn(PRIVATE_NET_TYPE).anyTimes();
        EasyMock.replay(request);   //保存期望结果
        EasyMock.verify(request);

        String groupId = MONGO_CLIENT_APP_ID;
        String dataId = clusterName;
        //String profile = "fat:LPT10";
        long version = 1L;
        String content = buildMongoClusterContent(false);
        ConfigDetail configDetail = new ConfigDetail(groupId, dataId, profile, version, content);
        WrappedRequest wrappedRequest = new WrappedRequest(request, configDetail);
        PluginResult pluginResult = mongoServerPlugin.postHandle(wrappedRequest);
        assert (pluginResult != null);
        System.out.println("pluginResult.code=" + pluginResult.getCode() + ", pluginResult.message=" + pluginResult.getMessage());
        assert pluginResult.getCode() != PluginStatusCode.OK;
        System.out.println("pluginResult.message=" + pluginResult.getMessage());
    }

    @Test
    public void registerPoints() throws Exception {
    }

}