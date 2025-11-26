package com.fraudengine.util;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGenerator {

    public byte[] createPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder =
                    new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();

            builder.withHtmlContent(html, "");
            builder.toStream(out);
            builder.run();

            return out.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate PDF", ex);
        }
    }
}
