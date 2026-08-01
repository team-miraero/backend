package org.jejuro.miraero.domain.goal.controller;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalCreateResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalListResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;
import org.jejuro.miraero.domain.goal.service.GoalService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    @PostMapping("/possibility")
    public ResponseEntity<ApiResponse<GoalPossibilityResponse>> checkPossibility(
            @Valid @RequestBody GoalPossibilityRequest request
            ){

        GoalPossibilityResponse response = goalService.checkPossibility(request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalCreateResponse>> createGoal(
            //@AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GoalCreateRequest request
    ) {

        // TODO: 인증 구현 후 userId 가져오기
        Long userId = 1L;
        //Long userId = userDetails.getUserId();

        GoalCreateResponse response =
                goalService.createGoal(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 목표 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalListResponse>>> getGoals() {

        // TODO Security 적용 후 변경
        Long userId = 1L;

        return ResponseEntity.ok(
                ApiResponse.success(
                    goalService.getGoalsByUserId(userId)
                )
        );
    }


}
