package com.zhitian.breeding.lark.base;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.zhitian.breeding.analysis.model.FcrRecord;
import com.zhitian.breeding.analysis.model.FcrStandard;

public interface FcrBaseClient {
    List<FcrRecord> listFcrRecords(String batchId, LocalDate startDate, LocalDate endDate);

    Optional<FcrStandard> findFcrStandard(String breedName, String feedingMode, int ageDays);
}
