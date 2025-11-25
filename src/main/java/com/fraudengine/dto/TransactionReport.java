package com.fraudengine.dto;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class TransactionReport {

    private Transaction transaction;
    private Customer customer;
    private List<RuleResult> rules;

    public TransactionReport(Transaction tx, Customer customer, List<RuleResult> rules) {
        this.transaction = tx;
        this.customer = customer;
        this.rules = rules;
    }
}
