package com.fraudengine.service;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.dto.Customer;
import com.fraudengine.repository.RuleResultRepository;
import com.fraudengine.repository.TransactionRepository;
import com.fraudengine.util.PdfGenerator;
import com.fraudengine.util.TemplateRenderer;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TransactionReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private RuleResultRepository ruleResultRepository;
    @Mock
    private PdfGenerator pdfGenerator;

    // Make the TemplateRenderer a mock to allow stubbing with Mockito matchers
    @Mock
    private TemplateRenderer templateRenderer;

    @InjectMocks
    private TransactionReportService reportService;

    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setUp() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        // Use the mocked TemplateRenderer when constructing the service
        reportService = new TransactionReportService(transactionRepository, ruleResultRepository, templateRenderer, pdfGenerator);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    void generateReport_shouldRenderTemplateAndReturnPdf() {
        // Arrange
        String transactionId = "T1";
        Transaction tx = Transaction.builder()
                .transactionId(transactionId)
                .customerId("C1")
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .merchant("StoreA")
                .location("Cape Town")
                .channel("ONLINE")
                .build();
        RuleResult rule = RuleResult.builder()
                .transactionId(transactionId)
                .ruleName("TestRule")
                .passed(true)
                .reason("OK")
                .score(10)
                .build();
        byte[] fakePdf = new byte[]{1,2,3};
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));
        when(ruleResultRepository.findByTransactionId(transactionId)).thenReturn(Collections.singletonList(rule));
        // Stub the mock renderer
        when(templateRenderer.render(eq("transaction-report.html"), any())).thenReturn("<html>report</html>");
        when(pdfGenerator.createPdf(anyString())).thenReturn(fakePdf);

        // Act
        byte[] result = reportService.generateReport(transactionId);

        // Assert
        assertThat(result).isEqualTo(fakePdf);
        verify(transactionRepository).findById(transactionId);
        verify(ruleResultRepository).findByTransactionId(transactionId);
        verify(templateRenderer).render(eq("transaction-report.html"), any());

        // Capture the HTML passed to pdfGenerator
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(pdfGenerator).createPdf(htmlCaptor.capture());
        String generatedHtml = htmlCaptor.getValue();

        // Print the HTML to the console
        System.out.println("Generated HTML:\n" + generatedHtml);

        // Optionally, add assertions on the HTML content
        assertThat(generatedHtml).contains("report");
    }

    @Test
    void renderActualHtmlTemplate() {
        // Arrange
        String transactionId = "T1";
        Transaction tx = Transaction.builder()
                .transactionId(transactionId)
                .customerId("C1")
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .merchant("StoreA")
                .location("Cape Town")
                .channel("ONLINE")
                .build();
        Customer customer = new Customer("C1");
        RuleResult rule = RuleResult.builder()
                .transactionId(transactionId)
                .ruleName("TestRule")
                .passed(true)
                .reason("OK")
                .score(10)
                .build();
        HashMap<String, Object> model = new HashMap<>();
        model.put("tx", tx);
        model.put("customer", customer);
        model.put("rules", Collections.singletonList(rule));

        // Create a real TemplateRenderer for this test so we actually render the file
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        try {
            cfg.setDirectoryForTemplateLoading(new java.io.File("src/main/resources/templates"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        TemplateRenderer realRenderer = new TemplateRenderer(cfg);

        // Act
        String html = realRenderer.render("transaction-report.html", model);

        // Print the HTML to the console
        System.out.println("Rendered HTML from real template:\n" + html);

        // Assert some expected content
        assertThat(html).contains("Fraud Engine – Transaction Report");
        assertThat(html).contains("T1");
        assertThat(html).contains("StoreA");
        assertThat(html).contains("TestRule");
    }
}
