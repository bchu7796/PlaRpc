package com.plarpc.grpchandler;

import com.google.protobuf.ByteString;
import com.plarpc.serialization.SerializationToolApi;
import com.plarpc.serialization.SerializationToolImpl;
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
public class GrpcHandlerServer<T> {
    T object = null;

    private static final Logger logger = Logger.getLogger(GrpcHandlerServer.class.getName());

    private Server server;

    public GrpcHandlerServer(T object) {
        this.object = object;
    }

    public void start(int port) throws IOException {
        /* The port on which the server should run */
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
                GrpcHandlerServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    class GrpcCallerImpl extends GrpcCallerGrpc.GrpcCallerImplBase {
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
            } catch (RuntimeException e) {
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

            // Return
            ByteString serializedReturnValue = null;
            try {
                serializedReturnValue = serializationTool.toString(returnValue);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Failed to serialize objects: ", e);
            }

            ReturnValue reply = ReturnValue.newBuilder().build().newBuilder().
                    setSerializedReturnValue(serializedReturnValue).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
