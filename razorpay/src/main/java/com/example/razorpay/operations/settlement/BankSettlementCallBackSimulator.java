package com.example.razorpay.operations.settlement;

import com.example.razorpay.common.enums.SettlementStatus;
import com.example.razorpay.common.utils.RandomizerUtil;
import com.example.razorpay.operations.entity.Settlement;
import com.example.razorpay.operations.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
