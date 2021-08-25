package com.ctrip.platform.dal.cluster;

import com.ctrip.platform.dal.cluster.cluster.DefaultClusterTest;
import com.ctrip.platform.dal.cluster.cluster.DefaultLocalConfigProviderTest;
import com.ctrip.platform.dal.cluster.util.CaseInsensitivePropertiesTest;
import com.ctrip.platform.dal.cluster.util.ObjectHolderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by @author zhuYongMing on 2019/11/29.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DefaultClusterTest.class,
        DefaultLocalConfigProviderTest.class,
        ObjectHolderTest.class,
        CaseInsensitivePropertiesTest.class
})
// test: 99/99 passed/all env:fat
public class AllTests {
}
