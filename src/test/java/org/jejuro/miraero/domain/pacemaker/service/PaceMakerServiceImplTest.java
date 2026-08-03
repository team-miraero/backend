package org.jejuro.miraero.domain.pacemaker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
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
        paceMakerService = new PaceMakerImpl(paceMakerMapper);
    }

    @Test
    @DisplayName("자동저축 상태가 ACTIVE이면 enabled true를 반환한다")
    void getPaceMaker_active() {
        AutoSaving autoSaving = createAutoSaving(21L, "ACTIVE");
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(autoSaving);

        PaceMakerResponse response = paceMakerService.getPaceMaker(USER_ID);

        assertEquals(21L, response.getAutoSavingId());
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
        assertNull(response.getStatus());
        assertFalse(response.isEnabled());
        verify(paceMakerMapper).findByUserId(USER_ID);
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
}
