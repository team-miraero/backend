package org.jejuro.miraero.domain.availablemoney.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.service.AutoTransferQueryService;
import org.jejuro.miraero.domain.availablemoney.calculator.AvailableMoneyCalculator;
import org.jejuro.miraero.domain.availablemoney.dto.response.DailyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.dto.response.MonthlyAvailableMoneyResponse;
import org.jejuro.miraero.domain.transaction.service.TransactionQueryService;
import org.jejuro.miraero.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailableMoneyServiceImpl implements AvailableMoneyService {

    private final AvailableMoneyCalculator calculator;
    private final TransactionQueryService transactionQueryService;
    private final AutoTransferQueryService autoTransferQueryService;
    private final UserService userService;

    @Override
    public MonthlyAvailableMoneyResponse getMonthlyAvailableMoney(Long userId, Long goalId) {
        List<LocalDateTime> salaryDateTimes = transactionQueryService.getLatestSalaryDateTimes(userId, 3);

        PaydayPeriod period = resolvePaydayPeriod(salaryDateTimes);

        Long fixedExpense = transactionQueryService.getFixedExpenseSum(
                userId, period.startDate, period.endDate);
        Long variableExpense = transactionQueryService.getVariableExpenseSum(
                userId, period.startDate, period.endDate);

        Long monthlyIncome =  userService.getMonthlyIncome(userId);
        Long targetTransfer = autoTransferQueryService.getTargetGoalTransferAmount(goalId);
        Long otherTransfer = autoTransferQueryService.getOtherGoalTransferAmount(userId, goalId);

        Long availableMoney = calculator.calculateMonthlyAvailableMoney(
                monthlyIncome, fixedExpense, variableExpense, targetTransfer, otherTransfer);

        return MonthlyAvailableMoneyResponse.builder()
                .monthlyIncome(monthlyIncome)
                .fixedExpense(fixedExpense)
                .variableExpense(variableExpense)
                .targetGoalAutoTransfer(targetTransfer)
                .otherGoalAutoTransfer(otherTransfer)
                .monthlyAvailableMoney(availableMoney)
                .build();
    }

    @Override
    public DailyAvailableMoneyResponse getDailyAvailableMoney(Long userId, Long goalId) {
        MonthlyAvailableMoneyResponse monthly = getMonthlyAvailableMoney(userId, goalId);

        List<LocalDateTime> salaryDateTimes = transactionQueryService.getLatestSalaryDateTimes(userId, 3);
        PaydayPeriod period = resolvePaydayPeriod(salaryDateTimes);

        // 아침 8시 기준 영업일 날짜 계산
        LocalDate businessDate = LocalDateTime.now().minusHours(8).toLocalDate();
        long remainingDays = ChronoUnit.DAYS.between(businessDate, period.endDate.toLocalDate()) + 1;
        if (remainingDays <= 0) remainingDays = 1;

        Long todayAvailableMoney = calculator.calculateDailyAvailableMoney(
                monthly.getMonthlyAvailableMoney(), remainingDays);
        Long todayExpense = transactionQueryService.getTodayExpenseSum(userId);

        return DailyAvailableMoneyResponse.builder()
                .todayAvailableMoney(todayAvailableMoney)
                .todayExpense(todayExpense)
                .remainingAvailableMoney(calculator.calculateRemainingMoney(todayAvailableMoney, todayExpense))
                .build();
    }

    private PaydayPeriod resolvePaydayPeriod(List<LocalDateTime> salaryDateTimes) {
        if (salaryDateTimes == null || salaryDateTimes.isEmpty()) {
            LocalDate now = LocalDate.now();
            // Fallback: 이번 달 1일 08:00:00 ~ 다음 달 1일 07:59:59
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atTime(8, 0, 0);
            LocalDateTime nextSalaryDate = now.plusMonths(1).withDayOfMonth(1).atTime(8, 0, 0);
            LocalDateTime endOfMonth = nextSalaryDate.minusSeconds(1);

            return new PaydayPeriod(startOfMonth, endOfMonth, nextSalaryDate);
        }

        // 수령일 당일 08:00:00 시각으로 정제
        LocalDateTime lastSalaryDateTime = salaryDateTimes.get(0).toLocalDate().atTime(8, 0, 0);
        LocalDateTime nextSalaryDateTime = calculateNextSalaryDate(salaryDateTimes);

        // 다음 월급일 08:00:00의 1초 전 = 07:59:59
        LocalDateTime periodEndDateTime = nextSalaryDateTime.minusSeconds(1);

        return new PaydayPeriod(lastSalaryDateTime, periodEndDateTime, nextSalaryDateTime);
    }

    private LocalDateTime calculateNextSalaryDate(List<LocalDateTime> salaryDateTimes) {
        LocalDateTime latest = salaryDateTimes.get(0);
        LocalDate latestDate = latest.toLocalDate();

        int originalDay = resolveOriginalPayDay(salaryDateTimes);

        YearMonth nextMonth = YearMonth.from(latestDate).plusMonths(1);
        int targetDay = Math.min(originalDay, nextMonth.lengthOfMonth());
        LocalDate targetDate = nextMonth.atDay(targetDay);

        LocalDate adjustedDate = adjustForWeekend(targetDate);

        // 다음 월급일 아침 08:00:00 지정
        return adjustedDate.atTime(8, 0, 0);
    }

    private int resolveOriginalPayDay(List<LocalDateTime> salaryDateTimes) {
        LocalDate latestDate = salaryDateTimes.get(0).toLocalDate();

        if (salaryDateTimes.size() >= 2) {
            int previousDay = salaryDateTimes.get(1).toLocalDate().getDayOfMonth();
            if (salaryDateTimes.size() == 2 && latestDate.getDayOfMonth() == previousDay) {
                return previousDay;
            }
            if (salaryDateTimes.size() >= 3) {
                int thirdDay = salaryDateTimes.get(2).toLocalDate().getDayOfMonth();
                if (previousDay == thirdDay) {
                    return previousDay;
                }
            }
        }

        if (latestDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
            int day1Later = latestDate.plusDays(1).getDayOfMonth();
            int day2Later = latestDate.plusDays(2).getDayOfMonth();

            if (day1Later % 5 == 0) return day1Later;
            if (day2Later % 5 == 0) return day2Later;
        }

        return latestDate.getDayOfMonth();
    }

    private LocalDate adjustForWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return date.minusDays(1);
        if (dow == DayOfWeek.SUNDAY) return date.minusDays(2);
        return date;
    }

    private static class PaydayPeriod {
        final LocalDateTime startDate;
        final LocalDateTime endDate;
        final LocalDateTime nextSalaryDate;

        PaydayPeriod(LocalDateTime startDate, LocalDateTime endDate, LocalDateTime nextSalaryDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.nextSalaryDate = nextSalaryDate;
        }
    }
}