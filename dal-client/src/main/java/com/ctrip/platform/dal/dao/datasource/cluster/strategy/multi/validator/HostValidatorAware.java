package com.ctrip.platform.dal.dao.datasource.cluster.strategy.multi.validator;

/**
 * @Author limingdong
 * @create 2021/8/19
 */
public interface HostValidatorAware {

    void setHostValidator(HostValidator hostValidator);
}
