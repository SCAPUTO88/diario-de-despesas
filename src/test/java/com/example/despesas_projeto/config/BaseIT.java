package com.example.despesas_projeto.config;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@Testcontainers
public class BaseIT {

    @Container
    protected static final LocalStackContainer LOCAL_STACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:1.4.0")
    )
            .withServices(LocalStackContainer.Service.DYNAMODB)
            .withExposedPorts(4566);

    @DynamicPropertySource
    static void overrideConfiguration(DynamicPropertyRegistry registry) {
        registry.add("aws.dynamodb.endpoint",
                () -> "http://" + LOCAL_STACK.getHost() + ":" + LOCAL_STACK.getMappedPort(4566));
        registry.add("aws.region", () -> LOCAL_STACK.getRegion());
        registry.add("aws.access.key", () -> "test");
        registry.add("aws.secret.key", () -> "test");
    }
}