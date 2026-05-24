package com.wens.breeding.app.baseapp;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lark/base-app")
public class BaseAppAnalysisController {
    private final ApplicationAnalysisService applicationAnalysisService;

    public BaseAppAnalysisController(ApplicationAnalysisService applicationAnalysisService) {
        this.applicationAnalysisService = applicationAnalysisService;
    }

    @PostMapping("/analysis-requests")
    public BaseAppAnalysisResponse createAnalysisRequest(@RequestBody BaseAppAnalysisRequest request) {
        return applicationAnalysisService.submit(request);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<BaseAppErrorResponse> handleBadRequest(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseAppErrorResponse("BAD_REQUEST", exception.getMessage()));
    }
}
