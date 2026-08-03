package org.jejuro.miraero.domain.goal.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.YearMonth;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoalPeriodResponse {
    private Integer goalMonths;
    private YearMonth startDate;
    private YearMonth endDate;
    private Integer remainMonths;
}
