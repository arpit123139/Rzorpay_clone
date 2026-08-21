package com.example.razorpay.merchant.api;

import com.example.razorpay.common.dto.SettlementBankDetails;
import com.example.razorpay.common.dto.WebhookTarget;
import com.example.razorpay.merchant.entity.Merchant;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {

    List<WebhookTarget> getActiveConfigForEvent(UUID merchantId, String eventType);

    List<UUID> listActiveMerchantIds();

    SettlementBankDetails getSettlementBankDetail(UUID merchantId);
}
