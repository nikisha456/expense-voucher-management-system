package com.abc.expensevoucher.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final AppProperties appProperties;

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(appProperties.getTimezone()));
    }
}
