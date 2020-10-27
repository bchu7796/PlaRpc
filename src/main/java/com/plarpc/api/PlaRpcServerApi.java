package com.plarpc.api;

import java.io.IOException;

public interface PlaRpcServerApi<T> {
    public void start(int port) throws IOException, InterruptedException;
}
