package org.jejuro.miraero.domain.transaction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseQueryResult {

    private Long categoryId;
    private String categoryName;
    private Long amount;
}
