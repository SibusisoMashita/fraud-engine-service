package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MerchantBlacklistRule implements FraudRule {

    private static final Set<String> BLACKLIST = Set.of(
            "FraudStore",
            "ShadyDealer",
            "SuspiciousMerchant"
    );

    @Override
    public RuleResult evaluate(Transaction tx) {

        boolean passed = !BLACKLIST.contains(tx.getMerchant());

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
        return "MerchantBlacklistRule";
    }
}
