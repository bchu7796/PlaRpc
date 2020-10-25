package com.plarpc.server;

import java.io.IOException;

public interface PlaRpcHandlerServerApi<T> {
    public void start(int port) throws IOException, InterruptedException;
}
