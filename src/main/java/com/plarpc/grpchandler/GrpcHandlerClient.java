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
     * Construct client connecting to service server at {@code host:port}.
     */
    public GrpcHandlerClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    /**
     * Construct client for accessing service server using the existing channel.
     */
    GrpcHandlerClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GrpcCallerGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * call method in service class.
     */
    public Object callMethod(String methodName, List<ByteString> arguments) throws RuntimeException {
        RpcData request = RpcData.newBuilder().setMethodName(methodName).
                setNumOfArguments(arguments.size()).
                addAllSerializedArguments(arguments).build();
        ReturnValue response;
        try {
            response = blockingStub.callMethod(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            throw new RuntimeException(e);
        }
        SerializationToolApi serializationTool = new SerializationToolImpl();
        Object returnObject = null;
        try {
            returnObject = serializationTool.toObject(response.getSerializedReturnValue());
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Failed to deserialize objects: ", e);
            throw new RuntimeException(e);
        }

        logger.info("called method: " + methodName + ", return value: " + returnObject);
        return returnObject;
    }
}
