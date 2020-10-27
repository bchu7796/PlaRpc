package com.plarpc.test;

import com.plarpc.api.PlaRpcServerApi;
import com.plarpc.implementation.PlaRpcServerImpl;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        Test test = new TestImpl();
        PlaRpcServerApi<Test> server = new PlaRpcServerImpl<Test>(test);
        server.start(5000);
    }
}
