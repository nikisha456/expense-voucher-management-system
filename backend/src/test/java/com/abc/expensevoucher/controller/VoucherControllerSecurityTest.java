package com.abc.expensevoucher.controller;

import com.abc.expensevoucher.config.AppProperties;
import com.abc.expensevoucher.config.SecurityConfig;
import com.abc.expensevoucher.exception.JsonErrorWriter;
import com.abc.expensevoucher.security.CustomUserDetailsService;
import com.abc.expensevoucher.security.JwtAuthenticationFilter;
import com.abc.expensevoucher.security.JwtService;
import com.abc.expensevoucher.security.RestAccessDeniedHandler;
import com.abc.expensevoucher.security.RestAuthenticationEntryPoint;
import com.abc.expensevoucher.service.PdfService;
import com.abc.expensevoucher.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VoucherController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        JsonErrorWriter.class
})
class VoucherControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private PdfService pdfService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AppProperties appProperties;

    @Test
    void unauthenticatedListIsRejected() throws Exception {
        mockMvc.perform(get("/api/vouchers/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ACCOUNTS")
    void accountsCannotCreateVouchers() throws Exception {
        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "voucherDate": "2026-10-01",
                                  "expenseDate": "2026-10-01",
                                  "departmentName": "Engineering",
                                  "expenseTitle": "Taxi",
                                  "expenseCategory": "TRAVEL",
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DIRECTOR")
    void directorCannotUseEmployeeList() throws Exception {
        mockMvc.perform(get("/api/vouchers/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanListOwnVouchers() throws Exception {
        when(voucherService.listMine(any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/vouchers/my"))
                .andExpect(status().isOk());
    }
}
