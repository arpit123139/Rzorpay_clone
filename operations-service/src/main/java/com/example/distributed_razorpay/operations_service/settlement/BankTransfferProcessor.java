package com.example.distributed_razorpay.operations_service.settlement;




import com.example.distributed_razorpay.common_lib.entity.Money;
import com.example.distributed_razorpay.operations_service.settlement.dto.BankTransfferResult;

import java.util.UUID;

public interface BankTransfferProcessor {

    public BankTransfferResult initiate(UUID settlementId , UUID merchantId , Money amount , String bankAccount , String ifsc);
}
