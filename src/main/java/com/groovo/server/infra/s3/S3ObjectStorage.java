package com.groovo.server.infra.s3;

import java.io.InputStream;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3ObjectStorage {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final S3Properties properties;

  public S3ObjectStorage(S3Client s3Client, S3Presigner s3Presigner, S3Properties properties) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.properties = properties;
  }

  public S3PresignedUrl createUploadPresignedUrl(String key, String contentType) {
    PutObjectRequest.Builder objectRequestBuilder =
        PutObjectRequest.builder().bucket(bucket()).key(key);
    if (StringUtils.hasText(contentType)) {
      objectRequestBuilder.contentType(contentType);
    }

    PresignedPutObjectRequest presignedRequest =
        s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(properties.presignedUrlExpiration())
                .putObjectRequest(objectRequestBuilder.build())
                .build());
    return new S3PresignedUrl(
        key,
        presignedRequest.url().toExternalForm(),
        presignedRequest.expiration(),
        presignedRequest.signedHeaders());
  }

  public S3PresignedUrl createDownloadPresignedUrl(String key) {
    PresignedGetObjectRequest presignedRequest =
        s3Presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(properties.presignedUrlExpiration())
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket()).key(key).build())
                .build());
    return new S3PresignedUrl(
        key,
        presignedRequest.url().toExternalForm(),
        presignedRequest.expiration(),
        presignedRequest.signedHeaders());
  }

  public void upload(String key, InputStream inputStream, long contentLength, String contentType) {
    PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder().bucket(bucket()).key(key);
    if (StringUtils.hasText(contentType)) {
      requestBuilder.contentType(contentType);
    }

    s3Client.putObject(
        requestBuilder.build(), RequestBody.fromInputStream(inputStream, contentLength));
  }

  public void delete(String key) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket()).key(key).build());
  }

  public String objectUrl(String key) {
    return s3Client
        .utilities()
        .getUrl(GetUrlRequest.builder().bucket(bucket()).key(key).build())
        .toExternalForm();
  }

  private String bucket() {
    if (!StringUtils.hasText(properties.bucket())) {
      throw new IllegalStateException("AWS S3 bucket is not configured.");
    }
    return properties.bucket();
  }
}
