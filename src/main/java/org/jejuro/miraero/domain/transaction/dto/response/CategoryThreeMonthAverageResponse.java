package org.jejuro.miraero.domain.transaction.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryThreeMonthAverageResponse {

    private String startMonth;
    private String endMonth;
    private List<CategoryThreeMonthAverageItemResponse> categories;
}
