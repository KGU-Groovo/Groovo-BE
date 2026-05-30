package com.groovo.server.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_users_email", columnNames = "email"),
		@UniqueConstraint(name = "uk_users_provider", columnNames = {"provider", "provider_id"})
	}
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	// 이메일 가입 시에만 채워지는 비밀번호 해시(BCrypt 등). 소셜 로그인 사용자는 null.
	@Column
	private String password;

	@Column(nullable = false)
	private String nickname;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	// 소셜 로그인 사용자만 채워짐. 이메일 가입 사용자는 null.
	@Enumerated(EnumType.STRING)
	@Column
	private Provider provider;

	@Column(name = "provider_id")
	private String providerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserStatus status;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private User(
		String email,
		String password,
		String nickname,
		String profileImageUrl,
		Provider provider,
		String providerId,
		Role role,
		UserStatus status
	) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.provider = provider;
		this.providerId = providerId;
		this.role = role != null ? role : Role.USER;
		this.status = status != null ? status : UserStatus.ACTIVE;
	}
}
