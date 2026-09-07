package com.example.distributed_razorpay.operations_service.entity;


import com.example.distributed_razorpay.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settlement_payment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SettlementPayment extends BaseEntity {

    @EmbeddedId
    private SettlementPaymentId id;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

}
