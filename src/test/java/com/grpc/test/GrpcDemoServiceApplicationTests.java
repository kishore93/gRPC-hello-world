package com.grpc.test;

import com.grpc.test.config.GrpcTestConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.stream.Stream;

@SpringBootTest
@Import(GrpcTestConfig.class)
public class GrpcDemoServiceApplicationTests {

	@Autowired
	HelloServiceGrpc.HelloServiceBlockingStub serviceStub;

	public static Stream<String> unaryEndpointTestDataProvider() {
		return Stream.of(
				"Name 1", "Name 2", "Name 3"
		);
	}

	private HelloWorldProto.HelloRequest getHelloRequest(String name) {
		return HelloWorldProto.HelloRequest.newBuilder()
				.setName(name)
				.build();
	}

	@ParameterizedTest
	@MethodSource("unaryEndpointTestDataProvider")
	void testUnaryHelloWorld(String name) {
		HelloWorldProto.HelloResponse helloResponse = serviceStub.sayHello(getHelloRequest(name));
		Assertions.assertThat(helloResponse)
				.describedAs("Response should have name that is requested")
				.extracting(HelloWorldProto.HelloResponse::getMessage)
				.isEqualTo("Hello " + name);
	}

}
