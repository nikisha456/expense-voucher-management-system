package com.abc.expensevoucher.security;

import com.abc.expensevoucher.exception.JsonErrorWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonErrorWriter errorWriter;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        String message = accessDeniedException.getMessage();
        if (message == null || message.isBlank() || "Access is denied".equals(message)) {
            message = "You do not have permission to perform this action";
        }
        errorWriter.write(response, request, HttpServletResponse.SC_FORBIDDEN, "Forbidden", message);
    }
}
