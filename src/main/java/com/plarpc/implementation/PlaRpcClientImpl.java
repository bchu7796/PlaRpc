package com.plarpc.implementation;

import com.google.protobuf.ByteString;
import com.plarpc.api.GrpcClientApi;
import com.plarpc.api.PlaRpcClientApi;
import com.plarpc.api.SerializationToolApi;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaRpcClientImpl<T> implements PlaRpcClientApi, InvocationHandler{
    private static final Logger logger = Logger.getLogger(PlaRpcClientImpl.class.getName());
    private Constructor<?> proxyConstructor;
    private String className;
    private String host;
    private int port;

    /**
     * Initialize the proxy constructor with the class we wish to send the RPC to.
     *
     * @param clazz Class we wish to send the RPC to.
     *
     */
    public PlaRpcClientImpl(Class<T> clazz, String host, int port) {
        this.className = clazz.getName();
        this.host = host;
        this.port = port;
        try {
            this.proxyConstructor = Proxy.getProxyClass(clazz.getClassLoader(),
                    new Class[] { clazz }).getConstructor(InvocationHandler.class);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Cannot create PlaRpcHandlerClientImpl instance: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Things to do when the target class method is invoked.
     *
     * @param proxy Proxy class instance.
     * @param method The method that is invoked.
     * @param args The arguments that is passed.
     *
     * @throws Throwable When serializing arguments or invoke method
     *                   through gRPC.
     *
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String targetClassName = this.className;

        /* Serialize arguments */
        List<ByteString> serializedObjects = new ArrayList<>();
        if(args != null) {
            SerializationToolApi serializationTool = new SerializationToolImpl();
            try {
                for (int i = 0; i < args.length; i++) {
                    serializedObjects.add(serializationTool.toString(args[i]));
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to serialize objects: ", e);
                throw new RuntimeException(e);
            }
        }

        /* Invoke the method through gRPC */
        GrpcClientApi grpcClient = new GrpcClientImpl(this.host, this.port);
        Object returnObject = null;
        try {
            returnObject = grpcClient.callMethod(method.getName(), serializedObjects);
        } catch (Exception e){
            logger.log(Level.WARNING, "Failed to invoke gRPC method: ", e);
            throw new RuntimeException(e);
        } finally{
            grpcClient.shutdown();
        }

        return returnObject;
    }


    /**
     * Build a proxy for the class we wish to RPC to.
     *
     * @return a proxy instance
     *
     */
    @Override
    public T rpc() {
        try{
            return (T) proxyConstructor.newInstance(new Object[] {this});
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            logger.log(Level.WARNING, "Cannot create proxy instance: ", e);
            throw new RuntimeException(e);
        }
    }
}
