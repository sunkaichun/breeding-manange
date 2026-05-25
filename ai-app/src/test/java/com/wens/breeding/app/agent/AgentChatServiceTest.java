package com.wens.breeding.app.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class AgentChatServiceTest {
    @Test
    void streamsGeneralChatWithoutTools() {
        CapturingChatClient chatClient = new CapturingChatClient("ok");
        AgentChatService service = new AgentChatService(new AgentToolRouter(List.of(new MatchingTool())), chatClient);
        RecordingSink sink = new RecordingSink();

        service.stream(request("普通聊天", false), sink);

        assertEquals(List.of("ok"), sink.tokens);
        assertTrue(sink.toolCalls.isEmpty());
        assertTrue(sink.toolResults.isEmpty());
        assertFalse(chatClient.lastPrompt.getUserPrompt().contains("Tool context"));
    }

    @Test
    void injectsToolResultsWhenToolsAreEnabled() {
        CapturingChatClient chatClient = new CapturingChatClient("done");
        AgentChatService service = new AgentChatService(new AgentToolRouter(List.of(new MatchingTool())), chatClient);
        RecordingSink sink = new RecordingSink();

        service.stream(request("请查询 BATCH-001", true), sink);

        assertEquals(List.of("done"), sink.tokens);
        assertEquals(1, sink.toolCalls.size());
        assertEquals(1, sink.toolResults.size());
        assertTrue(chatClient.lastPrompt.getUserPrompt().contains("Tool context"));
        assertTrue(chatClient.lastPrompt.getUserPrompt().contains("matched"));
    }

    @Test
    void rejectsInvalidMessages() {
        AgentChatService service = new AgentChatService(new AgentToolRouter(List.of()), new CapturingChatClient("ok"));
        AgentChatRequest request = new AgentChatRequest();

        assertThrows(IllegalArgumentException.class, () -> service.stream(request, new RecordingSink()));
    }

    private static AgentChatRequest request(String content, boolean enableTools) {
        AgentChatMessage message = new AgentChatMessage();
        message.setRole("user");
        message.setContent(content);
        AgentChatRequest request = new AgentChatRequest();
        request.setConversationId("test");
        request.setMessages(List.of(message));
        request.setEnableTools(enableTools);
        return request;
    }

    private static final class CapturingChatClient implements AgentChatClient {
        private final String token;
        private AgentChatPrompt lastPrompt;

        private CapturingChatClient(String token) {
            this.token = token;
        }

        @Override
        public void stream(AgentChatPrompt prompt, AgentTokenSink tokenSink) {
            this.lastPrompt = prompt;
            tokenSink.onToken(token);
        }
    }

    private static final class MatchingTool implements AgentTool {
        @Override
        public String name() {
            return "matching_tool";
        }

        @Override
        public String description() {
            return "test tool";
        }

        @Override
        public boolean supports(AgentToolRequest request) {
            return request.getLatestUserMessage().contains("BATCH-001");
        }

        @Override
        public AgentToolResult execute(AgentToolRequest request) {
            return new AgentToolResult(name(), "matched", Map.of("batchId", "BATCH-001"), Map.of("found", true));
        }
    }

    private static final class RecordingSink implements AgentEventSink {
        private final List<String> tokens = new ArrayList<>();
        private final List<String> toolCalls = new ArrayList<>();
        private final List<AgentToolResult> toolResults = new ArrayList<>();

        @Override
        public void onToken(String token) {
            tokens.add(token);
        }

        @Override
        public void onToolCall(String toolName, Map<String, Object> arguments) {
            toolCalls.add(toolName);
        }

        @Override
        public void onToolResult(AgentToolResult result) {
            toolResults.add(result);
        }
    }
}
