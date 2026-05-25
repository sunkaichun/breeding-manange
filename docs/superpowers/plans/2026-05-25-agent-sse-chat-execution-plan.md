# 2026-05-25 Agent SSE Chat 执行计划

## 目标

新增一个通用 Agent Chat 接口，支持 SSE 流式输出、直连大模型，并允许 Agent 在对话过程中调用数据分析工具。该接口不是单一数据分析接口，而是一个可扩展的通用 Agent 入口。

## 范围

- 新增 `POST /api/agent/chat/stream`。
- 返回 `text/event-stream`。
- 支持普通聊天流式输出。
- 支持按需调用工具，并把工具结果注入模型上下文。
- 首批内置工具：
  - `breeding_analysis`：复用现有 `AnalysisGraph` 执行养殖分析。
  - `batch_lookup`：查询鸡群基础信息。
- 支持 `breeding.ai.provider=static` 时本地模拟流，支持 `breeding.ai.provider=openai` 时真实调用 OpenAI SDK/代理服务商。

## 非范围

- 不在本轮实现完整多轮会话持久化。
- 不在本轮实现复杂 function calling 协议闭环。
- 不在本轮实现前端页面。
- 不改造现有 Base 应用分析接口。
- 不把 Agent 限定为养殖分析专用入口。

## 接口契约

### 请求

```http
POST /api/agent/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

```json
{
  "conversationId": "local-001",
  "messages": [
    {
      "role": "user",
      "content": "帮我分析 BATCH-001 最近三天体重趋势，并给出建议"
    }
  ],
  "enableTools": true
}
```

### SSE 事件

```text
event: token
data: {"content":"正在分析"}

event: tool_call
data: {"toolName":"breeding_analysis","arguments":{"batchId":"BATCH-001"}}

event: tool_result
data: {"toolName":"breeding_analysis","summary":"..."}

event: done
data: {"status":"COMPLETED"}
```

错误事件：

```text
event: error
data: {"code":"AGENT_ERROR","message":"..."}
```

## 执行任务

### T-AGENT-01 定义通用 Agent Chat API 契约

产出：

- `AgentChatRequest`
- `AgentChatMessage`
- `AgentSseEvent`
- 错误事件模型

验收：

- 请求体能表达多轮消息。
- SSE 事件类型稳定。
- 字段命名适合前端直接消费。

### T-AGENT-02 实现 SSE Controller 骨架

产出：

- `AgentChatController`
- `POST /api/agent/chat/stream`
- `SseEmitter` 基础输出能力

验收：

- 接口返回 `text/event-stream`。
- 能输出 `token` 和 `done`。
- 异常时能输出 `error`。

### T-AGENT-03 实现 AgentChatService 编排层

产出：

- `AgentChatService`
- 流式回调接口
- 消息归一化逻辑
- 系统提示词拼装逻辑

验收：

- 普通聊天能不依赖工具直接输出。
- `enableTools=false` 时不会调用工具。
- 空消息、非法 role、空 content 能给出清晰错误。

### T-AGENT-04 抽象 AgentTool 接口

产出：

- `AgentTool`
- `AgentToolRequest`
- `AgentToolResult`

验收：

- 工具有名称、描述、匹配逻辑和执行逻辑。
- 工具结果可被注入模型上下文。
- 后续新增工具不需要改 Controller。

### T-AGENT-05 实现 breeding_analysis 工具

产出：

- `BreedingAnalysisAgentTool`
- 从问题中识别 `batchId`
- 从问题中识别分析类型：
  - 体重趋势 -> `WEIGHT_TREND`
  - 均匀度 -> `UNIFORMITY`
  - 料肉比 -> `FEED_CONVERSION_RATIO`

验收：

- 问题包含 `BATCH-001` 和“体重趋势”时能调用现有 `AnalysisGraph`。
- 工具结果包含风险等级、摘要、原因、建议。
- 日期范围缺省时使用当前已有本地测试数据范围。

### T-AGENT-06 实现 batch_lookup 工具

产出：

- `BatchLookupAgentTool`
- 查询 `BreedingBaseClient.findBatchById`

验收：

- 问题包含批次号时能返回鸡群基础信息。
- 未找到批次时返回可读的工具结果，而不是抛出未处理异常。

### T-AGENT-07 实现轻量工具路由

产出：

- `AgentToolRouter`
- 根据用户最新问题选择工具
- 支持 0 个或多个工具结果注入上下文

验收：

- 普通聊天不误触发分析工具。
- 明确批次查询触发 `batch_lookup`。
- 明确分析诉求触发 `breeding_analysis`。

### T-AGENT-08 实现 OpenAI 流式客户端

产出：

- `OpenAiStreamingChatClient`
- 复用现有 `OpenAiProperties`
- 支持代理服务商 `base-url`
- 支持流式 token 回调

验收：

- `breeding.ai.provider=openai` 时使用真实 OpenAI SDK。
- `api-key`、`base-url`、`model` 都从配置读取。
- 连接失败能转换为 SSE `error`。

### T-AGENT-09 接入模型降级策略

产出：

- `StaticStreamingChatClient`
- provider 分流配置

验收：

- `provider=static` 时本地无密钥也能测试 SSE。
- `provider=openai` 时走真实大模型。

### T-AGENT-10 增加测试

产出：

- Controller SSE 测试
- Service 编排测试
- 工具路由测试
- `breeding_analysis` 工具测试
- `batch_lookup` 工具测试
- OpenAI 配置绑定测试补充

验收：

- `mvn -pl ai-app -am -Dmaven.repo.local=.m2/repository test` 通过。
- 工具调用和非工具聊天路径都覆盖。

### T-AGENT-11 更新本地测试文档

产出：

- 更新 `docs/local-testing-guide.md`
- 新增 SSE curl 示例
- 新增 OpenAI 代理服务商流式测试命令
- 新增常见问题

验收：

- 用户可按文档本地启动并调用 SSE 接口。
- 明确说明 Spring Boot 参数必须放在 `-jar` 后面。

### T-AGENT-12 全量验证与提交说明

产出：

- 运行全量测试
- 整理变更摘要
- 给出提交命令

验收：

- `mvn -Dmaven.repo.local=.m2/repository test` 通过。
- 工作区只包含本轮相关改动。

## 建议执行顺序

第一批：

1. `T-AGENT-01`
2. `T-AGENT-02`
3. `T-AGENT-03`
4. `T-AGENT-09`
5. `T-AGENT-10` 的基础 SSE 测试

目标：先跑通不依赖真实模型和工具的 SSE 通道。

第二批：

1. `T-AGENT-04`
2. `T-AGENT-05`
3. `T-AGENT-06`
4. `T-AGENT-07`
5. 工具相关测试

目标：接入通用 Agent 工具能力。

第三批：

1. `T-AGENT-08`
2. `T-AGENT-11`
3. `T-AGENT-12`

目标：接入真实 OpenAI 流式输出，完善本地测试文档并全量验证。

## 风险与处理

- 代理服务商可能不完整支持 OpenAI SDK 的流式 Responses API。
  - 处理：先封装 `OpenAiStreamingChatClient`，必要时降级到非流式完整响应再分段输出。
- 轻量工具路由可能误判用户意图。
  - 处理：首版只对明确关键词和批次号触发工具，避免过度调用。
- SSE 流式接口测试容易受异步影响。
  - 处理：Controller 测试优先验证状态、content type 和关键事件片段。

## 本地验证命令

```bash
cd /Users/edy/IdeaProjects/zhitian

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin:$PATH \
mvn -pl ai-app -am -Dmaven.repo.local=.m2/repository test
```

启动：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar
```

SSE 调用示例：

```bash
curl -N -X POST http://localhost:8080/api/agent/chat/stream \
  -H 'Content-Type: application/json' \
  -H 'Accept: text/event-stream' \
  -d '{
    "conversationId": "local-001",
    "messages": [
      {
        "role": "user",
        "content": "帮我分析 BATCH-001 最近三天体重趋势，并给出建议"
      }
    ],
    "enableTools": true
  }'
```
