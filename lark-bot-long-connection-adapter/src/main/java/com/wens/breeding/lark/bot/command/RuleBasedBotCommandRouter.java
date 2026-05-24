package com.wens.breeding.lark.bot.command;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.lark.bot.event.BotMessageEvent;

public final class RuleBasedBotCommandRouter implements BotCommandRouter {
    private static final int DEFAULT_RECENT_DAYS = 7;
    private static final Pattern CANONICAL_BATCH_ID = Pattern.compile("\\b(BATCH[-_A-Za-z0-9]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHINESE_BATCH_ID = Pattern.compile("([A-Za-z0-9_-]+)\\s*\\u6279\\u6b21");
    private static final Pattern ENGLISH_RECENT_DAYS = Pattern.compile("\\b(?:recent|last|past)\\s+(\\d{1,3})\\s+days?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHINESE_RECENT_DAYS = Pattern.compile("\\u6700\\u8fd1\\s*(\\d{1,3})\\s*\\u5929");

    @Override
    public BotCommand route(BotMessageEvent event) {
        Objects.requireNonNull(event, "event");
        String rawText = Texts.requireText(event.getContent(), "content").trim();
        String normalized = rawText.toLowerCase(Locale.ROOT);

        if (isHelp(rawText, normalized)) {
            return BotCommand.help(rawText);
        }

        Optional<String> batchId = extractBatchId(rawText);
        if (isAnalysis(rawText, normalized) && batchId.isPresent()) {
            return BotCommand.analysis(resolveAnalysisType(rawText, normalized), batchId.get(), extractRecentDays(rawText), rawText);
        }

        if (isBatchQuery(rawText, normalized) && batchId.isPresent()) {
            return BotCommand.batchQuery(batchId.get(), rawText);
        }

        return BotCommand.knowledgeQa(rawText, rawText);
    }

    private static boolean isHelp(String rawText, String normalized) {
        return "help".equals(normalized)
                || "/help".equals(normalized)
                || normalized.contains("commands")
                || rawText.contains("\u5e2e\u52a9")
                || rawText.contains("\u83dc\u5355");
    }

    private static boolean isAnalysis(String rawText, String normalized) {
        return normalized.contains("analyze")
                || normalized.contains("analysis")
                || normalized.contains("anomaly")
                || normalized.contains("abnormal")
                || normalized.contains("risk")
                || rawText.contains("\u5206\u6790")
                || rawText.contains("\u5f02\u5e38")
                || rawText.contains("\u9884\u8b66");
    }

    private static boolean isBatchQuery(String rawText, String normalized) {
        return normalized.contains("batch")
                || normalized.contains("status")
                || rawText.contains("\u67e5\u8be2")
                || rawText.contains("\u6279\u6b21");
    }

    private static Optional<String> extractBatchId(String rawText) {
        Matcher canonicalMatcher = CANONICAL_BATCH_ID.matcher(rawText);
        if (canonicalMatcher.find()) {
            return Optional.of(canonicalMatcher.group(1).toUpperCase(Locale.ROOT));
        }

        Matcher chineseMatcher = CHINESE_BATCH_ID.matcher(rawText);
        if (chineseMatcher.find()) {
            return Optional.of(chineseMatcher.group(1).toUpperCase(Locale.ROOT));
        }
        return Optional.empty();
    }

    private static int extractRecentDays(String rawText) {
        Matcher englishMatcher = ENGLISH_RECENT_DAYS.matcher(rawText);
        if (englishMatcher.find()) {
            return Integer.parseInt(englishMatcher.group(1));
        }

        Matcher chineseMatcher = CHINESE_RECENT_DAYS.matcher(rawText);
        if (chineseMatcher.find()) {
            return Integer.parseInt(chineseMatcher.group(1));
        }
        return DEFAULT_RECENT_DAYS;
    }

    private static AnalysisType resolveAnalysisType(String rawText, String normalized) {
        if (normalized.contains("fcr")
                || normalized.contains("feed conversion")
                || rawText.contains("\u6599\u8089\u6bd4")) {
            return AnalysisType.FEED_CONVERSION_RATIO;
        }
        if (normalized.contains("uniformity")
                || rawText.contains("\u5747\u5300")) {
            return AnalysisType.UNIFORMITY;
        }
        if (normalized.contains("market")
                || normalized.contains("listing")
                || rawText.contains("\u4e0a\u5e02")) {
            return AnalysisType.MARKET_READINESS;
        }
        if (normalized.contains("weight")
                || rawText.contains("\u4f53\u91cd")
                || rawText.contains("\u79f0\u91cd")) {
            return AnalysisType.WEIGHT_TREND;
        }
        if (normalized.contains("comprehensive")
                || normalized.contains("overall")
                || rawText.contains("\u7efc\u5408")
                || rawText.contains("\u6574\u4f53")) {
            return AnalysisType.COMPREHENSIVE;
        }
        return AnalysisType.COMPREHENSIVE;
    }
}
