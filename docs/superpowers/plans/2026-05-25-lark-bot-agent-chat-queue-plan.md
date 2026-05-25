# 飞书机器人长连接接入 Agent Chat 链路执行计划

## 背景

当前项目已经提供通用 Agent SSE 接口 `POST /api/agent/chat/stream`，由 `AgentChatService` 负责编排工具调用和模型流式输出。飞书机器人长连接适配器已有事件解析、幂等处理、命令路由和异步分析工作流，但机器人消息尚未统一接入通用 Agent chat 链路。

本次目标是把飞书长连接收到的文本消息转入 Agent chat 链路，并增加按会话串行的延迟队列，避免同一群聊或会话中短时间并发消费导致同时回复多条消息。

## 设计选择

采用方案 A：

```text
飞书长连接事件 -> 按 chatId 延迟串行队列 -> 事件幂等 -> BotAgentChatWorkflow -> AgentChatService -> 飞书单条回复
```

关键原则：

- 长连接适配器模块只定义 Bot 侧通用 chat port，不反向依赖 `ai-app`。
- `ai-app` 提供适配器，把 Bot chat port 桥接到现有 `AgentChatService`。
- 队列按 `chatId` 串行，同一会话内前一条消息未处理完成时后一条等待。
- 幂等处理放在队列实际消费阶段，避免异步入队后提前标记“已处理”。
- Bot 不发送“任务已接收”提示，Agent 输出汇总为一条最终飞书回复。
- 保留已有事件幂等层，队列只负责串行和延迟消费，不替代重复事件防护。

## 任务拆分

### T-BOT-CHAT-01 新增执行计划文档

- 新增本文档，明确链路、任务边界和验收标准。
- 验收：文档存在且可作为本轮实现检查清单。

### T-BOT-CHAT-02 定义 Bot 侧 Chat Port

- 在 `lark-bot-long-connection-adapter` 新增 `chat` 包。
- 定义 `BotAgentChatClient`、`BotAgentChatRequest`、`BotAgentChatMessage`、`BotAgentChatEventSink`。
- 验收：适配器模块不依赖 `ai-app`，测试可用 fake client 验证 workflow。

### T-BOT-CHAT-03 实现 BotAgentChatWorkflow

- 将 `BotMessageEvent` 转为 Bot chat request。
- 调用 `BotAgentChatClient.stream(...)` 收集 token。
- 只在完成后通过 `LarkImClient.sendText(...)` 发送一条最终回复。
- 非文本消息跳过。
- 异常时发送一条错误消息。
- 验收：文本消息只产生一条飞书回复，且请求 conversationId 使用 `chatId`。

### T-BOT-CHAT-04 实现按会话延迟串行队列

- 新增 `QueuedBotMessageEventHandler`。
- 支持配置延迟时间。
- 同一 `chatId` 的消息按提交顺序串行处理。
- 不同 `chatId` 可并行处理。
- 验收：单元测试证明同一会话不会并发执行 delegate。

### T-BOT-CHAT-05 在 ai-app 桥接 AgentChatService

- 新增 `AgentChatBotClient`，把 Bot chat request 转为 `AgentChatRequest`。
- 将 Agent token/tool 事件映射回 Bot event sink。
- 在 `AiAppConfiguration` 中装配：
  - `LarkImClient`
  - `BotAgentChatClient`
  - `BotAgentChatWorkflow`
  - `QueuedBotMessageEventHandler`
  - `IdempotentBotMessageEventHandler`
  - `BotMessageEventLineHandler`
- 验收：Spring Boot 上下文可启动，测试模式不依赖真实 OpenAI。

### T-BOT-CHAT-06 配置与文档更新

- 增加队列延迟和线程数配置。
- 更新 README 的飞书机器人链路图，体现队列和 Agent chat 接入。
- 验收：文档中能看清新链路和配置入口。

### T-BOT-CHAT-07 测试与提交

- 运行适配器模块测试。
- 运行 `ai-app` 及依赖测试。
- 运行全量 Maven 测试。
- 提交代码。
- 验收：`BUILD SUCCESS`，git 工作区干净。
