package com.wens.breeding.lark.bot.command;

import com.wens.breeding.analysis.model.AnalysisType;

public final class BotCommand {
    private final BotCommandType commandType;
    private final AnalysisType analysisType;
    private final String batchId;
    private final int recentDays;
    private final String question;
    private final String rawText;

    public BotCommand(
            BotCommandType commandType,
            AnalysisType analysisType,
            String batchId,
            int recentDays,
            String question,
            String rawText) {
        this.commandType = commandType == null ? BotCommandType.KNOWLEDGE_QA : commandType;
        this.analysisType = analysisType;
        this.batchId = batchId == null ? "" : batchId;
        this.recentDays = recentDays;
        this.question = question == null ? "" : question;
        this.rawText = Texts.requireText(rawText, "rawText");
        if (recentDays < 0) {
            throw new IllegalArgumentException("recentDays must be non-negative");
        }
    }

    public static BotCommand help(String rawText) {
        return new BotCommand(BotCommandType.HELP, null, "", 0, "", rawText);
    }

    public static BotCommand batchQuery(String batchId, String rawText) {
        return new BotCommand(BotCommandType.BATCH_QUERY, null, Texts.requireText(batchId, "batchId"), 0, "", rawText);
    }

    public static BotCommand analysis(AnalysisType analysisType, String batchId, int recentDays, String rawText) {
        return new BotCommand(
                BotCommandType.ANALYSIS,
                analysisType == null ? AnalysisType.COMPREHENSIVE : analysisType,
                Texts.requireText(batchId, "batchId"),
                recentDays,
                "",
                rawText);
    }

    public static BotCommand knowledgeQa(String question, String rawText) {
        return new BotCommand(
                BotCommandType.KNOWLEDGE_QA,
                AnalysisType.KNOWLEDGE_QA,
                "",
                0,
                Texts.requireText(question, "question"),
                rawText);
    }

    public BotCommandType getCommandType() {
        return commandType;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public String getBatchId() {
        return batchId;
    }

    public int getRecentDays() {
        return recentDays;
    }

    public String getQuestion() {
        return question;
    }

    public String getRawText() {
        return rawText;
    }

    public boolean hasBatchId() {
        return !batchId.isEmpty();
    }
}
