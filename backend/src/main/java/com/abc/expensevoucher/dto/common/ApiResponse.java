package com.abc.expensevoucher.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final PageMeta meta;

    public ApiResponse(boolean success, String message, T data, PageMeta meta) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.meta = meta;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<List<T>> page(List<T> data, Page<?> page) {
        PageMeta meta = new PageMeta(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        return new ApiResponse<>(true, "OK", data, meta);
    }
}
