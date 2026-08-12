package org.jejuro.miraero.domain.autotransfer.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AutoTransferExecutionResponse {

    private LocalDate executionDate;
    private int executedCount;
}
