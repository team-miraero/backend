package org.jejuro.miraero.domain.autotransfer.scheduler;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.service.AutoTransferExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoTransferScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoTransferScheduler.class);

    private final AutoTransferExecutionService autoTransferExecutionService;

    /**
     * 매일 08:00에 그날이 이체일인 자동이체를 실행한다.
     *
     * 08:00인 이유는 여유자금 계산이 하루 경계를 08:00으로 잡기 때문이다
     * (AvailableMoneyServiceImpl의 now().minusHours(8)). 다른 시각에 돌리면
     * 지출 집계 구간과 어긋난다.
     *
     * 매일 돌지만 실제 적립은 transfer_day가 오늘인 건에만 일어난다.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void executeAutoTransfers() {
        try {
            autoTransferExecutionService.executeAll(LocalDate.now(), null);
        } catch (Exception e) {
            log.error("저금통 자동이체 배치 실패", e);
        }
    }
}
