package com.abc.expensevoucher.controller;

import com.abc.expensevoucher.dto.common.ApiResponse;
import com.abc.expensevoucher.dto.voucher.RejectRequest;
import com.abc.expensevoucher.dto.voucher.VoucherFilter;
import com.abc.expensevoucher.dto.voucher.VoucherRequest;
import com.abc.expensevoucher.dto.voucher.VoucherResponse;
import com.abc.expensevoucher.dto.voucher.VoucherSummaryResponse;
import com.abc.expensevoucher.service.PdfService;
import com.abc.expensevoucher.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Vouchers")
public class VoucherController {

    private final VoucherService voucherService;
    private final PdfService pdfService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create a draft voucher")
    public ResponseEntity<ApiResponse<VoucherResponse>> create(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Draft saved", voucherService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update a draft voucher")
    public ApiResponse<VoucherResponse> update(@PathVariable Long id, @Valid @RequestBody VoucherRequest request) {
        return ApiResponse.ok("Draft updated", voucherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete a draft voucher")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ApiResponse.ok("Voucher deleted", null);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Submit a draft voucher for approval")
    public ApiResponse<VoucherResponse> submit(@PathVariable Long id) {
        return ApiResponse.ok("Voucher submitted for approval", voucherService.submit(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "List vouchers created by the signed-in employee")
    public ApiResponse<List<VoucherSummaryResponse>> mine(VoucherFilter filter) {
        return page(voucherService.listMine(filter));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('DIRECTOR')")
    @Operation(summary = "List vouchers waiting for director approval")
    public ApiResponse<List<VoucherSummaryResponse>> pending(VoucherFilter filter) {
        return page(voucherService.listPending(filter));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECTOR','ACCOUNTS')")
    @Operation(summary = "List every voucher")
    public ApiResponse<List<VoucherSummaryResponse>> all(VoucherFilter filter) {
        return page(voucherService.listAll(filter));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Voucher details visible to the current role")
    public ApiResponse<VoucherResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(voucherService.getById(id));
    }

    @PostMapping(value = "/{id}/signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Attach an employee signature to a draft voucher")
    public ApiResponse<VoucherResponse> signature(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Signature attached", voucherService.uploadEmployeeSignature(id, file));
    }

    @PostMapping("/{id}/signature/use-profile")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Copy the employee profile signature onto a draft voucher")
    public ApiResponse<VoucherResponse> useProfileSignature(@PathVariable Long id) {
        return ApiResponse.ok("Saved signature applied", voucherService.useProfileSignature(id));
    }

    @PostMapping(value = "/{id}/approve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DIRECTOR')")
    @Operation(summary = "Approve a pending voucher and store the director signature")
    public ApiResponse<VoucherResponse> approve(@PathVariable Long id,
                                                @RequestParam(value = "signature", required = false) MultipartFile signature,
                                                @RequestParam(value = "useSaved", defaultValue = "false") boolean useSaved) {
        return ApiResponse.ok("Voucher approved", voucherService.approve(id, signature, useSaved));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('DIRECTOR')")
    @Operation(summary = "Reject a pending voucher with a reason")
    public ApiResponse<VoucherResponse> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ApiResponse.ok("Voucher rejected", voucherService.reject(id, request.getReason()));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download a voucher as PDF")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        VoucherResponse voucher = voucherService.getById(id);
        byte[] body = pdfService.generate(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(voucher.getVoucherNumber() + ".pdf", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }

    private ApiResponse<List<VoucherSummaryResponse>> page(Page<VoucherSummaryResponse> result) {
        return ApiResponse.page(result.getContent(), result);
    }
}
