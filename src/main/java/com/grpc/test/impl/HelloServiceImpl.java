package com.grpc.test.impl;

import com.google.protobuf.Timestamp;
import com.grpc.test.HelloServiceGrpc;
import com.grpc.test.HelloWorldProto;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@GrpcService
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

    private final ConcurrentMap<String, Timestamp> clientLastActive = new ConcurrentHashMap<>();

    /**
     * <pre>
     * Says hello response once to the hello request.
     * </pre>
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void sayHello(HelloWorldProto.HelloRequest request, StreamObserver<HelloWorldProto.HelloResponse> responseObserver) {
        responseObserver.onNext(getHelloResponse(request));
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * Says hello response continuously to hello request.
     * </pre>
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void sayContinuousHello(HelloWorldProto.HelloRequest request, StreamObserver<HelloWorldProto.HelloResponse> responseObserver) {
        IntStream.range(0, 10)
                .sequential()
                .peek(_ -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forEach(_ -> {
                    responseObserver.onNext(getHelloResponse(request));
                });
        responseObserver.onCompleted();
    }

    /**
     * <pre>
     * Client sends continuous requests to server with hello pings
     * </pre>
     *
     * @param responseObserver
     */
    @Override
    public StreamObserver<HelloWorldProto.ClientActiveRequest> clientContinuousHello(StreamObserver<HelloWorldProto.RegisteredResponse> responseObserver) {
        responseObserver.onNext(getRegisteredResponse());
        return new StreamObserver<>() {

            @Override
            public void onNext(HelloWorldProto.ClientActiveRequest value) {
                var clientId = value.getClientId();
                var timestamp = value.getActiveAt();
                clientLastActive.putIfAbsent(clientId, timestamp);
                clientLastActive.put(clientId, timestamp);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * <pre>
     * Bidirectional com system
     * </pre>
     *
     * @param responseObserver
     */
    @Override
    public StreamObserver<HelloWorldProto.ClientActiveRequest> bidirectionalHello(StreamObserver<HelloWorldProto.RegisteredResponse> responseObserver) {
        var state = new AtomicBoolean(true);
        return new StreamObserver<>() {
            @Override
            public void onNext(HelloWorldProto.ClientActiveRequest value) {
                if (state.get()) {
                    responseObserver.onNext(getRegisteredResponse());
                    state.set(false);
                } else {
                    state.set(true);
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private HelloWorldProto.RegisteredResponse getRegisteredResponse() {
        return HelloWorldProto.RegisteredResponse
                .newBuilder()
                .setReferenceId(UUID.randomUUID().toString())
                .build();
    }

    private HelloWorldProto.HelloResponse getHelloResponse(HelloWorldProto.HelloRequest request) {
        return HelloWorldProto.HelloResponse.newBuilder()
                .setMessage("Hello " + request.getName())
                .build();
    }

}
