package com.groovo.server.auth.service;

import com.groovo.server.auth.dto.LoginRequest;
import com.groovo.server.auth.dto.SignupRequest;
import com.groovo.server.auth.dto.SignupResponse;
import com.groovo.server.auth.dto.TokenResponse;
import com.groovo.server.common.exception.BusinessException;
import com.groovo.server.common.exception.ErrorCode;
import com.groovo.server.common.jwt.JwtProvider;
import com.groovo.server.user.domain.User;
import com.groovo.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  public SignupResponse signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
    }
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
    }
    User user =
        User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .build();
    userRepository.save(user);
    return SignupResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .createdAt(user.getCreatedAt())
        .build();
  }

  public TokenResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
    String token = jwtProvider.generateToken(user.getId());
    return TokenResponse.builder()
        .accessToken(token)
        .tokenType("Bearer")
        .expiresIn((int) jwtProvider.getExpiresIn())
        .build();
  }

  public void logout(String token) {}
}
