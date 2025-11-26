package com.fraudengine.api;

import com.fraudengine.service.TransactionReportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TransactionReportController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class TransactionReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionReportService reportService;

    @Test
    void downloadReport_shouldReturnPdf() throws Exception {
        String transactionId = "T123";
        byte[] fakePdf = new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D }; // "%PDF-" header
        Mockito.when(reportService.generateReport(transactionId)).thenReturn(fakePdf);

        mockMvc.perform(get("/api/v1/reports/transaction/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=transaction-report-" + transactionId + ".pdf"))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(fakePdf));
    }
}
