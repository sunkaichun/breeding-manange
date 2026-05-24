package com.zhitian.breeding.lark.base;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.zhitian.breeding.analysis.model.BreedingBatch;
import com.zhitian.breeding.analysis.model.BreedingStandard;
import com.zhitian.breeding.analysis.model.WeightRecord;

public interface BreedingBaseClient {
    Optional<BreedingBatch> findBatchById(String batchId);

    List<WeightRecord> listWeightRecords(String batchId, LocalDate startDate, LocalDate endDate);

    Optional<BreedingStandard> findStandard(String breedName, String feedingMode, int ageDays);
}
