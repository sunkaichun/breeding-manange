package com.wens.breeding.visualization;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.lark.base.VisualizationDataRecord;

public final class WeightTrendVisualizationGenerator {
    public List<VisualizationDataRecord> generate(
            AnalysisRequest request,
            BreedingBatch batch,
            List<WeightRecord> records,
            List<BreedingStandard> standards) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(batch, "batch");
        List<WeightRecord> sortedRecords = sorted(records);
        List<VisualizationDataRecord> outputs = new ArrayList<>();

        outputs.add(new VisualizationDataRecord(
                request.getRequestId(),
                "line",
                "measuredDate",
                "averageWeightKg",
                buildTrendJson(sortedRecords)));
        outputs.add(new VisualizationDataRecord(
                request.getRequestId(),
                "band",
                "measuredDate",
                "standardWeightRangeKg",
                buildStandardBandJson(batch, sortedRecords, standards)));
        outputs.add(new VisualizationDataRecord(
                request.getRequestId(),
                "scatter",
                "measuredDate",
                "weightAnomaly",
                buildAnomalyJson(batch, sortedRecords, standards)));

        return outputs;
    }

    private static String buildTrendJson(List<WeightRecord> records) {
        JsonArrayBuilder builder = new JsonArrayBuilder();
        for (WeightRecord record : records) {
            builder.object()
                    .string("date", record.getMeasuredDate().toString())
                    .number("ageDays", record.getAgeDays())
                    .number("value", record.getAverageWeightKg())
                    .number("stockCount", record.getStockCount())
                    .endObject();
        }
        return builder.build();
    }

    private static String buildStandardBandJson(
            BreedingBatch batch,
            List<WeightRecord> records,
            List<BreedingStandard> standards) {
        JsonArrayBuilder builder = new JsonArrayBuilder();
        for (WeightRecord record : records) {
            Optional<BreedingStandard> standard = findStandard(batch, record, standards);
            if (standard.isPresent()) {
                builder.object()
                        .string("date", record.getMeasuredDate().toString())
                        .number("ageDays", record.getAgeDays())
                        .number("min", standard.get().getMinWeightKg())
                        .number("max", standard.get().getMaxWeightKg())
                        .endObject();
            }
        }
        return builder.build();
    }

    private static String buildAnomalyJson(
            BreedingBatch batch,
            List<WeightRecord> records,
            List<BreedingStandard> standards) {
        JsonArrayBuilder builder = new JsonArrayBuilder();
        for (WeightRecord record : records) {
            Optional<BreedingStandard> standard = findStandard(batch, record, standards);
            if (!standard.isPresent()) {
                builder.object()
                        .string("date", record.getMeasuredDate().toString())
                        .number("ageDays", record.getAgeDays())
                        .number("value", record.getAverageWeightKg())
                        .string("status", "MISSING_STANDARD")
                        .endObject();
                continue;
            }
            String status = anomalyStatus(record.getAverageWeightKg(), standard.get());
            if (!"NORMAL".equals(status)) {
                builder.object()
                        .string("date", record.getMeasuredDate().toString())
                        .number("ageDays", record.getAgeDays())
                        .number("value", record.getAverageWeightKg())
                        .string("status", status)
                        .endObject();
            }
        }
        return builder.build();
    }

    private static String anomalyStatus(BigDecimal actualWeight, BreedingStandard standard) {
        if (actualWeight.compareTo(standard.getMinWeightKg()) < 0) {
            return "BELOW_STANDARD";
        }
        if (actualWeight.compareTo(standard.getMaxWeightKg()) > 0) {
            return "ABOVE_STANDARD";
        }
        return "NORMAL";
    }

    private static Optional<BreedingStandard> findStandard(
            BreedingBatch batch,
            WeightRecord record,
            List<BreedingStandard> standards) {
        return safeList(standards).stream()
                .filter(standard -> standard.matches(batch.getBreedName(), batch.getFeedingMode(), record.getAgeDays()))
                .findFirst();
    }

    private static List<WeightRecord> sorted(List<WeightRecord> records) {
        List<WeightRecord> copy = new ArrayList<>(safeList(records));
        copy.sort(Comparator.comparing(WeightRecord::getMeasuredDate));
        return copy;
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? java.util.Collections.emptyList() : values;
    }
}
