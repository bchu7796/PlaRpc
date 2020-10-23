package com.plarpc.serialization;

import com.google.protobuf.ByteString;

public interface SerializationToolApi {
    public ByteString toString(Object object);
    public Object toObject(ByteString objectByte);
}
