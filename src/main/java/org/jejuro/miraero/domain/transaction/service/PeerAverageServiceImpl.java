package org.jejuro.miraero.domain.transaction.service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.domain.PeerCategoryAverageQueryResult;
import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageCategoryResponse;
import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageResponse;
import org.jejuro.miraero.domain.transaction.mapper.PeerAverageMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeerAverageServiceImpl implements PeerAverageService {

    private static final int MIN_YEAR = 2000;

    private final PeerAverageMapper peerAverageMapper;

    @Override
    @Transactional(readOnly = true)
    public PeerAverageResponse getPeerAverages(Long userId, Integer year, Integer month) {
        validate(userId, year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        List<PeerAverageCategoryResponse> categories = peerAverageMapper.findCategoryPeerAverages(
                        userId,
                        yearMonth.atDay(1).atStartOfDay(),
                        yearMonth.plusMonths(1).atDay(1).atStartOfDay()
                ).stream()
                .map(PeerAverageCategoryResponse::from)
                .collect(Collectors.toList());

        return new PeerAverageResponse(categories);
    }

    private void validate(Long userId, Integer year, Integer month) {
        if (userId == null || userId <= 0 || year == null || year < MIN_YEAR
                || year > YearMonth.now().getYear() + 1 || month == null || month < 1 || month > 12) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
