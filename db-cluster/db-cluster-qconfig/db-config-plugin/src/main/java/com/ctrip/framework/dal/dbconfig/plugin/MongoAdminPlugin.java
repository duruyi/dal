package com.ctrip.framework.dal.dbconfig.plugin;

import com.ctrip.framework.dal.dbconfig.plugin.handler.AdminHandler;
import com.ctrip.framework.dal.dbconfig.plugin.handler.AdminHandlerDispatcher;
import com.ctrip.framework.dal.dbconfig.plugin.handler.mongo.MongoClusterPostHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import qunar.tc.qconfig.plugin.PluginRegisterPoint;
import qunar.tc.qconfig.plugin.PluginResult;
import qunar.tc.qconfig.plugin.PluginStatusCode;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by shenjie on 2019/4/3.
 */
public class MongoAdminPlugin extends AdminPluginAdapter {

    private AdminHandlerDispatcher dispatcher;

    @Override
    public void init() {
        dispatcher = new AdminHandlerDispatcher();
        List<AdminHandler> adminHandlers = Lists.newArrayList();
        adminHandlers.add(new MongoClusterPostHandler(getQconfigService()));

        for (AdminHandler handler : adminHandlers) {
            dispatcher.register(handler);
        }
    }

    @Override
    public PluginResult preHandle(HttpServletRequest request) {
        PluginResult pluginResult = null;
        try {
            AdminHandler handler = dispatcher.getHandler(request);
            if (handler != null) {
                pluginResult = handler.preHandle(request);
            } else {
                String requestUri = request.getRequestURI();
                pluginResult = new PluginResult(PluginStatusCode.TITAN_ILLEGAL_REQUEST, "preHandle(): not find process handler, requestUri=" + requestUri);
            }
        } catch (Exception e) {
            pluginResult = new PluginResult(PluginStatusCode.TITAN_NOT_DEFINED, "preHandle(): handler error. " + e.getMessage());
        }
        return pluginResult;
    }

    @Override
    public PluginResult postHandle(HttpServletRequest request) {
        PluginResult pluginResult = null;
        try {
            AdminHandler handler = dispatcher.getHandler(request);
            if (handler != null) {
                pluginResult = handler.postHandle(request);
            } else {
                String requestUri = request.getRequestURI();
                pluginResult = new PluginResult(PluginStatusCode.TITAN_ILLEGAL_REQUEST, "postHandle(): not find process handler, requestUri=" + requestUri);
            }
        } catch (Exception e) {
            pluginResult = new PluginResult(PluginStatusCode.TITAN_NOT_DEFINED, "postHandle(): handler error. " + e.getMessage());
        }
        return pluginResult;
    }


    @Override
    public List<PluginRegisterPoint> registerPoints() {
        List<PluginRegisterPoint> registerPointList = new ImmutableList.Builder<PluginRegisterPoint>()
                .add(PluginRegisterPoint.ADM_TITAN_GET)
                .add(PluginRegisterPoint.ADM_TITAN_POST)
                .add(PluginRegisterPoint.ADM_TITAN_PUT)
                .add(PluginRegisterPoint.ADM_TITAN_DELETE)
                .build();
        return registerPointList;
    }
}
