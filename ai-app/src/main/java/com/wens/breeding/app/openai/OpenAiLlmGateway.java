package com.wens.breeding.app.openai;

import java.util.Objects;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.wens.breeding.graph.llm.LlmGateway;
import com.wens.breeding.graph.llm.LlmGatewayException;
import com.wens.breeding.graph.llm.LlmRequest;
import com.wens.breeding.graph.llm.LlmResponse;

public final class OpenAiLlmGateway implements LlmGateway {
    private final OpenAIClient client;
    private final String modelName;

    public OpenAiLlmGateway(OpenAIClient client, String modelName) {
        this.client = Objects.requireNonNull(client, "client");
        this.modelName = requireText(modelName, "modelName");
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        Objects.requireNonNull(request, "request");
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(modelName)
                .instructions(request.getSystemPrompt())
                .input(input(request))
                .build();

        try {
            Response response = client.responses().create(params);
            String outputText = requireText(outputText(response), "OpenAI response output");
            return new LlmResponse(outputText, modelName, 0, 0, "");
        } catch (RuntimeException exception) {
            throw new LlmGatewayException("OpenAI model request failed: " + exception.getMessage(), true, exception);
        }
    }

    private static String input(LlmRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("User request:\n")
                .append(request.getUserPrompt());

        if (!request.getResponseSchema().trim().isEmpty()) {
            builder.append("\n\nReturn format:\n")
                    .append(request.getResponseSchema());
        }
        return builder.toString();
    }

    private static String outputText(Response response) {
        StringBuilder builder = new StringBuilder();
        for (ResponseOutputItem item : response.output()) {
            item.message().ifPresent(message -> appendMessageText(builder, message));
        }
        return builder.toString();
    }

    private static void appendMessageText(StringBuilder builder, ResponseOutputMessage message) {
        for (ResponseOutputMessage.Content content : message.content()) {
            content.outputText().ifPresent(outputText -> appendLine(builder, outputText.text()));
            content.refusal().ifPresent(refusal -> appendLine(builder, refusal.refusal()));
        }
    }

    private static void appendLine(StringBuilder builder, String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(text);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new LlmGatewayException(fieldName + " must not be blank", false);
        }
        return value;
    }
}
