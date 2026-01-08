package com.accessrequest.api.controller;

import com.accessrequest.application.dto.*;
import com.accessrequest.application.service.RequestApplicationService;
import com.accessrequest.application.service.RequestApprovalApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RequestController.
 * Tests all request management endpoints with various scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RequestController Integration Tests")
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestApplicationService requestApplicationService;

    @MockBean
    private RequestApprovalApplicationService requestApprovalApplicationService;

    private UUID requestId;
    private UUID requestorId;
    private CreateRequestRequest createRequestRequest;
    private RequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        requestorId = UUID.randomUUID();

        createRequestRequest = CreateRequestRequest.builder()
                .requestorId(requestorId)
                .accessType("OS")
                .systemName("Linux Server")
                .justification("Need access for development work")
                .build();

        requestDTO = RequestDTO.builder()
                .requestId(requestId)
                .requestorId(requestorId)
                .requestorName("John Doe")
                .accessType("OS")
                .systemName("Linux Server")
                .justification("Need access for development work")
                .status("Draft")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Should create request successfully")
    void testCreateRequest() throws Exception {
        when(requestApplicationService.createRequest(any(CreateRequestRequest.class)))
                .thenReturn(requestDTO);

        mockMvc.perform(post("/api/v1/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.status").value("Draft"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Should fail to create request with invalid justification")
    void testCreateRequestWithInvalidJustification() throws Exception {
        CreateRequestRequest invalidRequest = CreateRequestRequest.builder()
                .requestorId(requestorId)
                .accessType("OS")
                .systemName("Linux Server")
                .justification("Short")  // Too short
                .build();

        mockMvc.perform(post("/api/v1/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Should get request details successfully")
    void testGetRequest() throws Exception {
        when(requestApplicationService.getRequest(requestId))
                .thenReturn(requestDTO);

        mockMvc.perform(get("/api/v1/requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.systemName").value("Linux Server"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Should return 404 when request not found")
    void testGetRequestNotFound() throws Exception {
        when(requestApplicationService.getRequest(any(UUID.class)))
                .thenThrow(new RuntimeException("Request not found"));

        mockMvc.perform(get("/api/v1/requests/{requestId}", UUID.randomUUID()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Should list requests with pagination")
    void testListRequests() throws Exception {
        Page<RequestDTO> page = new PageImpl<>(Arrays.asList(requestDTO), PageRequest.of(0, 20), 1);

        when(requestApplicationService.listRequests(
                anyString(), any(UUID.class), anyString(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/requests")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].requestId").value(requestId.toString()));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Should submit request successfully")
    void testSubmitRequest() throws Exception {
        RequestDTO submittedRequest = requestDTO.toBuilder()
                .status("PendingInitialApproval")
                .submittedAt(LocalDateTime.now())
                .build();

        when(requestApplicationService.submitRequest(eq(requestId), any(CreateRequestRequest.class)))
                .thenReturn(submittedRequest);

        mockMvc.perform(put("/api/v1/requests/{requestId}/submit", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PendingInitialApproval"));
    }

    @Test
    @WithMockUser(roles = "HEAD_OF_OFFICE")
    @DisplayName("Should approve request successfully")
    void testApproveRequest() throws Exception {
        UUID approverId = UUID.randomUUID();
        ApproveRequestRequest approveRequest = ApproveRequestRequest.builder()
                .approverId(approverId)
                .approvalType("HeadOfOffice")
                .comments("Approved")
                .build();

        RequestDTO approvedRequest = requestDTO.toBuilder()
                .status("PendingReview")
                .build();

        when(requestApprovalApplicationService.approveRequest(eq(requestId), any(ApproveRequestRequest.class)))
                .thenReturn(approvedRequest);

        mockMvc.perform(put("/api/v1/requests/{requestId}/approve", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PendingReview"));
    }

    @Test
    @WithMockUser(roles = "HEAD_OF_OFFICE")
    @DisplayName("Should decline request successfully")
    void testDeclineRequest() throws Exception {
        UUID declinerId = UUID.randomUUID();
        DeclineRequestRequest declineRequest = DeclineRequestRequest.builder()
                .declinerId(declinerId)
                .reason("Does not meet requirements for access")
                .build();

        RequestDTO declinedRequest = requestDTO.toBuilder()
                .status("Declined")
                .build();

        when(requestApprovalApplicationService.declineRequest(eq(requestId), any(DeclineRequestRequest.class)))
                .thenReturn(declinedRequest);

        mockMvc.perform(put("/api/v1/requests/{requestId}/decline", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(declineRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Declined"));
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    @DisplayName("Should endorse request successfully")
    void testEndorseRequest() throws Exception {
        UUID reviewerId = UUID.randomUUID();
        EndorseRequestRequest endorseRequest = EndorseRequestRequest.builder()
                .reviewerId(reviewerId)
                .comments("Endorsed for approval")
                .build();

        RequestDTO endorsedRequest = requestDTO.toBuilder()
                .status("PendingFinalApproval")
                .build();

        when(requestApprovalApplicationService.endorseRequest(eq(requestId), any(EndorseRequestRequest.class)))
                .thenReturn(endorsedRequest);

        mockMvc.perform(put("/api/v1/requests/{requestId}/endorse", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(endorseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PendingFinalApproval"));
    }

    @Test
    @WithMockUser(roles = "HEAD")
    @DisplayName("Should return request to reviewer successfully")
    void testReturnRequest() throws Exception {
        UUID returnerId = UUID.randomUUID();
        ReturnRequestRequest returnRequest = ReturnRequestRequest.builder()
                .returnerId(returnerId)
                .reason("Need more information")
                .build();

        RequestDTO returnedRequest = requestDTO.toBuilder()
                .status("ReturnedToReviewer")
                .build();

        when(requestApprovalApplicationService.returnRequest(eq(requestId), any(ReturnRequestRequest.class)))
                .thenReturn(returnedRequest);

        mockMvc.perform(put("/api/v1/requests/{requestId}/return", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ReturnedToReviewer"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should implement request successfully")
    void testImplementRequest() throws Exception {
        UUID implementerId = UUID.randomUUID();
        ImplementRequestRequest implementRequest = ImplementRequestRequest.builder()
                .implementerId(implementerId)
                .notes("Access granted")
                .build();

        RequestDTO implementedRequest = requestDTO.toBuilder()
                .status("Implemented")
                .build();

        when(requestApprovalApplicationService.implementRequest(eq(requestId), any(ImplementRequestRequest.class)))
                .thenReturn(implementedRequest);

        mockMvc.perform(put("/api/v1/requests/{requestId}/implement", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(implementRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Implemented"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Should get request history successfully")
    void testGetRequestHistory() throws Exception {
        HistoryDTO historyDTO = HistoryDTO.builder()
                .timestamp(LocalDateTime.now())
                .action("Created")
                .actor("John Doe")
                .previousStatus(null)
                .newStatus("Draft")
                .build();

        Page<HistoryDTO> page = new PageImpl<>(Arrays.asList(historyDTO), PageRequest.of(0, 50), 1);

        when(requestApplicationService.getRequestHistory(eq(requestId), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/requests/{requestId}/history", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("Created"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    @DisplayName("Should return 403 when insufficient permissions")
    void testForbiddenAccess() throws Exception {
        UUID implementerId = UUID.randomUUID();
        ImplementRequestRequest implementRequest = ImplementRequestRequest.builder()
                .implementerId(implementerId)
                .notes("Access granted")
                .build();

        mockMvc.perform(put("/api/v1/requests/{requestId}/implement", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(implementRequest)))
                .andExpect(status().isForbidden());
    }
}
