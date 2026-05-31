package com.groovo.server.config;

import com.groovo.server.infra.s3.S3Properties;
import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

	@Bean
	S3Client s3Client(S3Properties properties) {
		S3ClientBuilder builder = S3Client.builder()
			.region(Region.of(properties.region()))
			.credentialsProvider(credentialsProvider(properties));

		if (StringUtils.hasText(properties.endpoint())) {
			builder.endpointOverride(URI.create(properties.endpoint()));
		}
		if (properties.pathStyleAccessEnabled()) {
			builder.serviceConfiguration(S3Configuration.builder()
				.pathStyleAccessEnabled(true)
				.build());
		}
		return builder.build();
	}

	@Bean
	S3Presigner s3Presigner(S3Properties properties) {
		S3Presigner.Builder builder = S3Presigner.builder()
			.region(Region.of(properties.region()))
			.credentialsProvider(credentialsProvider(properties));

		if (StringUtils.hasText(properties.endpoint())) {
			builder.endpointOverride(URI.create(properties.endpoint()));
		}
		if (properties.pathStyleAccessEnabled()) {
			builder.serviceConfiguration(S3Configuration.builder()
				.pathStyleAccessEnabled(true)
				.build());
		}
		return builder.build();
	}

	private AwsCredentialsProvider credentialsProvider(S3Properties properties) {
		if (StringUtils.hasText(properties.accessKey()) && StringUtils.hasText(properties.secretKey())) {
			return StaticCredentialsProvider.create(
				AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
			);
		}
		return DefaultCredentialsProvider.builder().build();
	}
}
