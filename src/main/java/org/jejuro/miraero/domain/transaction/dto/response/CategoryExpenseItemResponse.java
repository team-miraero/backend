package org.jejuro.miraero.domain.transaction.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryExpenseItemResponse {
    private Long categoryId;
    private String categoryName;
    private Long amount;
    private BigDecimal ratio;
}
