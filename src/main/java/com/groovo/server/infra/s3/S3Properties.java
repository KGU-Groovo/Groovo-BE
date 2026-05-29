package com.groovo.server.infra.s3;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
	String bucket,
	String region,
	String endpoint,
	boolean pathStyleAccessEnabled,
	String accessKey,
	String secretKey,
	Duration presignedUrlExpiration
) {

	private static final String DEFAULT_REGION = "ap-northeast-2";
	private static final Duration DEFAULT_PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(5);

	public S3Properties {
		if (region == null || region.isBlank()) {
			region = DEFAULT_REGION;
		}
		if (presignedUrlExpiration == null) {
			presignedUrlExpiration = DEFAULT_PRESIGNED_URL_EXPIRATION;
		}
	}
}
