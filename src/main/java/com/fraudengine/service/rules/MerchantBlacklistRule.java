package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleName;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.MerchantBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantBlacklistRule implements FraudRule {

    private final MerchantBlacklistService blacklistService;

    @Override
    public RuleResult evaluate(Transaction tx) {

        boolean isBlacklisted = blacklistService.isBlacklisted(tx.getMerchant());
        boolean passed = !isBlacklisted;

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Merchant clean" : "Merchant is blacklisted")
                .score(passed ? 0 : 4)
                .build();
    }

    @Override
    public String getRuleName() {
        return RuleName.MERCHANT_BLACKLIST.value();
    }
}
