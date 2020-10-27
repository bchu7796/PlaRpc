package com.plarpc.implementation;

import com.google.protobuf.ByteString;
import com.plarpc.api.SerializationToolApi;

import java.io.*;
import java.util.logging.Logger;

public class SerializationToolImpl implements SerializationToolApi {
    private static final Logger logger = Logger.getLogger(SerializationToolImpl.class.getName());

    @Override
    public ByteString toString(Object object) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
        } finally {
            bos.close();
        }
        byte[] objectBytes = bos.toByteArray();
        return ByteString.copyFrom(objectBytes);
    }

    @Override
    public Object toObject(ByteString objectByte) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(objectByte.toByteArray());
        ObjectInput in = null;
        Object obj = null;
        try {
            in = new ObjectInputStream(bis);
            obj = in.readObject();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return obj;
    }
}
