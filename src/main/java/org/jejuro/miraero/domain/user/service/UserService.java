package org.jejuro.miraero.domain.user.service;

import org.jejuro.miraero.domain.user.dto.request.UserSignUpRequest;
import org.jejuro.miraero.domain.user.dto.response.UserSignUpResponse;

public interface UserService {

  UserSignUpResponse signUp(UserSignUpRequest request);
}
