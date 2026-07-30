package org.jejuro.miraero.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.user.domain.User;

@Getter
@AllArgsConstructor
public class SignUpResponse {

    private Long userId;
    private String name;
    private String email;

    public static SignUpResponse from(User user) {
        return new SignUpResponse(
            user.getUserId(),
            user.getName(),
            user.getEmail()
        );
    }
}
