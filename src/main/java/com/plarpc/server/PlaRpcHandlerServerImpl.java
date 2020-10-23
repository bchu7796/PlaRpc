package com.plarpc.server;

import com.plarpc.client.PlaRpcHandlerClientImpl;
import com.plarpc.grpchandler.GrpcHandlerServer;

import java.io.IOException;
import java.util.logging.Logger;

public class PlaRpcHandlerServerImpl<T> implements PlaRpcHandlerServerApi{
    private static final Logger logger = Logger.getLogger(PlaRpcHandlerClientImpl.class.getName());
    private T object;

    public PlaRpcHandlerServerImpl(T object) {
        this.object = object;
    }

    @Override
    public void init(int port) throws IOException, InterruptedException{
        GrpcHandlerServer<T> grpcHandlerServer = new GrpcHandlerServer(this.object);
        grpcHandlerServer.start(port);
        grpcHandlerServer.blockUntilShutdown();
    }
}
