package com.wens.breeding.app.agent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.lark.base.BreedingBaseClient;

import org.springframework.stereotype.Component;

@Component
public final class BatchLookupAgentTool implements AgentTool {
    private final BreedingBaseClient breedingBaseClient;

    public BatchLookupAgentTool(BreedingBaseClient breedingBaseClient) {
        this.breedingBaseClient = breedingBaseClient;
    }

    @Override
    public String name() {
        return "batch_lookup";
    }

    @Override
    public String description() {
        return "Query breeding batch basic information by batch id.";
    }

    @Override
    public boolean supports(AgentToolRequest request) {
        String message = request.getLatestUserMessage();
        return BatchIdExtractor.extract(message).isPresent()
                && (containsAny(message, "查询", "基础信息", "批次", "鸡群", "batch")
                || !containsAny(message, "分析", "趋势", "均匀度", "料肉比"));
    }

    @Override
    public AgentToolResult execute(AgentToolRequest request) {
        String batchId = BatchIdExtractor.extract(request.getLatestUserMessage()).orElse("");
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("batchId", batchId);

        Optional<BreedingBatch> batch = breedingBaseClient.findBatchById(batchId);
        if (!batch.isPresent()) {
            return new AgentToolResult(name(), "未找到批次 " + batchId, arguments, Map.of("found", false));
        }

        BreedingBatch value = batch.get();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("found", true);
        data.put("batchId", value.getBatchId());
        data.put("organizationName", value.getOrganizationName());
        data.put("breedName", value.getBreedName());
        data.put("feedingMode", value.getFeedingMode());
        data.put("entryDate", value.getEntryDate().toString());
        data.put("plannedMarketDate", value.getPlannedMarketDate() == null ? "" : value.getPlannedMarketDate().toString());
        data.put("responsibleName", value.getResponsibleName());

        return new AgentToolResult(name(), "已查询到批次 " + batchId + " 的基础信息。", arguments, data);
    }

    private static boolean containsAny(String text, String... keywords) {
        String normalized = text == null ? "" : text.toLowerCase();
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
