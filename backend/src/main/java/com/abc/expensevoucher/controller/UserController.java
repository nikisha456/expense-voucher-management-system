package com.abc.expensevoucher.controller;

import com.abc.expensevoucher.dto.auth.UserResponse;
import com.abc.expensevoucher.dto.common.ApiResponse;
import com.abc.expensevoucher.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Current user profile")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(userService.me());
    }

    @PostMapping(value = "/me/signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload the signed-in user's signature image")
    public ApiResponse<UserResponse> uploadSignature(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Signature saved", userService.uploadSignature(file));
    }
}
