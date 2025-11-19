package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;

public interface FraudRule {

    RuleResult evaluate(Transaction transaction);

    String getRuleName();
}
