package org.jejuro.miraero.domain.autotransfer.controller;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.dto.response.AutoTransferExecutionResponse;
import org.jejuro.miraero.domain.autotransfer.exception.AutoTransferErrorCode;
import org.jejuro.miraero.domain.autotransfer.service.AutoTransferExecutionService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auto-transfers")
@RequiredArgsConstructor
public class AutoTransferController {

    private final AutoTransferExecutionService autoTransferExecutionService;

    /**
     * 자동이체를 즉시 실행한다.
     *
     * 스케줄러는 매일 08:00에만 돌아 시연 중에 보여줄 수 없어서 둔 엔드포인트다.
     * date를 주면 그날 기준으로 실행해 지난 이체일들을 순서대로 재생할 수 있다.
     * 본인 저금통만 대상이라 다른 사용자의 데이터에는 영향이 없다.
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<AutoTransferExecutionResponse>> execute(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate executionDate = date == null ? LocalDate.now() : date;

        // 아직 오지 않은 이체를 미리 당겨받지 못하게 막는다
        if (executionDate.isAfter(LocalDate.now())) {
            throw new BusinessException(AutoTransferErrorCode.FUTURE_EXECUTION_DATE);
        }

        int executedCount =
                autoTransferExecutionService.executeAll(executionDate, user.getUserId());

        return ResponseEntity.ok(ApiResponse.success(
                AutoTransferExecutionResponse.builder()
                        .executionDate(executionDate)
                        .executedCount(executedCount)
                        .build()
        ));
    }
}
