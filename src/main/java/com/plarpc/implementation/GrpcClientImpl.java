package com.plarpc.implementation;

import com.google.protobuf.ByteString;
import com.plarpc.api.GrpcClientApi;
import com.plarpc.api.SerializationToolApi;
import com.plarpc.grpchandler.GrpcCallerGrpc;
import com.plarpc.grpchandler.ReturnValue;
import com.plarpc.grpchandler.RpcData;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcClientImpl implements GrpcClientApi {
    private static final Logger logger = Logger.getLogger(GrpcClientImpl.class.getName());
    private final ManagedChannel channel;
    private final GrpcCallerGrpc.GrpcCallerBlockingStub blockingStub;

    /**
     * Construct client connecting to service server at {@code host:port}.
     *
     * @param host The host address of the service server.
     * @param port The port of the service server.
     */
    public GrpcClientImpl(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    /**
     * Construct client for accessing service server using the existing channel.
     *
     * @param channel The existing ManagedChannel instance.
     *
     */
    GrpcClientImpl(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GrpcCallerGrpc.newBlockingStub(channel);
    }

    /**
     * Shut down the channel.
     *
     */
    @Override
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Call method in service class.
     *
     * @param methodName The method name we wish to invoke through gRPC.
     * @param arguments The arguments we wish to pass to the server.
     *
     */
    @Override
    public Object callMethod(String methodName, List<ByteString> arguments) {
        RpcData request = RpcData.newBuilder().setMethodName(methodName).
                setNumOfArguments(arguments.size()).
                addAllSerializedArguments(arguments).build();
        ReturnValue response;

        /* Invoke the method through gRPC */
        try {
            response = blockingStub.callMethod(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            throw new RuntimeException(e);
        }

        /* Deserialize the reponse message */
        SerializationToolApi serializationTool = new SerializationToolImpl();
        Object returnObject = null;
        try {
            returnObject = serializationTool.toObject(response.getSerializedReturnValue());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to deserialize objects: ", e);
            throw new RuntimeException(e);
        }

        logger.info("called method: " + methodName + ", return value: " + returnObject);
        return returnObject;
    }
}
