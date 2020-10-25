package com.plarpc.client;

import com.google.protobuf.ByteString;
import com.plarpc.grpchandler.GrpcHandlerClient;
import com.plarpc.serialization.SerializationToolApi;
import com.plarpc.serialization.SerializationToolImpl;
import com.plarpc.servicemapper.ServiceMapperApi;
import com.plarpc.servicemapper.ServiceMapperImpl;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaRpcHandlerClientImpl<T> implements PlaRpcHandlerClientApi, InvocationHandler{
    private static final Logger logger = Logger.getLogger(PlaRpcHandlerClientImpl.class.getName());
    private static final ServiceMapperApi serviceMapper = new ServiceMapperImpl();
    private Constructor<?> proxyConstructor;
    private String className;

    /**
     * Initialize the proxy constructor with the class we wish to send the RPC to.
     *
     * @param clazz Class we wish to send the RPC to.
     *
     * @throws RuntimeException
     */
    public PlaRpcHandlerClientImpl(Class<T> clazz) throws RuntimeException {
        this.className = clazz.getName();
        try {
            this.proxyConstructor = Proxy.getProxyClass(clazz.getClassLoader(),
                    new Class[] { clazz }).getConstructor(InvocationHandler.class);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
            logger.log(Level.WARNING, "Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String targetClassName = this.className;

        String address = null;
        try {
            address = serviceMapper.getLocationByName(targetClassName);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Cannot get service by name: ", e);
            throw new RuntimeException(e);
        }

        String host = null;
        int port;
        try {
            String[] splitAddress = address.split(":");
            host = splitAddress[0];
            port = Integer.valueOf(splitAddress[1]);
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.WARNING, "Address format wrong: ", e);
            throw new RuntimeException(e);
        }


        List<ByteString> serializedObjects = new ArrayList<>();
        if(args != null) {
            SerializationToolApi serializationTool = new SerializationToolImpl();
            try {
                for (int i = 0; i < args.length; i++) {
                    serializedObjects.add(serializationTool.toString(args[i]));
                }
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Failed to serialize objects: ", e);
                throw new RuntimeException(e);
            }
        }

        GrpcHandlerClient grpcClient = new GrpcHandlerClient(host, port);
        Object returnObject = null;
        try {
            returnObject = grpcClient.callMethod(method.getName(), serializedObjects);
        } finally {
            grpcClient.shutdown();
        }

        return returnObject;
    }


    /**
     * Build a proxy for the class we wish to RPC to.
     *
     * @return a proxy instance
     *
     * @throws RuntimeException
     */
    @Override
    public T rpc() throws RuntimeException{
        try{
            return (T) proxyConstructor.newInstance(new Object[] {this});
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            logger.info("Exception: " + e);
            throw new RuntimeException(e);
        }
    }
}
