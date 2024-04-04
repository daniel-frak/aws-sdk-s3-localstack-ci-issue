package com.example.awssdks3localstackciissue;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.localstack.LocalStackContainer;

/**
 * Based on:
 * https://stackoverflow.com/a/68890310
 */
public class LocalStackContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final LocalStackContainerWrapper LOCALSTACK_CONTAINER = LocalStackContainerWrapper.get();

    public LocalStackContainerInitializer() {
        LOCALSTACK_CONTAINER.withServices(LocalStackContainer.Service.SQS)
                .start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        LOCALSTACK_CONTAINER.setSqsProperties(applicationContext);
    }
}
