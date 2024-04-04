package com.example.awssdks3localstackciissue;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(S3RepositoryTestExtension.class)
public @interface S3RepositoryTest {

    String[] buckets() default {};

    FileToUpload[] filesToUpload() default {};

    @interface FileToUpload {
        String bucket();

        String key();

        String filePath();
    }
}
