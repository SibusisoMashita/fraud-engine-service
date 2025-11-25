package com.fraudengine.service;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.dto.Customer;
import com.fraudengine.dto.TransactionReport;
import com.fraudengine.repository.RuleResultRepository;
import com.fraudengine.repository.TransactionRepository;
import com.fraudengine.util.PdfGenerator;
import com.fraudengine.util.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionReportService {

    private final TransactionRepository transactionRepository;
    private final RuleResultRepository ruleResultRepository;
    private final TemplateRenderer templateRenderer;
    private final PdfGenerator pdfGenerator;

    @Autowired
    public TransactionReportService(
            TransactionRepository transactionRepository,
            RuleResultRepository ruleResultRepository,
            TemplateRenderer templateRenderer,
            PdfGenerator pdfGenerator) {

        this.transactionRepository = transactionRepository;
        this.ruleResultRepository = ruleResultRepository;
        this.templateRenderer = templateRenderer;
        this.pdfGenerator = pdfGenerator;
    }

    /**
     * Generates a PDF report for a given transaction.
     */
    public byte[] generateReport(String transactionId) {

        // 1. Fetch Transaction
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        // 2. Build Customer DTO (DB does not store customers directly)
        Customer customer = new Customer(tx.getCustomerId());

        // 3. Fetch Rule Results
        List<RuleResult> rules = ruleResultRepository.findByTransactionId(transactionId);
        rules.forEach(r -> r.setRuleName(prettyRuleName(r.getRuleName())));


        // 4. Build the report model
        TransactionReport report = new TransactionReport(tx, customer, rules);

        // 5. Prepare model map for template
        Map<String, Object> model = new HashMap<>();
        model.put("tx", report.getTransaction());
        model.put("customer", report.getCustomer());
        model.put("rules", report.getRules());

        // 6. Render HTML using Freemarker/Thymeleaf
        String html = templateRenderer.render("transaction-report.html", model);

        // 7. Convert HTML → PDF
        return pdfGenerator.createPdf(html);
    }

    public String prettyRuleName(String ruleName) {
        return ruleName
                .replaceAll("Rule$", "")                 // remove “Rule” suffix
                .replaceAll("([a-z])([A-Z])", "$1 $2")   // split camel-case
                .trim();
    }

}
