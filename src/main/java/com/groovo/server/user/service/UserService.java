package com.groovo.server.user.service;

import com.groovo.server.common.exception.BusinessException;
import com.groovo.server.common.exception.ErrorCode;
import com.groovo.server.user.domain.User;
import com.groovo.server.user.dto.UserProfileResponse;
import com.groovo.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  public UserProfileResponse getUserProfile(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    return UserProfileResponse.from(user);
  }
}
