package com.example.awssdks3localstackciissue;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

public class LocalStackContainerWrapper extends LocalStackContainer {

    private static final String IMAGE_VERSION = "localstack/localstack:3.2.0";
    private static final String SQS_ENDPOINT = "spring.cloud.aws.sqs.endpoint";
    private static final String AWS_REGION = "spring.cloud.aws.region.static";

    public LocalStackContainerWrapper(DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    public static LocalStackContainerWrapper get() {
        DockerImageName imageName = DockerImageName.parse(IMAGE_VERSION)
                .asCompatibleSubstituteFor("localstack/localstack");
        return new LocalStackContainerWrapper(imageName);
    }

    public void setSqsProperties(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                SQS_ENDPOINT + "=" + getEndpointOverride(Service.SQS),
                AWS_REGION + "=" + getRegion()
        ).applyTo(applicationContext.getEnvironment());
    }
}
