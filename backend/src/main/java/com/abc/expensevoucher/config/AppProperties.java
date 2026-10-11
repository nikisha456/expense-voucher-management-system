package com.abc.expensevoucher.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String uploadDir = "uploads";
    private long maxSignatureBytes = 2_097_152L;
    private String timezone = "Asia/Kolkata";
    private String corsOrigins = "http://localhost:5173,http://127.0.0.1:5173";

    public List<String> corsOriginList() {
        if (corsOrigins == null || corsOrigins.isBlank()) {
            return List.of("http://localhost:5173", "http://127.0.0.1:5173");
        }
        return Arrays.stream(corsOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }
}
