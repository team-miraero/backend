package org.jejuro.miraero.domain.user.service;

import org.jejuro.miraero.domain.user.domain.User;

public interface UserService {

    User create(UserCreateCommand command);
}
