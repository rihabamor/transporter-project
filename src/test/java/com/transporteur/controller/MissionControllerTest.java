package com.transporteur.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;

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
import com.transporteur.dto.MissionRequest;
import com.transporteur.dto.MissionResponse;
import com.transporteur.dto.PriceProposalRequest;
import com.transporteur.dto.TransporteurAvailableResponse;
import com.transporteur.dto.UpdatePriceRequest;
import com.transporteur.service.MissionService;

@ExtendWith(MockitoExtension.class)
class MissionControllerTest {

    @Mock
    private MissionService missionService;

    @InjectMocks
    private MissionController missionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(missionController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateMission_Success() throws Exception {
        // Given
        MissionRequest request = new MissionRequest();
        request.setTransporteurId(1L);
        request.setDateMission(LocalDateTime.now().plusDays(1));
        request.setLieuDepart("Tunis");
        request.setLieuArrivee("Sfax");

        MissionResponse response = new MissionResponse();
        response.setIdMission(1L);
        when(missionService.createMission(any(MissionRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/missions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMission").value(1L));
    }

    @Test
    void testGetClientMissions_Success() throws Exception {
        // Given
        MissionResponse mission = new MissionResponse();
        mission.setIdMission(1L);
        when(missionService.getClientMissions()).thenReturn(Arrays.asList(mission));

        // When & Then
        mockMvc.perform(get("/api/missions/client"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMission").value(1L));
    }

    @Test
    void testGetTransporteurMissions_Success() throws Exception {
        // Given
        MissionResponse mission = new MissionResponse();
        mission.setIdMission(1L);
        when(missionService.getTransporteurMissions()).thenReturn(Arrays.asList(mission));

        // When & Then
        mockMvc.perform(get("/api/missions/transporteur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMission").value(1L));
    }

    @Test
    void testGetMissionById_Success() throws Exception {
        // Given
        MissionResponse response = new MissionResponse();
        response.setIdMission(1L);
        when(missionService.getMissionById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/missions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMission").value(1L));
    }

    @Test
    void testUpdateMissionStatus_Success() throws Exception {
        // Given
        MissionController.StatusUpdateRequest request = new MissionController.StatusUpdateRequest();
        request.setStatut("EN_COURS");

        MissionResponse response = new MissionResponse();
        response.setIdMission(1L);
        when(missionService.updateMissionStatus(1L, "EN_COURS")).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/missions/1/statut")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testProposePrice_Success() throws Exception {
        // Given
        PriceProposalRequest request = new PriceProposalRequest();
        request.setProposedPrice(100.0);

        MissionResponse response = new MissionResponse();
        response.setIdMission(1L);
        when(missionService.proposePrice(1L, 100.0)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/missions/1/propose-price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testConfirmPrice_Success() throws Exception {
        // Given
        MissionResponse response = new MissionResponse();
        response.setIdMission(1L);
        when(missionService.confirmPrice(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/missions/1/confirm-price"))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelMission_Success() throws Exception {
        // Given
        MissionResponse response = new MissionResponse();
        response.setIdMission(1L);
        when(missionService.cancelMission(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/missions/1/annuler"))
                .andExpect(status().isOk());
    }
}

