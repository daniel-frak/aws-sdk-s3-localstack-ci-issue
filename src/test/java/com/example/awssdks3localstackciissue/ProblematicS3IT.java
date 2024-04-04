package com.example.awssdks3localstackciissue;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@S3RepositoryTest(filesToUpload = @S3RepositoryTest.FileToUpload
        (bucket = "test-bucket", key = "test-file.json",
                filePath = "s3/s3_sample_file.json"))
class ProblematicS3IT {

    @Test
    void shouldTest(S3Client s3Client) {
        List<S3Object> result = s3Client.listObjects(request -> request.bucket("test-bucket")).contents();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().key()).isEqualTo("test-file.json");
    }
}
