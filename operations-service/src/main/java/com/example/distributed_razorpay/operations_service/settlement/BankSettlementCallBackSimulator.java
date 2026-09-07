package com.example.distributed_razorpay.operations_service.settlement;


import com.example.distributed_razorpay.common_lib.enums.SettlementStatus;
import com.example.distributed_razorpay.operations_service.entity.Settlement;
import com.example.distributed_razorpay.operations_service.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor

public class BankSettlementCallBackSimulator {

    private final SettlementRepository settlementRepository;
    private final SettlementTransactionExecutor settlementTransactionExecutor;

    @Scheduled(fixedDelayString = "5000")
    @SchedulerLock(name = "operations-service-settlement-simulator",lockAtMostFor = "10s",lockAtLeastFor = "1s")
    public void processCallbacks(){

        List<Settlement> settlements = settlementRepository.findByStatus(SettlementStatus.TRANSFER_PENDING);
        if(settlements.isEmpty()) return;

        for(Settlement settlement:settlements){
            simulateCallBack(settlement);
        }
    }

    private void simulateCallBack(Settlement settlement) {

        log.info("Initializing settlement callback for settlementId: {}",settlement.getId());
        settlementTransactionExecutor.resolveTransfer(settlement.getId(),null,null);

    }
}
