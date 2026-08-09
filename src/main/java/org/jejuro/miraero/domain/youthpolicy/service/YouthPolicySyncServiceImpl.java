package org.jejuro.miraero.domain.youthpolicy.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicy;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiItem;
import org.jejuro.miraero.domain.youthpolicy.mapper.YouthPolicyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class YouthPolicySyncServiceImpl implements YouthPolicySyncService {

    private static final DateTimeFormatter APPLICATION_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Pattern APPLICATION_PERIOD_PATTERN = Pattern.compile("^(\\d{8})\\s*~\\s*(\\d{8})$");
    private static final String QUALIFICATION_SEPARATOR = "\n";

    private final YouthPolicyMapper youthPolicyMapper;

    @Override
    @Transactional
    public void syncYouthPolicy(YouthPolicyApiItem source) {
        youthPolicyMapper.upsert(toYouthPolicy(source));
    }

    private YouthPolicy toYouthPolicy(YouthPolicyApiItem source) {
        ApplicationPeriod applicationPeriod = parseApplicationPeriod(source.getAplyYmd());

        return YouthPolicy.builder()
                .policyNo(source.getPlcyNo())
                .policyName(source.getPlcyNm())
                .policyKeyword(source.getPlcyKywdNm())
                .policyDescription(source.getPlcyExplnCn())
                .supportContent(source.getPlcySprtCn())
                .providerInstitutionCode(source.getSprvsnInstCd())
                .providerInstitutionName(source.getSprvsnInstCdNm())
                .applicationStartDate(applicationPeriod.getApplicationStartDate())
                .applicationEndDate(applicationPeriod.getApplicationEndDate())
                .applicationPeriodText(source.getAplyYmd())
                .applicationMethod(source.getPlcyAplyMthdCn())
                .applicationUrl(source.getAplyUrlAddr())
                .referenceUrl(source.getRefUrlAddr1())
                .minAge(toInteger(source.getSprtTrgtMinAge()))
                .maxAge(toInteger(source.getSprtTrgtMaxAge()))
                .incomeConditionCode(source.getEarnCndSeCd())
                .minIncome(toLong(source.getEarnMinAmt()))
                .maxIncome(toLong(source.getEarnMaxAmt()))
                .incomeConditionText(source.getEarnEtcCn())
                .qualification(createQualification(
                        source.getAddAplyQlfcCndCn(),
                        source.getPtcpPrpTrgtCn()
                ))
                .syncedAt(LocalDateTime.now())
                .build();
    }

    private ApplicationPeriod parseApplicationPeriod(String applicationPeriodText) {
        if (isBlank(applicationPeriodText)) {
            return ApplicationPeriod.empty();
        }

        Matcher matcher = APPLICATION_PERIOD_PATTERN.matcher(applicationPeriodText.trim());
        if (!matcher.matches()) {
            return ApplicationPeriod.empty();
        }

        try {
            return new ApplicationPeriod(
                    LocalDate.parse(matcher.group(1), APPLICATION_DATE_FORMATTER),
                    LocalDate.parse(matcher.group(2), APPLICATION_DATE_FORMATTER)
            );
        } catch (DateTimeException exception) {
            return ApplicationPeriod.empty();
        }
    }

    private Integer toInteger(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long toLong(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String createQualification(String additionalQualification, String participationTarget) {
        if (isBlank(additionalQualification) && isBlank(participationTarget)) {
            return null;
        }
        if (isBlank(additionalQualification)) {
            return participationTarget;
        }
        if (isBlank(participationTarget)) {
            return additionalQualification;
        }
        return additionalQualification + QUALIFICATION_SEPARATOR + participationTarget;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class ApplicationPeriod {

        private final LocalDate applicationStartDate;
        private final LocalDate applicationEndDate;

        private ApplicationPeriod(LocalDate applicationStartDate, LocalDate applicationEndDate) {
            this.applicationStartDate = applicationStartDate;
            this.applicationEndDate = applicationEndDate;
        }

        private static ApplicationPeriod empty() {
            return new ApplicationPeriod(null, null);
        }

        private LocalDate getApplicationStartDate() {
            return applicationStartDate;
        }

        private LocalDate getApplicationEndDate() {
            return applicationEndDate;
        }
    }
}
