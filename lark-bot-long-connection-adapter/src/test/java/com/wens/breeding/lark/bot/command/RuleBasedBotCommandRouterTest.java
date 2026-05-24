package com.wens.breeding.lark.bot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.lark.bot.event.BotMessageEvent;

class RuleBasedBotCommandRouterTest {
    private final RuleBasedBotCommandRouter router = new RuleBasedBotCommandRouter();

    @Test
    void routesHelpCommand() {
        BotCommand command = router.route(event("help"));

        assertEquals(BotCommandType.HELP, command.getCommandType());
    }

    @Test
    void routesBatchQueryCommand() {
        BotCommand command = router.route(event("query BATCH-001 status"));

        assertEquals(BotCommandType.BATCH_QUERY, command.getCommandType());
        assertEquals("BATCH-001", command.getBatchId());
    }

    @Test
    void routesChineseWeightTrendAnalysisCommand() {
        BotCommand command = router.route(event("\u5206\u6790A\u6279\u6b21\u6700\u8fd17\u5929\u4f53\u91cd"));

        assertEquals(BotCommandType.ANALYSIS, command.getCommandType());
        assertEquals(AnalysisType.WEIGHT_TREND, command.getAnalysisType());
        assertEquals("A", command.getBatchId());
        assertEquals(7, command.getRecentDays());
    }

    @Test
    void routesEnglishFcrAnalysisCommand() {
        BotCommand command = router.route(event("analyze BATCH-002 FCR for recent 14 days"));

        assertEquals(BotCommandType.ANALYSIS, command.getCommandType());
        assertEquals(AnalysisType.FEED_CONVERSION_RATIO, command.getAnalysisType());
        assertEquals("BATCH-002", command.getBatchId());
        assertEquals(14, command.getRecentDays());
    }

    @Test
    void routesUnknownQuestionToKnowledgeQa() {
        BotCommand command = router.route(event("How should I adjust feeding after rain?"));

        assertEquals(BotCommandType.KNOWLEDGE_QA, command.getCommandType());
        assertEquals(AnalysisType.KNOWLEDGE_QA, command.getAnalysisType());
        assertTrue(command.getQuestion().contains("feeding"));
    }

    private static BotMessageEvent event(String content) {
        return new BotMessageEvent(
                "evt-001",
                "im.message.receive_v1",
                "ou_sender",
                "oc_chat",
                "group",
                "om_message",
                "text",
                content,
                "1779616799000",
                "1779616800000");
    }
}
