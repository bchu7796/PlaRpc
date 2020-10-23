package com.plarpc.grpchandler;

import com.google.protobuf.ByteString;
import com.plarpc.serialization.SerializationToolApi;
import com.plarpc.serialization.SerializationToolImpl;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcHandlerClient {
    private static final Logger logger = Logger.getLogger(GrpcHandlerClient.class.getName());

    private final ManagedChannel channel;
    private final GrpcCallerGrpc.GrpcCallerBlockingStub blockingStub;

    /**
     * Construct client connecting to HelloWorld server at {@code host:port}.
     */
    public GrpcHandlerClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    GrpcHandlerClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GrpcCallerGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Say hello to server.
     */
    public Object callMethod(String methodName, List<ByteString> arguments) {
        logger.info("Will try to call " + methodName + " ...");

        RpcData request = RpcData.newBuilder().setMethodName(methodName).
                setNumOfArguments(arguments.size()).
                addAllSerializedArguments(arguments).build();
        ReturnValue response;
        try {
            response = blockingStub.callMethod(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return null;
        }
        SerializationToolApi serializationTool = new SerializationToolImpl();
        Object returnObject = serializationTool.toObject(response.getSerializedReturnValue());
        logger.info("Return: " + returnObject);
        return returnObject;
    }
}
