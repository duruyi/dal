package com.ctrip.framework.dal.dbconfig.plugin.handler.dal;

import com.ctrip.framework.dal.dbconfig.plugin.config.PluginConfigManager;
import com.ctrip.framework.dal.dbconfig.plugin.constant.DalConstants;
import com.ctrip.framework.dal.dbconfig.plugin.entity.dal.DalClusterEntity;
import com.ctrip.framework.dal.dbconfig.plugin.entity.dal.DalClusterUpdateInputEntity;
import com.ctrip.framework.dal.dbconfig.plugin.entity.dal.DatabaseInfo;
import com.ctrip.framework.dal.dbconfig.plugin.entity.dal.DatabaseShardInfo;
import com.ctrip.framework.dal.dbconfig.plugin.handler.AdminHandler;
import com.ctrip.framework.dal.dbconfig.plugin.util.GsonUtils;
import com.ctrip.framework.dal.dbconfig.plugin.util.MockQconfigService;
import com.google.common.collect.Lists;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.DelegatingServletInputStream;
import qunar.tc.qconfig.plugin.PluginConstant;
import qunar.tc.qconfig.plugin.PluginResult;
import qunar.tc.qconfig.plugin.PluginStatusCode;
import qunar.tc.qconfig.plugin.QconfigService;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;

/**
 * Created by shenjie on 2019/6/28.
 */
public class DalClusterUpdateHandlerTest implements DalConstants {
    private String env = "fat";
    private String subEnv = "fat22";
    private String operator = "testUser";
    private String sslCode = "VZ00000000000441";
    private HttpServletRequest request;
    private AdminHandler handler;

    public DalClusterUpdateHandlerTest() {
        QconfigService service = new MockQconfigService();
        PluginConfigManager pluginConfigManager = PluginConfigManager.getInstance(service);
        handler = new DalClusterUpdateHandler(service, pluginConfigManager);
    }

    @Before
    public void beforeTest() {
        request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getParameter(REQ_PARAM_ENV)).andReturn(env).anyTimes();
        EasyMock.expect(request.getParameter(REQ_PARAM_SUB_ENV)).andReturn(subEnv).anyTimes();
        EasyMock.expect(request.getParameter(REQ_PARAM_OPERATOR)).andReturn(operator).anyTimes();
        EasyMock.expect(request.getParameter(REQ_PARAM_TARGET_APPID)).andReturn(CLUSTER_CONFIG_STORE_APP_ID).anyTimes();

        try {
            DatabaseInfo database = new DatabaseInfo("master", "127.0.0.1", 8080, "demoDbShard01",
                    "user_w", "123456", 1, "sun");
            DatabaseShardInfo databaseShard = new DatabaseShardInfo(0, "masterDomain", "slaveDomain", 8080, 8080,
                    "masterTitanKey", "slaveTitanKey", Lists.newArrayList(database, database));

            DalClusterEntity cluster = new DalClusterEntity("demoCluster", "mysql", 1,
                    Lists.newArrayList(databaseShard, databaseShard));

            DalClusterUpdateInputEntity updateInputEntity = new DalClusterUpdateInputEntity();
            updateInputEntity.setEnv(env);
            updateInputEntity.setData(Lists.newArrayList(cluster));

            String body = GsonUtils.t2Json(updateInputEntity);

            System.out.println("-----------------dal cluster update request begin-----------------");
            System.out.println(body);
            System.out.println("-----------------dal cluster update request end-----------------");

            EasyMock.expect(request.getContentLength()).andReturn(body.length()).anyTimes();
            EasyMock.expect(request.getInputStream())
                    .andReturn(new DelegatingServletInputStream(new ByteArrayInputStream(body.getBytes())))
                    .anyTimes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPreHandle() {
        request.setAttribute(EasyMock.eq(REQ_ATTR_OPERATOR), EasyMock.anyString());
        EasyMock.replay(request);

        PluginResult result = handler.preHandle(request);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getCode(), PluginStatusCode.OK);
    }

    @Test
    public void testPostHandle1() {
        EasyMock.expect(request.getAttribute(REQ_ATTR_OPERATOR)).andReturn(operator).anyTimes();
        EasyMock.expect(request.getAttribute(PluginConstant.REMOTE_IP)).andReturn("127.0.0.1").anyTimes();
        EasyMock.replay(request);

        PluginResult result = handler.postHandle(request);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getCode(), PluginStatusCode.OK);
    }

    @Test
    public void testPostHandle2() {
        EasyMock.expect(request.getAttribute(REQ_ATTR_OPERATOR)).andReturn(operator).anyTimes();
        EasyMock.expect(request.getAttribute(PluginConstant.REMOTE_IP)).andReturn("127.0.0.2").anyTimes();
        EasyMock.replay(request);

        PluginResult result = handler.postHandle(request);
        Assert.assertNotNull(result);
        Assert.assertNotEquals(result.getCode(), PluginStatusCode.OK);
    }
}