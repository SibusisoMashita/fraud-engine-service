package com.fraudengine.util;

import freemarker.template.Configuration; // Added import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.Map;

@Component
public class TemplateRenderer {

    private final Configuration freemarker;

    @Autowired
    public TemplateRenderer(Configuration freemarker) {
        this.freemarker = freemarker;
    }

    public String render(String template, Map<String, Object> model) {
        try {
            StringWriter out = new StringWriter();
            freemarker.getTemplate(template).process(model, out);
            return out.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to render template: " + template, ex);
        }
    }
}
