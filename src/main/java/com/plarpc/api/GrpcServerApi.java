package com.plarpc.api;

import java.io.IOException;

public interface GrpcServerApi<T> {
    public void start(int port) throws IOException;
    public void blockUntilShutdown() throws InterruptedException;
}
