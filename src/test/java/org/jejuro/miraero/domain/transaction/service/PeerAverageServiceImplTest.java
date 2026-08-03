package org.jejuro.miraero.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jejuro.miraero.domain.transaction.domain.PeerCategoryAverageQueryResult;
import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageResponse;
import org.jejuro.miraero.domain.transaction.mapper.PeerAverageMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PeerAverageServiceImplTest {

    @Mock
    private PeerAverageMapper peerAverageMapper;

    private PeerAverageService service;

    @BeforeEach
    void setUp() {
        service = new PeerAverageServiceImpl(peerAverageMapper);
    }

    @Test
    void getPeerAverages_returnsCategoryAveragesIncludingZeroAmount() {
        when(peerAverageMapper.findCategoryPeerAverages(
                eq(1L),
                eq(LocalDateTime.of(2026, 7, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 8, 1, 0, 0))
        )).thenReturn(List.of(
                new PeerCategoryAverageQueryResult(1L, "Food", 285_000L),
                new PeerCategoryAverageQueryResult(2L, "Cafe", 0L)
        ));

        PeerAverageResponse response = service.getPeerAverages(1L, 2026, 7);

        assertEquals(2, response.getCategories().size());
        assertEquals(1L, response.getCategories().get(0).getCategoryId());
        assertEquals("Food", response.getCategories().get(0).getCategoryName());
        assertEquals(285_000L, response.getCategories().get(0).getPeerAverageAmount());
        assertEquals(0L, response.getCategories().get(1).getPeerAverageAmount());
    }

    @Test
    void getPeerAverages_usesRequestedMonthRange() {
        when(peerAverageMapper.findCategoryPeerAverages(
                eq(1L),
                eq(LocalDateTime.of(2026, 1, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 2, 1, 0, 0))
        )).thenReturn(List.of());

        service.getPeerAverages(1L, 2026, 1);

        verify(peerAverageMapper).findCategoryPeerAverages(
                1L,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 2, 1, 0, 0)
        );
    }

    @Test
    void getPeerAverages_rejectsInvalidInput() {
        assertThrows(BusinessException.class, () -> service.getPeerAverages(null, 2026, 7));
        assertThrows(BusinessException.class, () -> service.getPeerAverages(1L, 1999, 7));
        assertThrows(BusinessException.class, () -> service.getPeerAverages(1L, 2026, 13));

        verifyNoInteractions(peerAverageMapper);
    }
}
