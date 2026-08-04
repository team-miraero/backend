package org.jejuro.miraero.domain.goal.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetListResponse;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalAssetController {

    private final GoalAssetService goalAssetService;

    /**
     * 목표 연결 자산 조회
     */
    @GetMapping("/{goalId}/assets")
    public ResponseEntity<ApiResponse<GoalAssetListResponse>> getGoalAssets(
            @PathVariable Long goalId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        goalAssetService.getGoalAssets(goalId)
                )
        );
    }
}
