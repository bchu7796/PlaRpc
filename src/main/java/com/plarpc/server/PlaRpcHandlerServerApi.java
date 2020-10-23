package com.plarpc.server;

import java.io.IOException;

public interface PlaRpcHandlerServerApi<T> {
    public void init(int port) throws IOException, InterruptedException;
}
