package com.wens.breeding.app.agent;

import com.openai.client.OpenAIClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseErrorEvent;
import com.openai.models.responses.ResponseStreamEvent;

public final class OpenAiStreamingChatClient implements AgentChatClient {
    private final OpenAIClient client;
    private final String modelName;

    public OpenAiStreamingChatClient(OpenAIClient client, String modelName) {
        this.client = client;
        this.modelName = requireText(modelName, "modelName");
    }

    @Override
    public void stream(AgentChatPrompt prompt, AgentTokenSink tokenSink) {
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(modelName)
                .instructions(prompt.getSystemPrompt())
                .input(prompt.getUserPrompt())
                .build();

        try (StreamResponse<ResponseStreamEvent> stream = client.responses().createStreaming(params)) {
            stream.stream().forEach(event -> handleEvent(event, tokenSink));
        }
    }

    private static void handleEvent(ResponseStreamEvent event, AgentTokenSink tokenSink) {
        event.outputTextDelta().ifPresent(delta -> tokenSink.onToken(delta.delta()));
        event.error().ifPresent(OpenAiStreamingChatClient::throwError);
    }

    private static void throwError(ResponseErrorEvent event) {
        String code = event.code().orElse("OPENAI_STREAM_ERROR");
        throw new IllegalStateException(code + ": " + event.message());
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
