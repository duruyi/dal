package com.ctrip.platform.dal.dao.callByNativeSyntax;

import com.ctrip.platform.dal.dao.BaseSingleUpdateTest;
import com.ctrip.platform.dal.dao.CtripTaskFactory;
import com.ctrip.platform.dal.dao.DalParser;
import com.ctrip.platform.dal.dao.task.SingleTask;

public class SingleUpdateTest extends BaseSingleUpdateTest {
    @Override
    public <T> SingleTask<T> getTest(DalParser<T> parser) {
        return new CtripTaskFactory().createSingleUpdateTask(parser);
    }
}
