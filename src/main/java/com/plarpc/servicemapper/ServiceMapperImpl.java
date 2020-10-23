package com.plarpc.servicemapper;

import java.util.HashMap;
import java.util.Map;

public class ServiceMapperImpl implements ServiceMapperApi{
    Map<String, String> serviceMap = new HashMap();

    public ServiceMapperImpl() {
        /*
        TODO
         */
        serviceMap.put("com.plarpc.test.Test", "localhost:5000");
    }

    @Override
    public String getLocationByName(String name) {
        return this.serviceMap.get(name);
    }
}
