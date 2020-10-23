package com.plarpc.test;

import com.plarpc.server.PlaRpcHandlerServerApi;
import com.plarpc.server.PlaRpcHandlerServerImpl;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        Test test = new TestImpl();
        PlaRpcHandlerServerApi<Test> server = new PlaRpcHandlerServerImpl<>(test);
        server.init(5000);
    }
}
