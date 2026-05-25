package com.wens.breeding.app.agent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.analysis.model.RequestSource;
import com.wens.breeding.graph.AnalysisGraph;

import org.springframework.stereotype.Component;

@Component
public final class BreedingAnalysisAgentTool implements AgentTool {
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}\\b");
    private static final LocalDate DEFAULT_START_DATE = LocalDate.parse("2026-05-20");
    private static final LocalDate DEFAULT_END_DATE = LocalDate.parse("2026-05-22");

    private final AnalysisGraph analysisGraph;

    public BreedingAnalysisAgentTool(AnalysisGraph analysisGraph) {
        this.analysisGraph = analysisGraph;
    }

    @Override
    public String name() {
        return "breeding_analysis";
    }

    @Override
    public String description() {
        return "Run breeding data analysis for a batch, including weight trend, uniformity, and feed conversion ratio.";
    }

    @Override
    public boolean supports(AgentToolRequest request) {
        String message = request.getLatestUserMessage();
        return BatchIdExtractor.extract(message).isPresent()
                && containsAny(message, "分析", "趋势", "体重", "均匀度", "料肉比", "fcr", "风险", "建议");
    }

    @Override
    public AgentToolResult execute(AgentToolRequest request) {
        String message = request.getLatestUserMessage();
        String batchId = BatchIdExtractor.extract(message)
                .orElseThrow(() -> new IllegalArgumentException("batchId is required for breeding analysis"));
        AnalysisType analysisType = analysisType(message);
        DateRange dateRange = dateRange(message);
        AnalysisRequest analysisRequest = new AnalysisRequest(
                "AGENT-" + UUID.randomUUID(),
                RequestSource.MANUAL_TEST,
                "agent",
                batchId,
                analysisType,
                dateRange.startDate,
                dateRange.endDate,
                message);
        AnalysisResult result = analysisGraph.run(analysisRequest);

        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("batchId", batchId);
        arguments.put("analysisType", analysisType.name());
        arguments.put("startDate", dateRange.startDate.toString());
        arguments.put("endDate", dateRange.endDate.toString());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("requestId", result.getRequestId());
        data.put("riskLevel", result.getRiskLevel().name());
        data.put("summary", result.getSummary());
        data.put("reasons", result.getReasons());
        data.put("suggestions", result.getSuggestions());

        return new AgentToolResult(name(), result.getSummary(), arguments, data);
    }

    private static AnalysisType analysisType(String message) {
        String normalized = message == null ? "" : message.toLowerCase();
        if (normalized.contains("均匀度") || normalized.contains("uniformity")) {
            return AnalysisType.UNIFORMITY;
        }
        if (normalized.contains("料肉比") || normalized.contains("fcr") || normalized.contains("feed conversion")) {
            return AnalysisType.FEED_CONVERSION_RATIO;
        }
        return AnalysisType.WEIGHT_TREND;
    }

    private static DateRange dateRange(String message) {
        Matcher matcher = DATE_PATTERN.matcher(message == null ? "" : message);
        Optional<LocalDate> first = matcher.find() ? Optional.of(LocalDate.parse(matcher.group())) : Optional.empty();
        Optional<LocalDate> second = matcher.find() ? Optional.of(LocalDate.parse(matcher.group())) : Optional.empty();
        LocalDate startDate = first.orElse(DEFAULT_START_DATE);
        LocalDate endDate = second.orElse(DEFAULT_END_DATE);
        if (endDate.isBefore(startDate)) {
            return new DateRange(endDate, startDate);
        }
        return new DateRange(startDate, endDate);
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

    private static final class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        private DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
