package com.wens.breeding.graph.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.WeightRecord;

public final class AiExecutionContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private final AnalysisRequest request;
    private BreedingBatch batch;
    private List<WeightRecord> weightRecords = Collections.emptyList();
    private List<BreedingStandard> weightStandards = Collections.emptyList();
    private List<FcrRecord> fcrRecords = Collections.emptyList();
    private List<FcrStandard> fcrStandards = Collections.emptyList();
    private AnalysisResult analysisResult;

    public AiExecutionContext(AnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        this.request = request;
    }

    public AnalysisRequest getRequest() {
        return request;
    }

    public BreedingBatch getBatch() {
        return batch;
    }

    public void setBatch(BreedingBatch batch) {
        this.batch = batch;
    }

    public boolean hasBatch() {
        return batch != null;
    }

    public List<WeightRecord> getWeightRecords() {
        return weightRecords;
    }

    public void setWeightRecords(List<WeightRecord> weightRecords) {
        this.weightRecords = immutableCopy(weightRecords);
    }

    public List<BreedingStandard> getWeightStandards() {
        return weightStandards;
    }

    public void setWeightStandards(List<BreedingStandard> weightStandards) {
        this.weightStandards = immutableCopy(weightStandards);
    }

    public List<FcrRecord> getFcrRecords() {
        return fcrRecords;
    }

    public void setFcrRecords(List<FcrRecord> fcrRecords) {
        this.fcrRecords = immutableCopy(fcrRecords);
    }

    public List<FcrStandard> getFcrStandards() {
        return fcrStandards;
    }

    public void setFcrStandards(List<FcrStandard> fcrStandards) {
        this.fcrStandards = immutableCopy(fcrStandards);
    }

    public AnalysisResult getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(AnalysisResult analysisResult) {
        this.analysisResult = analysisResult;
    }

    public boolean hasAnalysisResult() {
        return analysisResult != null;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
