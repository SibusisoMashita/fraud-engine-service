package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleName;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantBlacklistRule implements FraudRule {

    private static final Logger log = LoggerFactory.getLogger(MerchantBlacklistRule.class);

    private final MerchantService merchantService;

    @Override
    public RuleResult evaluate(Transaction tx) {

        String txId = tx.getTransactionId();
        String merchant = tx.getMerchant();

        boolean isBlacklisted;

        try {
            isBlacklisted = merchantService.isBlacklisted(merchant);
        } catch (IllegalArgumentException e) {
            // Unknown merchant → treated as clean (Option A)
            log.warn("[tx={}] ⚠️ event=merchant_not_in_registry merchant={}", txId, merchant);
            isBlacklisted = false;
        }

        boolean passed = !isBlacklisted;

        // Human-friendly log
        if (isBlacklisted) {
            log.info("[tx={}] 🟠 Merchant '{}' is blacklisted", txId, merchant);
        } else {
            log.debug("[tx={}] 🔵 Merchant '{}' is clean or unregistered", txId, merchant);
        }

        // Structured audit log
        log.debug(
            "[tx={}] event=MerchantBlacklistRule merchant={} blacklisted={} passed={}",
            txId,
            merchant,
            isBlacklisted,
            passed
        );

        return RuleResult.builder()
                .transactionId(txId)
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Merchant clean or not registered" : "Merchant is blacklisted")
                .score(passed ? 0 : 4)
                .build();
    }

    @Override
    public String getRuleName() {
        return RuleName.MERCHANT_BLACKLIST.value();
    }
}
