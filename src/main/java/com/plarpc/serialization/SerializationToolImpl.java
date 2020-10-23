package com.plarpc.serialization;

import com.google.protobuf.ByteString;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerializationToolImpl implements SerializationToolApi {
    private static final Logger logger = Logger.getLogger(SerializationToolImpl.class.getName());

    @Override
    public ByteString toString(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
        } catch (IOException e){
            logger.log(Level.WARNING, "Serialization failed: {0}", e);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                // ignore close exception
            }
        }
        byte[] objectBytes = bos.toByteArray();
        return ByteString.copyFrom(objectBytes);
    }

    @Override
    public Object toObject(ByteString objectByte) {
        ByteArrayInputStream bis = new ByteArrayInputStream(objectByte.toByteArray());
        ObjectInput in = null;
        Object obj = null;
        try {
            in = new ObjectInputStream(bis);
            obj = in.readObject();
        } catch (IOException | ClassNotFoundException e){
            logger.log(Level.WARNING, "Serialization failed: {0}", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return obj;
    }
}
