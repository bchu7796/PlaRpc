package com.plarpc.implementation;

import com.google.protobuf.ByteString;
import com.plarpc.api.GrpcServerApi;
import com.plarpc.api.SerializationToolApi;
import com.plarpc.grpchandler.GrpcCallerGrpc;
import com.plarpc.grpchandler.ReturnValue;
import com.plarpc.grpchandler.RpcData;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class GrpcServerImpl<T> implements GrpcServerApi {
    private T object = null;
    private static final Logger logger = Logger.getLogger(GrpcServerImpl.class.getName());
    private Server server;

    /**
     * Construct GrpcServerImpl instance with target service object.
     *
     * @param object Target service class instance
     *
     */
    public GrpcServerImpl(T object) {
        this.object = object;
    }

    /**
     * Start the gRPC server.
     *
     * @param port The port on which the server should run.
     *
     * @throws IOException
     */
    @Override
    public void start(int port) throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new GrpcCallerImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcServerImpl.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    /**
     * Stop the server
     *
     */
    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     *
     */
    @Override
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    class GrpcCallerImpl extends GrpcCallerGrpc.GrpcCallerImplBase {
        /**
         * The implementation of gRPC service.
         *
         * @param req request from the client
         * @param responseObserver
         */
        @Override
        public void callMethod(RpcData req, StreamObserver<ReturnValue> responseObserver) {
            /* Extract the RPC data from req */
            String methodName = req.getMethodName();
            int numOfArguments = req.getNumOfArguments();
            Object[] objects = new Object[numOfArguments];
            Class[] classes = new Class[numOfArguments];
            SerializationToolApi serializationTool = new SerializationToolImpl();

            try {
                for (int i = 0; i < numOfArguments; i++) {
                    objects[i] = serializationTool.toObject(req.getSerializedArguments(i));
                    classes[i] = objects[i].getClass();
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to deserialize objects: ", e);
            }


            /* Invoke method */
            Method method = null;
            try {
                method = object.getClass().
                        getMethod(methodName, classes);
            } catch (NoSuchMethodException e) {
                logger.log(Level.WARNING, "Target class do not contain method: ", e);
            }

            Object returnValue = null;
            try {
                returnValue = method.invoke(object, objects);
                logger.info(methodName + "returns: " + returnValue);
            } catch (IllegalArgumentException | IllegalAccessException |
                    InvocationTargetException e) {
                logger.log(Level.WARNING, "Fail to invoke method: ", e);
            }

            /* Response */
            ByteString serializedReturnValue = null;
            try {
                serializedReturnValue = serializationTool.toString(returnValue);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to serialize objects: ", e);
            }

            ReturnValue reply = ReturnValue.newBuilder().build().newBuilder().
                    setSerializedReturnValue(serializedReturnValue).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
