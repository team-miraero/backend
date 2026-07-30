package org.jejuro.miraero.domain.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaginationResponse {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    public static PaginationResponse of(int page, int size, long totalElements) {
        int totalPages = (int) ((totalElements + size - 1) / size);
        boolean hasNext = page < totalPages;

        return new PaginationResponse(page, size, totalElements, totalPages, hasNext);
    }
}
