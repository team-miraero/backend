package org.jejuro.miraero.domain.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpenseCategoryResponse {

    private Long categoryId;
    private String categoryName;

    public static ExpenseCategoryResponse of(Long categoryId, String categoryName) {
        return new ExpenseCategoryResponse(categoryId, categoryName);
    }
}
