package org.jejuro.miraero.domain.auth.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.auth.dto.request.LoginRequest;
import org.jejuro.miraero.domain.auth.dto.request.SignUpRequest;
import org.jejuro.miraero.domain.auth.dto.response.LoginResponse;
import org.jejuro.miraero.domain.auth.dto.response.SignUpResponse;
import org.jejuro.miraero.domain.auth.service.AuthService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
        @Valid @RequestBody SignUpRequest request
    ) {
        SignUpResponse response = authService.signUp(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
