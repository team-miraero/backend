package org.jejuro.miraero.domain.goal.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetListResponse;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalAssetController {

    private final GoalAssetService goalAssetService;

    //목표와 자산 연결
    @PostMapping("/{goalId}/assets")
    public ResponseEntity<ApiResponse<Void>> addGoalAssets(
            @PathVariable Long goalId,
            @Valid @RequestBody List<GoalAssetRequest> request,
            @AuthenticationPrincipal AuthenticatedUser user
    ){
        Long userId = user.getUserId();

        goalAssetService.saveGoalAssets(userId,goalId,request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }


    /**
     * 목표 연결 자산 조회
     */
    @GetMapping("/{goalId}/assets")
    public ResponseEntity<ApiResponse<GoalAssetListResponse>> getGoalAssets(
            @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        goalAssetService.getGoalAssets(user.getUserId(), goalId)
                )
        );
    }

    @DeleteMapping("/{goalId}/assets/{assetType}/{assetId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoalAsset(
            @PathVariable Long goalId,
            @PathVariable AssetType assetType,
            @PathVariable Long assetId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {

        goalAssetService.deleteGoalAsset(
                user.getUserId(),
                goalId,
                assetType,
                assetId
        );

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }


}
