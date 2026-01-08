package com.accessrequest.application.service;

import com.accessrequest.application.dto.CreateRequestRequest;
import com.accessrequest.application.dto.RequestDTO;
import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.repository.RequestRepository;
import com.accessrequest.domain.valueobject.RequestId;
import com.accessrequest.domain.valueobject.RequestJustification;
import com.accessrequest.domain.valueobject.RequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestApplicationService.
 * Tests business logic orchestration and transaction management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RequestApplicationService Tests")
class RequestApplicationServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private RequestApplicationService requestApplicationService;

    private UUID requestorId;
    private CreateRequestRequest createRequestRequest;
    private Request request;
    private RequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestorId = UUID.randomUUID();

        createRequestRequest = CreateRequestRequest.builder()
                .requestorId(requestorId)
                .accessType("OS")
                .systemName("Linux Server")
                .justification("Need access for development work")
                .build();

        request = Request.builder()
                .requestId(RequestId.generate())
                .requestorId(requestorId)
                .accessType("OS")
                .systemName("Linux Server")
                .justification(new RequestJustification("Need access for development work"))
                .status(RequestStatus.DRAFT)
                .build();

        requestDTO = RequestDTO.builder()
                .requestId(request.getRequestId().getValue())
                .requestorId(requestorId)
                .requestorName("John Doe")
                .accessType("OS")
                .systemName("Linux Server")
                .justification("Need access for development work")
                .status("Draft")
                .build();
    }

    @Test
    @DisplayName("Should create request successfully")
    void testCreateRequest() {
        when(requestRepository.save(any(Request.class)))
                .thenReturn(request);

        RequestDTO result = requestApplicationService.createRequest(createRequestRequest);

        assertNotNull(result);
        assertEquals(requestorId, result.getRequestorId());
        assertEquals("OS", result.getAccessType());
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    @DisplayName("Should fail to create request with invalid justification")
    void testCreateRequestWithInvalidJustification() {
        CreateRequestRequest invalidRequest = CreateRequestRequest.builder()
                .requestorId(requestorId)
                .accessType("OS")
                .systemName("Linux Server")
                .justification("Short")
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                requestApplicationService.createRequest(invalidRequest));
    }

    @Test
    @DisplayName("Should get request successfully")
    void testGetRequest() {
        when(requestRepository.findById(request.getRequestId()))
                .thenReturn(Optional.of(request));

        RequestDTO result = requestApplicationService.getRequest(request.getRequestId().getValue());

        assertNotNull(result);
        assertEquals(request.getRequestId().getValue(), result.getRequestId());
        verify(requestRepository, times(1)).findById(request.getRequestId());
    }

    @Test
    @DisplayName("Should throw exception when request not found")
    void testGetRequestNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(requestRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                requestApplicationService.getRequest(nonExistentId));
    }

    @Test
    @DisplayName("Should list requests with pagination")
    void testListRequests() {
        Page<Request> page = new PageImpl<>(Arrays.asList(request), PageRequest.of(0, 20), 1);
        when(requestRepository.findAll(any()))
                .thenReturn(page);

        Page<RequestDTO> result = requestApplicationService.listRequests(
                null, null, null, null, null, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(requestRepository, times(1)).findAll(any());
    }

    @Test
    @DisplayName("Should search requests successfully")
    void testSearchRequests() {
        Page<Request> page = new PageImpl<>(Arrays.asList(request), PageRequest.of(0, 20), 1);
        when(requestRepository.findAll(any()))
                .thenReturn(page);

        Page<RequestDTO> result = requestApplicationService.searchRequests(
                "Linux", "Draft", requestorId, "OS", null, null, null, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get request history")
    void testGetRequestHistory() {
        when(requestRepository.findById(request.getRequestId()))
                .thenReturn(Optional.of(request));

        Page<Object> result = requestApplicationService.getRequestHistory(
                request.getRequestId().getValue(), PageRequest.of(0, 50));

        assertNotNull(result);
        verify(requestRepository, times(1)).findById(request.getRequestId());
    }

    @Test
    @DisplayName("Should submit request successfully")
    void testSubmitRequest() {
        UUID headOfOfficeId = UUID.randomUUID();
        when(requestRepository.findById(request.getRequestId()))
                .thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class)))
                .thenReturn(request);

        RequestDTO result = requestApplicationService.submitRequest(
                request.getRequestId().getValue(), createRequestRequest);

        assertNotNull(result);
        verify(requestRepository, times(1)).findById(request.getRequestId());
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    @DisplayName("Should get pending requests for approver")
    void testGetPendingRequestsForApprover() {
        UUID approverId = UUID.randomUUID();
        Page<Request> page = new PageImpl<>(Arrays.asList(request), PageRequest.of(0, 20), 1);
        when(requestRepository.findAll(any()))
                .thenReturn(page);

        Page<RequestDTO> result = requestApplicationService.getPendingRequestsForApprover(
                approverId, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get requests by requestor")
    void testGetRequestsByRequestor() {
        Page<Request> page = new PageImpl<>(Arrays.asList(request), PageRequest.of(0, 20), 1);
        when(requestRepository.findAll(any()))
                .thenReturn(page);

        Page<RequestDTO> result = requestApplicationService.getRequestsByRequestor(
                requestorId, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get requests by access type")
    void testGetRequestsByAccessType() {
        Page<Request> page = new PageImpl<>(Arrays.asList(request), PageRequest.of(0, 20), 1);
        when(requestRepository.findAll(any()))
                .thenReturn(page);

        Page<RequestDTO> result = requestApplicationService.getRequestsByAccessType(
                "OS", PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get overdue requests")
    void testGetOverdueRequests() {
        Page<Request> page = new PageImpl<>(Arrays.asList(request), PageRequest.of(0, 20), 1);
        when(requestRepository.findAll(any()))
                .thenReturn(page);

        Page<RequestDTO> result = requestApplicationService.getOverdueRequests(
                PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get requests by date range")
    void testGetRequestsByDateRange() {
        Page<Request> page = new PageImpl<>(Arrays.asList(request), PageRequest.of(0, 20), 1);
        when(requestRepository.findAll(any()))
                .thenReturn(page);

        Page<RequestDTO> result = requestApplicationService.getRequestsByDateRange(
                request.getCreatedAt().minusDays(1),
                request.getCreatedAt().plusDays(1),
                PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
