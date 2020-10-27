package com.plarpc.api;

import com.google.protobuf.ByteString;

public interface SerializationToolApi {
    public ByteString toString(Object object) throws Exception;
    public Object toObject(ByteString objectByte) throws Exception;
}
