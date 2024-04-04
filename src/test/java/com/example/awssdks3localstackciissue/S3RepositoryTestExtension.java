package com.example.awssdks3localstackciissue;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class S3RepositoryTestExtension
        implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final LocalStackContainerWrapper LOCAL_STACK_CONTAINER =
            LocalStackContainerInitializer.LOCALSTACK_CONTAINER;
    private S3Client s3Client;
    private List<String> buckets;
    private List<S3RepositoryTest.FileToUpload> filesToUpload;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return S3Client.class.equals(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return s3Client;
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        log.info("Entering beforeAll");
        LOCAL_STACK_CONTAINER.start();
        s3Client = S3Client.builder()
                .endpointOverride(LOCAL_STACK_CONTAINER.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        LOCAL_STACK_CONTAINER.getAccessKey(), LOCAL_STACK_CONTAINER.getSecretKey())))
                .region(Region.of(LOCAL_STACK_CONTAINER.getRegion()))
                .build();

        if (extensionContext.getElement().isPresent() &&
                extensionContext.getElement().get().isAnnotationPresent(S3RepositoryTest.class)) {
            buckets = List.of(extensionContext.getElement().get().getAnnotation(S3RepositoryTest.class).buckets());
            filesToUpload =
                    List.of(extensionContext.getElement().get().getAnnotation(S3RepositoryTest.class).filesToUpload());
        }
        log.info("Leaving beforeAll");
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        log.info("Entering beforeEach");
        for (String bucket : getBuckets()) {
            log.info("Creating bucket - {}", bucket);
            s3Client.createBucket(request -> request.bucket(bucket));
            log.info("Created bucket - {}", bucket);
        }
        for (S3RepositoryTest.FileToUpload fileToUpload : filesToUpload) {
            uploadSampleFile(fileToUpload.filePath(), fileToUpload.bucket(), fileToUpload.key());
        }
        log.info("Leaving beforeEach");
    }

    private List<String> getBuckets() {
        Stream<String> fileBuckets = filesToUpload.stream()
                .map(S3RepositoryTest.FileToUpload::bucket);
        return Stream.concat(buckets.stream(), fileBuckets)
                .distinct()
                .toList();
    }

    private void uploadSampleFile(String filePath, String bucket, String key) {
        File file = getSampleFile(filePath);

        log.info("Uploading file - {} to bucket {} as key {}", filePath, bucket, key);
        s3Client.putObject(request -> request.bucket(bucket).key(key),
                RequestBody.fromFile(file));
        log.info("Uploaded file - {} to bucket {} as key {}", filePath, bucket, key);
    }

    private File getSampleFile(String filePath) {
        URL resourceURL = S3RepositoryTestExtension.class.getClassLoader().getResource(filePath);
        return new File(Objects.requireNonNull(resourceURL).getFile());
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        log.info("Entering afterEach");
        for (String bucket : getBuckets()) {
            deleteBucket(bucket);
        }
        log.info("Leaving afterEach");
    }

    private void deleteBucket(String bucket) {
        deleteBucketContents(bucket);
        s3Client.deleteBucket(request -> request.bucket(bucket));
    }

    private void deleteBucketContents(String bucket) {
        ListObjectsResponse objects = s3Client.listObjects(request -> request.bucket(bucket));
        for (S3Object s3Object : objects.contents()) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Object.key())
                    .build();

            log.info("Deleting bucket - {}", bucket);
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted bucket - {}", bucket);
        }
    }
}
