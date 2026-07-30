package org.jejuro.miraero.domain.goal.controller;


import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;
import org.jejuro.miraero.domain.goal.service.GoalService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
