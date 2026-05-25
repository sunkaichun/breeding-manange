package com.wens.breeding.app.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.wens.breeding.lark.bot.chat.BotAgentChatEventSink;
import com.wens.breeding.lark.bot.chat.BotAgentChatRequest;

import org.junit.jupiter.api.Test;

class AgentChatBotClientTest {
    @Test
    void bridgesBotRequestToAgentChatService() {
        CapturingAgentChatClient agentChatClient = new CapturingAgentChatClient();
        AgentChatService agentChatService = new AgentChatService(new AgentToolRouter(List.of()), agentChatClient);
        AgentChatBotClient botClient = new AgentChatBotClient(agentChatService);
        RecordingBotSink sink = new RecordingBotSink();

        botClient.stream(BotAgentChatRequest.singleUserMessage("oc_chat", "你好"), sink);

        assertTrue(agentChatClient.lastPrompt.getUserPrompt().contains("user: 你好"));
        assertEquals(List.of("agent ", "reply"), sink.tokens);
    }

    private static final class CapturingAgentChatClient implements AgentChatClient {
        private AgentChatPrompt lastPrompt;

        @Override
        public void stream(AgentChatPrompt prompt, AgentTokenSink tokenSink) {
            this.lastPrompt = prompt;
            tokenSink.onToken("agent ");
            tokenSink.onToken("reply");
        }
    }

    private static final class RecordingBotSink implements BotAgentChatEventSink {
        private final List<String> tokens = new ArrayList<>();

        @Override
        public void onToken(String token) {
            tokens.add(token);
        }

        @Override
        public void onToolCall(String toolName) {
        }

        @Override
        public void onToolResult(String toolName, String summary) {
        }
    }
}
