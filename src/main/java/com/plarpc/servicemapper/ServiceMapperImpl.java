package com.plarpc.servicemapper;

import java.util.HashMap;
import java.util.Map;

public class ServiceMapperImpl implements ServiceMapperApi{
    Map<String, String> serviceMap = null;

    public ServiceMapperImpl() {
        /*
        TODO
         */
        this.serviceMap = new HashMap<>();
        this.serviceMap.put("com.plarpc.test.Test", "localhost:5000");
    }

    @Override
    public String getLocationByName(String name) throws RuntimeException {
        String address = this.serviceMap.get(name);
        if(address == null) {
            throw new RuntimeException("Cannot find service class");
        }
        return address;
    }
}
