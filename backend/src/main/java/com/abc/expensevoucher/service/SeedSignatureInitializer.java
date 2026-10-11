package com.abc.expensevoucher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedSignatureInitializer implements ApplicationRunner {

    private final FileStorageService fileStorageService;

    @Override
    public void run(ApplicationArguments args) {
        fileStorageService.ensureSeedSignatures();
    }
}
