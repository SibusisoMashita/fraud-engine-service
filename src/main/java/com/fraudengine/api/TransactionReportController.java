package com.fraudengine.api;

import com.fraudengine.service.TransactionReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class TransactionReportController {

    private final TransactionReportService reportService;

    public TransactionReportController(TransactionReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/transaction/{transactionId}", produces = "application/pdf")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String transactionId) {

        byte[] pdf = reportService.generateReport(transactionId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transaction-report-" + transactionId + ".pdf")
                .body(pdf);
    }
}
