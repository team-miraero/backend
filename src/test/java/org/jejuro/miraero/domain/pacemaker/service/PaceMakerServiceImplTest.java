package org.jejuro.miraero.domain.pacemaker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWeeklyStreakResponse;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaceMakerServiceImplTest {

    private static final Long USER_ID = 10L;

    @Mock
    private PaceMakerMapper paceMakerMapper;

    private PaceMakerService paceMakerService;

    @BeforeEach
    void setUp() {
        paceMakerService = new PaceMakerServiceImpl(paceMakerMapper);
    }

    @Test
    @DisplayName("자동저축 상태가 ACTIVE이면 enabled true를 반환한다")
    void getPaceMaker_active() {
        AutoSaving autoSaving = createAutoSaving(21L, "ACTIVE");
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(autoSaving);

        PaceMakerResponse response = paceMakerService.getPaceMaker(USER_ID);

        assertEquals(21L, response.getAutoSavingId());
        assertTrue(response.isRegistered());
        assertEquals("ACTIVE", response.getStatus());
        assertTrue(response.isEnabled());
        verify(paceMakerMapper).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("자동저축 상태가 PAUSED이면 enabled false를 반환한다")
    void getPaceMaker_paused() {
        AutoSaving autoSaving = createAutoSaving(22L, "PAUSED");
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(autoSaving);

        PaceMakerResponse response = paceMakerService.getPaceMaker(USER_ID);

        assertEquals(22L, response.getAutoSavingId());
        assertTrue(response.isRegistered());
        assertEquals("PAUSED", response.getStatus());
        assertFalse(response.isEnabled());
        verify(paceMakerMapper).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("자동저축 설정이 없으면 미개설 상태를 반환한다")
    void getPaceMaker_notCreated() {
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(null);

        PaceMakerResponse response = paceMakerService.getPaceMaker(USER_ID);

        assertNull(response.getAutoSavingId());
        assertFalse(response.isRegistered());
        assertNull(response.getStatus());
        assertFalse(response.isEnabled());
        verify(paceMakerMapper).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("자동저축 상태 변경에 성공하면 변경된 상태를 반환한다")
    void updateStatus_success() {
        Long autoSavingId = 21L;
        AutoSaving autoSaving = createAutoSaving(autoSavingId, "PAUSED");
        when(paceMakerMapper.updateStatus(USER_ID, autoSavingId, "PAUSED")).thenReturn(1);
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(autoSaving);

        PaceMakerResponse response = paceMakerService.updateStatus(USER_ID, autoSavingId, "PAUSED");

        assertEquals(autoSavingId, response.getAutoSavingId());
        assertTrue(response.isRegistered());
        assertEquals("PAUSED", response.getStatus());
        assertFalse(response.isEnabled());
        verify(paceMakerMapper).updateStatus(USER_ID, autoSavingId, "PAUSED");
        verify(paceMakerMapper).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("수정된 자동저축이 없으면 리소스 없음 예외를 발생시킨다")
    void updateStatus_notFound() {
        Long autoSavingId = 99L;
        when(paceMakerMapper.updateStatus(USER_ID, autoSavingId, "ACTIVE")).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.updateStatus(USER_ID, autoSavingId, "ACTIVE")
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(paceMakerMapper).updateStatus(USER_ID, autoSavingId, "ACTIVE");
        verify(paceMakerMapper, never()).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("includeStreak false이면 기본 대시보드만 반환한다")
    void getDashboard_withoutStreak() {
        PaceMakerDashboardSummaryResponse summary = createDashboardSummary();
        when(paceMakerMapper.findDashboardByUserId(USER_ID)).thenReturn(summary);

        PaceMakerDashboardResponse response = paceMakerService.getDashboard(USER_ID, false);

        assertEquals(21L, response.getAutoSavingId());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(300_000L, response.getMaxAmount());
        assertEquals(3L, response.getMoneyBox().getMoneyBoxId());
        assertEquals(120_000L, response.getMoneyBox().getBalance());
        assertEquals("123-***-789", response.getMoneyBox().getMaskedAccountNumber());
        assertTrue(response.getTodaySaving().isSaved());
        assertEquals(5_000L, response.getTodaySaving().getAmount());
        assertNull(response.getCurrentStreak());
        assertNull(response.getWeeklyStreak());
        assertNull(response.getMonthlySuccessCount());
        verify(paceMakerMapper).findDashboardByUserId(USER_ID);
        verify(paceMakerMapper, never()).findRecentSavingDates(anyLong());
        verify(paceMakerMapper, never()).findWeeklyStreak(anyLong());
        verify(paceMakerMapper, never()).countMonthlySuccess(anyLong());
    }

    @Test
    @DisplayName("includeStreak true이면 스트릭 통계를 함께 반환한다")
    void getDashboard_withStreak() {
        PaceMakerDashboardSummaryResponse summary = createDashboardSummary();
        List<LocalDate> savingDates = List.of(
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(2)
        );
        List<PaceMakerWeeklyStreakResponse> weeklyStreak = List.of(
                PaceMakerWeeklyStreakResponse.builder()
                        .dayOfWeek("MONDAY")
                        .saved(true)
                        .amount(5_000L)
                        .build()
        );
        when(paceMakerMapper.findDashboardByUserId(USER_ID)).thenReturn(summary);
        when(paceMakerMapper.findRecentSavingDates(21L)).thenReturn(savingDates);
        when(paceMakerMapper.findWeeklyStreak(21L)).thenReturn(weeklyStreak);
        when(paceMakerMapper.countMonthlySuccess(21L)).thenReturn(18);

        PaceMakerDashboardResponse response = paceMakerService.getDashboard(USER_ID, true);

        assertEquals(3, response.getCurrentStreak());
        assertEquals(weeklyStreak, response.getWeeklyStreak());
        assertEquals(18, response.getMonthlySuccessCount());
        verify(paceMakerMapper).findDashboardByUserId(USER_ID);
        verify(paceMakerMapper).findRecentSavingDates(21L);
        verify(paceMakerMapper).findWeeklyStreak(21L);
        verify(paceMakerMapper).countMonthlySuccess(21L);
    }

    @Test
    @DisplayName("대시보드 정보가 없으면 리소스 없음 예외를 발생시킨다")
    void getDashboard_notFound() {
        when(paceMakerMapper.findDashboardByUserId(USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.getDashboard(USER_ID, false)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(paceMakerMapper).findDashboardByUserId(USER_ID);
        verify(paceMakerMapper, never()).findRecentSavingDates(anyLong());
        verify(paceMakerMapper, never()).findWeeklyStreak(anyLong());
        verify(paceMakerMapper, never()).countMonthlySuccess(anyLong());
    }

    private AutoSaving createAutoSaving(Long autoSavingId, String status) {
        AutoSaving autoSaving = new AutoSaving();
        ReflectionTestUtils.setField(autoSaving, "autoSavingId", autoSavingId);
        ReflectionTestUtils.setField(autoSaving, "userId", USER_ID);
        ReflectionTestUtils.setField(autoSaving, "moneyBoxId", 3L);
        ReflectionTestUtils.setField(autoSaving, "accountId", 4L);
        ReflectionTestUtils.setField(autoSaving, "maxAmount", 100_000L);
        ReflectionTestUtils.setField(autoSaving, "autoSavingStatus", status);
        return autoSaving;
    }

    private PaceMakerDashboardSummaryResponse createDashboardSummary() {
        PaceMakerDashboardSummaryResponse summary = new PaceMakerDashboardSummaryResponse();
        ReflectionTestUtils.setField(summary, "autoSavingId", 21L);
        ReflectionTestUtils.setField(summary, "status", "ACTIVE");
        ReflectionTestUtils.setField(summary, "maxAmount", 300_000L);
        ReflectionTestUtils.setField(summary, "moneyBoxId", 3L);
        ReflectionTestUtils.setField(summary, "balance", 120_000L);
        ReflectionTestUtils.setField(summary, "maskedAccountNumber", "123-***-789");
        ReflectionTestUtils.setField(summary, "todaySaved", true);
        ReflectionTestUtils.setField(summary, "todaySavingAmount", 5_000L);
        return summary;
    }
}
