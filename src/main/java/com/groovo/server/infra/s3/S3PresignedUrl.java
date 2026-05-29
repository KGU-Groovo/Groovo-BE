package com.groovo.server.infra.s3;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record S3PresignedUrl(
	String key,
	String url,
	Instant expiresAt,
	Map<String, List<String>> signedHeaders
) {
}
