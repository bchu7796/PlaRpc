package com.plarpc.api;

import com.google.protobuf.ByteString;

import java.util.List;

public interface GrpcClientApi {
    public void shutdown() throws InterruptedException;
    public Object callMethod(String methodName, List<ByteString> arguments);
}
