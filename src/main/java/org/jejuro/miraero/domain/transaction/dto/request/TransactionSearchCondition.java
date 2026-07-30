package org.jejuro.miraero.domain.transaction.dto.request;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TransactionSearchCondition {

    private final Integer year;
    private final Integer month;
    private final Long categoryId;
    private final Integer page;
    private final Integer size;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long offset;

    @Builder
    public TransactionSearchCondition(
            Integer year,
            Integer month,
            Long categoryId,
            Integer page,
            Integer size
    ) {
        this.year = year;
        this.month = month;
        this.categoryId = categoryId;
        this.page = page;
        this.size = size;
    }

    public void setQueryRange(LocalDateTime startDateTime, LocalDateTime endDateTime, long offset) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.offset = offset;
    }
}
