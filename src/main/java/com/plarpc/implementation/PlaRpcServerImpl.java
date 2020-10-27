package com.plarpc.implementation;

import com.plarpc.api.GrpcServerApi;
import com.plarpc.api.PlaRpcServerApi;

import java.io.IOException;
import java.util.logging.Logger;

public class PlaRpcServerImpl<T> implements PlaRpcServerApi{
    private static final Logger logger = Logger.getLogger(PlaRpcServerImpl.class.getName());
    private T object;

    public PlaRpcServerImpl(T object) {
        this.object = object;
    }

    @Override
    public void start(int port) throws IOException, InterruptedException{
        GrpcServerApi<T> grpcHandlerServer = new GrpcServerImpl<T>(this.object);
        grpcHandlerServer.start(port);
        grpcHandlerServer.blockUntilShutdown();
    }
}
