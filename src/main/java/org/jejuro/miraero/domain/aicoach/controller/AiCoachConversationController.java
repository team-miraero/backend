package org.jejuro.miraero.domain.aicoach.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationCreateResponse;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationResponse;
import org.jejuro.miraero.domain.aicoach.service.AiCoachConversationService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-coach/conversations")
public class AiCoachConversationController {

    private final AiCoachConversationService aiCoachConversationService;

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<AiCoachConversationResponse>> getLatestConversation(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AiCoachConversationResponse response =
                aiCoachConversationService.getLatestConversation(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiCoachConversationCreateResponse>> createConversation(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AiCoachConversationCreateResponse response =
                aiCoachConversationService.createConversation(user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiCoachConversationResponse>>> getConversations(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        List<AiCoachConversationResponse> responses =
                aiCoachConversationService.getConversations(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        aiCoachConversationService.deleteConversation(user.getUserId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
