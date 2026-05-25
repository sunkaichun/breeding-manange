package com.wens.breeding.app.agent;

public final class StaticStreamingChatClient implements AgentChatClient {
    @Override
    public void stream(AgentChatPrompt prompt, AgentTokenSink tokenSink) {
        String response = "我已收到你的问题。"
                + "如果问题包含批次号或分析诉求，我会结合可用工具结果回答；"
                + "当前为本地 static 模式，未调用真实大模型。";
        for (String token : response.split("(?<=。|；|，)")) {
            if (!token.isEmpty()) {
                tokenSink.onToken(token);
            }
        }
    }
}
