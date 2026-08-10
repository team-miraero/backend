package org.jejuro.miraero.domain.goal.milestone.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneListResponse;
import org.jejuro.miraero.domain.goal.milestone.service.MilestoneService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class MilestoneController {

    private final MilestoneService milestoneService;

    /**
     * 목표의 마일스톤 여정을 조회한다.
     *
     * 25%, 50%, 75%, 100% 마일스톤과
     * 각 마일스톤에 연결된 AI 리포트를 함께 반환한다.
     */
    @GetMapping("/{goalId}/milestones")
    public ResponseEntity<ApiResponse<MilestoneListResponse>> getMilestoneJourney(
            @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user.getUserId();

        MilestoneListResponse response =
                milestoneService.getMilestones(
                        goalId,
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}