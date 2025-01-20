package com.grpc.test.config;

import com.grpc.test.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SuppressWarnings("unused")
@TestConfiguration
public class GrpcTestConfig {

    @Bean
    public HelloServiceGrpc.HelloServiceBlockingStub serviceStub(ManagedChannel channel) {
        return HelloServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9090)
                                    .usePlaintext()
                                    .build();
    }
}
