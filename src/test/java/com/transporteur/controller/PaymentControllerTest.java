package com.transporteur.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transporteur.dto.PaymentRequest;
import com.transporteur.dto.PaymentResponse;
import com.transporteur.dto.PaymentStatusResponse;
import com.transporteur.service.PaymentService;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testProcessPayment_Success() throws Exception {
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setMissionId(1L);
        request.setAmount(100.0);
        request.setCardNumber("1234567890123456");
        request.setCardHolderName("John Doe");

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(1L);
        response.setMissionId(1L);
        response.setAmount(100.0);
        response.setPaymentStatus("COMPLETED");
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1L))
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }

    @Test
    void testCheckPaymentStatus_Success() throws Exception {
        // Given
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setMissionId(1L);
        response.setIsPaid(true);
        response.setAmount(100.0);
        response.setPaymentStatus("COMPLETED");
        when(paymentService.checkPaymentStatus(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/payment/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPaid").value(true))
                .andExpect(jsonPath("$.amount").value(100.0));
    }
}

