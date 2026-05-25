package com.wens.breeding.app.agent;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/agent")
public class AgentChatController {
    private static final long SSE_TIMEOUT_MS = 120_000L;

    private final AgentChatService agentChatService;
    private final ObjectMapper objectMapper;

    public AgentChatController(AgentChatService agentChatService, ObjectMapper objectMapper) {
        this.agentChatService = agentChatService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(path = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody AgentChatRequest request) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        CompletableFuture.runAsync(() -> streamToEmitter(request, emitter));
        return emitter;
    }

    private void streamToEmitter(AgentChatRequest request, SseEmitter emitter) {
        try {
            agentChatService.stream(request, new SseAgentEventSink(emitter));
            send(emitter, "done", Map.of("status", "COMPLETED"));
            emitter.complete();
        } catch (Exception exception) {
            try {
                send(emitter, "error", Map.of(
                        "code", "AGENT_ERROR",
                        "message", exception.getMessage() == null ? "Agent stream failed" : exception.getMessage()));
            } catch (IOException ignored) {
                // The client may have already disconnected.
            }
            emitter.complete();
        }
    }

    private void send(SseEmitter emitter, String eventName, Map<String, Object> payload) throws IOException {
        emitter.send(SseEmitter.event()
                .name(eventName)
                .data(toJson(payload), MediaType.APPLICATION_JSON));
    }

    private String toJson(Map<String, Object> payload) throws JsonProcessingException {
        return objectMapper.writeValueAsString(payload);
    }

    private final class SseAgentEventSink implements AgentEventSink {
        private final SseEmitter emitter;

        private SseAgentEventSink(SseEmitter emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onToken(String token) {
            if (token == null || token.isEmpty()) {
                return;
            }
            try {
                send(emitter, "token", Map.of("content", token));
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to send token event", exception);
            }
        }

        @Override
        public void onToolCall(String toolName, Map<String, Object> arguments) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("toolName", toolName);
            payload.put("arguments", arguments);
            try {
                send(emitter, "tool_call", payload);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to send tool_call event", exception);
            }
        }

        @Override
        public void onToolResult(AgentToolResult result) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("toolName", result.getToolName());
            payload.put("summary", result.getSummary());
            payload.put("data", result.getData());
            try {
                send(emitter, "tool_result", payload);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to send tool_result event", exception);
            }
        }
    }
}
