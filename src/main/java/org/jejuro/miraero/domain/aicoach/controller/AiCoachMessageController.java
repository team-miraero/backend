package org.jejuro.miraero.domain.aicoach.controller;

import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.dto.request.AiCoachMessageCreateRequest;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachMessageResponse;
import org.jejuro.miraero.domain.aicoach.service.AiCoachMessageService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-coach/conversations")
public class AiCoachMessageController {

    private final AiCoachMessageService aiCoachMessageService;

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<AiCoachMessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        List<AiCoachMessageResponse> responses = aiCoachMessageService.getMessages(
                user.getUserId(),
                conversationId
        );
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<AiCoachMessageResponse>> sendQuestion(
            @PathVariable Long conversationId,
            @Valid @RequestBody AiCoachMessageCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AiCoachMessageResponse response = aiCoachMessageService.sendQuestion(
                user.getUserId(),
                conversationId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
