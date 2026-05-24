package com.wens.breeding.app.baseapp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.wens.breeding.analysis.model.RiskLevel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BaseAppAnalysisController.class)
class BaseAppAnalysisControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationAnalysisService applicationAnalysisService;

    @Test
    void acceptsBaseAppAnalysisRequest() throws Exception {
        when(applicationAnalysisService.submit(any(BaseAppAnalysisRequest.class)))
                .thenReturn(new BaseAppAnalysisResponse(
                        "REQ-APP-001",
                        "COMPLETED",
                        RiskLevel.HIGH,
                        "Weight trend has high risk.",
                        Collections.singletonList("Average weight is below the standard range."),
                        Collections.singletonList("Review the feeding plan."),
                        Collections.singletonList("viz-001")));

        mockMvc.perform(post("/api/lark/base-app/analysis-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"requesterOpenId\":\"ou_test\","
                                + "\"batchId\":\"BATCH-001\","
                                + "\"analysisType\":\"WEIGHT_TREND\","
                                + "\"startDate\":\"2026-05-20\","
                                + "\"endDate\":\"2026-05-22\""
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("REQ-APP-001"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.visualizationRecordIds[0]").value("viz-001"));
    }

    @Test
    void returnsBadRequestWhenServiceRejectsInput() throws Exception {
        when(applicationAnalysisService.submit(any(BaseAppAnalysisRequest.class)))
                .thenThrow(new IllegalArgumentException("batchId must not be blank"));

        mockMvc.perform(post("/api/lark/base-app/analysis-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"batchId\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("batchId must not be blank"));
    }
}
