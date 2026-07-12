package com.example.razorpay.payment.simulator;

import com.example.razorpay.common.enums.ChaosMode;
import com.example.razorpay.common.enums.PaymentStatus;
import com.example.razorpay.common.utils.RandomizerUtil;
import com.example.razorpay.payment.entity.Payment;
import com.example.razorpay.payment.repository.PaymentRepository;
import com.example.razorpay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
//In this simulator we are combining Bank+PaymentProcessor + Webhook Event + The real flow is explain in the notes
/*
The Banking Simulator acts as a Payment Processor + Bank events (using schedular)
and therefore the PaymentProcessor is the one who talks to the gateway's resolveAuthorization() method.
In the real world, the Payment Processor entity would receive an HTTP call to the webhook api exposed by Payment Processor
from the banks and the processor calls the gateway's resolveAuthorization()
* */
public class BankCallbackSimulator {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")  // Call Methods after every 5 sec
    public void processCallbacks() {

        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);

        List<Payment> candidates = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);

        log.info("Simulating payments for {} payments", candidates.size());
        if (candidates.isEmpty()) return;

        for (Payment payment: candidates) {
            simulateCallback(payment);
        }

    }

    private void simulateCallback(Payment payment) {
            SimulatorConfig.MethodSimulatorConfig methodConfig = simulatorConfig.configFor(payment.getMethod());

            LocalDateTime dueAt = dueAt(payment,methodConfig);

            if(LocalDateTime.now().isBefore(dueAt))
                return;


            switch (simulatorConfig.getChaosMode()){
                case SUCCESS -> resolve(payment,true);
                case FAILURE -> resolve(payment,false);
                case TIMEOUT -> {
                    log.debug("BankCallBack Simulator: Payment TimedOut");
                }
                case NORMAL,SLOW -> resolve(payment,shouldApprove(payment,methodConfig));
            }
     }

     private void resolve(Payment payment,boolean approve){
            if(approve){
                String bankRef = "SIM_BANK_REF"+ RandomizerUtil.randomBase64(8);
                paymentService.resolveAuthorization(payment.getId(),true,bankRef,null,null);
            }
            else{
                paymentService.resolveAuthorization(payment.getId(),false,null,"SIM_BANK_ERROR_CODE","Simulated bank rejected Payment");
            }
     }
    //Just a way to simulate
     private boolean shouldApprove(Payment payment,SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig){
            int bucket = Math.abs(payment.getId().hashCode())%100;

            return bucket < methodSimulatorConfig.getSuccessRate();
     }

     // time when they payment will be due and need to be procssesd
     LocalDateTime dueAt(Payment payment , SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig){

        int range = methodSimulatorConfig.getMaxDelaySeconds()-methodSimulatorConfig.getMinDelaySeconds();
        int delaySeconds = methodSimulatorConfig.getMinDelaySeconds() + Math.abs(payment.getId().hashCode())%(range+1);

        if(simulatorConfig.getChaosMode() == ChaosMode.SLOW){
            delaySeconds*=2;
        }

        return payment.getCreatedAt().plusSeconds(delaySeconds);
     }

}
