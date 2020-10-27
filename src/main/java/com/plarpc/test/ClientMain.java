package com.plarpc.test;

import com.plarpc.api.PlaRpcClientApi;
import com.plarpc.implementation.PlaRpcClientImpl;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        PlaRpcClientApi<Test> client = new PlaRpcClientImpl<Test>(Test.class, "localhost", 5000);
        client.rpc().hello();
        client.rpc().helloString("world");
        client.rpc().square(10);
        client.rpc().sum(10, 10);
    }
}
