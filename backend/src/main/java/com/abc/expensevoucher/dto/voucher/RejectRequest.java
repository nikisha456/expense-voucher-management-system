package com.abc.expensevoucher.dto.voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 10, max = 1000, message = "Rejection reason must be between 10 and 1000 characters")
    private String reason;
}
