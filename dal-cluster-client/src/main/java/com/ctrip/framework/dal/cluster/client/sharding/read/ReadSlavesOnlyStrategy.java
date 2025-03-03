package com.ctrip.framework.dal.cluster.client.sharding.read;

import com.ctrip.framework.dal.cluster.client.base.HostSpec;
import com.ctrip.framework.dal.cluster.client.exception.HostNotExpectedException;

import java.util.Map;
import java.util.Set;

public class ReadSlavesOnlyStrategy extends ReadSlavesFirstStrategy {
    @Override
    public void init(Set<HostSpec> hostSpecs) {

    }

    @Override
    public HostSpec pickRead(Map<String, Object> map) throws HostNotExpectedException {
        if (map.get(routeStrategy) != null)
            return dalHintsRoute(map);

        return slaveOnly();
    }

    @Override
    public void onChange(Set<HostSpec> hostSpecs) {

    }

    @Override
    public void dispose() {

    }
}
