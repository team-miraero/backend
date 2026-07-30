package org.jejuro.miraero.domain.goal.controller;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalCreateResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;
import org.jejuro.miraero.domain.goal.service.GoalService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    @PostMapping("/possibility")
    public ResponseEntity<ApiResponse<GoalPossibilityResponse>> checkPossibility(
            @RequestBody GoalPossibilityRequest request
            ){

        GoalPossibilityResponse response = goalService.checkPossibility(request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PostMapping
    public ApiResponse<GoalCreateResponse> createGoal(
            //@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody GoalCreateRequest request
    ) {

        // TODO: 인증 구현 후 userId 가져오기
        Long userId = 1L;
        //Long userId = userDetails.getUserId();

        GoalCreateResponse response =
                goalService.createGoal(userId, request);

        return ApiResponse.success(response);
    }



}
