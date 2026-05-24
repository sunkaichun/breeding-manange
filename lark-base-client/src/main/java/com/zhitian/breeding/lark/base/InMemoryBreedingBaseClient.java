package com.zhitian.breeding.lark.base;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.zhitian.breeding.analysis.model.AnalysisRequest;
import com.zhitian.breeding.analysis.model.AnalysisResult;
import com.zhitian.breeding.analysis.model.BreedingBatch;
import com.zhitian.breeding.analysis.model.BreedingStandard;
import com.zhitian.breeding.analysis.model.WeightRecord;

public final class InMemoryBreedingBaseClient implements BreedingBaseClient, AiBaseWriteClient {
    private final Map<String, BreedingBatch> batches;
    private final List<WeightRecord> weightRecords;
    private final List<BreedingStandard> standards;
    private final Map<String, AnalysisRequest> analysisRequests = new LinkedHashMap<>();
    private final Map<String, AnalysisResult> analysisResults = new LinkedHashMap<>();
    private final Map<String, VisualizationDataRecord> visualizationData = new LinkedHashMap<>();

    public InMemoryBreedingBaseClient(
            List<BreedingBatch> batches,
            List<WeightRecord> weightRecords,
            List<BreedingStandard> standards) {
        this.batches = indexBatches(batches);
        this.weightRecords = immutableSortedWeights(weightRecords);
        this.standards = immutableStandards(standards);
    }

    @Override
    public Optional<BreedingBatch> findBatchById(String batchId) {
        return Optional.ofNullable(batches.get(requireText(batchId, "batchId")));
    }

    @Override
    public List<WeightRecord> listWeightRecords(String batchId, LocalDate startDate, LocalDate endDate) {
        String normalizedBatchId = requireText(batchId, "batchId");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }

        return weightRecords.stream()
                .filter(record -> normalizedBatchId.equals(record.getBatchId()))
                .filter(record -> !record.getMeasuredDate().isBefore(startDate))
                .filter(record -> !record.getMeasuredDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BreedingStandard> findStandard(String breedName, String feedingMode, int ageDays) {
        if (ageDays < 0) {
            throw new IllegalArgumentException("ageDays must be non-negative");
        }
        String normalizedBreedName = requireText(breedName, "breedName");
        String normalizedFeedingMode = requireText(feedingMode, "feedingMode");
        return standards.stream()
                .filter(standard -> standard.matches(normalizedBreedName, normalizedFeedingMode, ageDays))
                .findFirst();
    }

    @Override
    public String createAnalysisRequest(AnalysisRequest request) {
        AnalysisRequest nonNullRequest = Objects.requireNonNull(request, "request");
        analysisRequests.put(nonNullRequest.getRequestId(), nonNullRequest);
        return nonNullRequest.getRequestId();
    }

    @Override
    public String saveAnalysisResult(AnalysisResult result) {
        AnalysisResult nonNullResult = Objects.requireNonNull(result, "result");
        analysisResults.put(nonNullResult.getRequestId(), nonNullResult);
        return nonNullResult.getRequestId();
    }

    @Override
    public String saveVisualizationData(VisualizationDataRecord data) {
        VisualizationDataRecord nonNullData = Objects.requireNonNull(data, "data");
        String recordId = "viz-" + UUID.randomUUID();
        visualizationData.put(recordId, nonNullData);
        return recordId;
    }

    public Optional<AnalysisRequest> findWrittenAnalysisRequest(String requestId) {
        return Optional.ofNullable(analysisRequests.get(requireText(requestId, "requestId")));
    }

    public Optional<AnalysisResult> findWrittenAnalysisResult(String requestId) {
        return Optional.ofNullable(analysisResults.get(requireText(requestId, "requestId")));
    }

    public Optional<VisualizationDataRecord> findWrittenVisualizationData(String recordId) {
        return Optional.ofNullable(visualizationData.get(requireText(recordId, "recordId")));
    }

    private static Map<String, BreedingBatch> indexBatches(List<BreedingBatch> batches) {
        Map<String, BreedingBatch> indexed = new LinkedHashMap<>();
        for (BreedingBatch batch : safeList(batches)) {
            BreedingBatch nonNullBatch = Objects.requireNonNull(batch, "batch");
            indexed.put(nonNullBatch.getBatchId(), nonNullBatch);
        }
        return indexed;
    }

    private static List<WeightRecord> immutableSortedWeights(List<WeightRecord> weightRecords) {
        List<WeightRecord> copy = new ArrayList<>(safeList(weightRecords));
        copy.sort(Comparator
                .comparing(WeightRecord::getMeasuredDate)
                .thenComparing(WeightRecord::getBatchId));
        return java.util.Collections.unmodifiableList(copy);
    }

    private static List<BreedingStandard> immutableStandards(List<BreedingStandard> standards) {
        return java.util.Collections.unmodifiableList(new ArrayList<>(safeList(standards)));
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? java.util.Collections.emptyList() : values;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
