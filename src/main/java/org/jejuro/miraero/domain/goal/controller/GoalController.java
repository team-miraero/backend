package org.jejuro.miraero.domain.goal.controller;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.dto.request.*;
import org.jejuro.miraero.domain.goal.dto.response.*;
import org.jejuro.miraero.domain.goal.service.GoalService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    /**
     * 목표 실현 가능성 조회
     */
    @PostMapping("/possibility")
    public ResponseEntity<ApiResponse<GoalPossibilityResponse>> checkPossibility(
            @Valid @RequestBody GoalPossibilityRequest request
            ){

        GoalPossibilityResponse response = goalService.checkPossibility(request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 목표 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GoalCreateResponse>> createGoal(
            @Valid @RequestBody GoalCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if(request == null){
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        // TODO: 인증 구현 후 userId 가져오기
        Long userId = user.getUserId();

        GoalCreateResponse response =
                goalService.createGoal(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 목표 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalListResponse>>> getGoals(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {

        // TODO Security 적용 후 변경
        Long userId = user.getUserId();

        return ResponseEntity.ok(
                ApiResponse.success(
                    goalService.getGoalsByUserId(userId)
                )
        );
    }

    @GetMapping("/collection")
    public ResponseEntity<ApiResponse<List<GoalCollectionResponse>>> getCollection(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user.getUserId();
        List<GoalCollectionResponse> responses =
                goalService.getGoalCollections(userId);

        return ResponseEntity.ok(
                ApiResponse.success(responses)
        );
    }

    /**
     * 목표 상세 조회
     */
    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalDetailResponse>> getGoalDetail(
            @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ){
        Long userId = user.getUserId();
        return ResponseEntity.ok(
                ApiResponse.success(goalService.getGoalDetail(userId,goalId))
        );
    }

    @PatchMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Void>> updateGoal(
            @PathVariable Long goalId,
            @RequestBody GoalUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        //JWT에서 받아오기로
        Long userId = user.getUserId();
        goalService.updateGoal(userId ,goalId, request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ){
        //JWT 적용 예정
        Long userId = user.getUserId();

        goalService.deleteGoal(userId,goalId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{goalId}/collection")
    public ResponseEntity<ApiResponse<Void>> saveCollection(
            @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
            ){
        Long userId = user.getUserId();

        goalService.saveCollection(userId,goalId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{goalId}/status")
    public ResponseEntity<ApiResponse<Void>> updateGoalStatus(
            @PathVariable Long goalId,
            @Valid @RequestBody GoalStatusUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {

        Long userId = user.getUserId();

        goalService.updateGoalStatus(
                userId,
                goalId,
                request.getStatus()
        );

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }



}
