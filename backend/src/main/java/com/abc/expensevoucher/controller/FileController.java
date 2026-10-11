package com.abc.expensevoucher.controller;

import com.abc.expensevoucher.service.SignatureAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/files/signatures")
@RequiredArgsConstructor
@Tag(name = "Files")
public class FileController {

    private final SignatureAccessService signatureAccessService;

    @GetMapping("/{fileName:.+}")
    @Operation(summary = "Download a signature image the current user is allowed to see")
    public ResponseEntity<Resource> signature(@PathVariable String fileName) {
        SignatureAccessService.SignatureFile file = signatureAccessService.load(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mediaType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(file.resource());
    }
}
