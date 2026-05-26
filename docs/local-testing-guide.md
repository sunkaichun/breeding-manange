# 本地测试指南

本文档用于在本地验证 breeding-ai-backend 的构建、单元测试、应用启动、HTTP 接口，以及 OpenAI 代理服务商配置。

## 1. 环境要求

- JDK 21
- Maven
- 当前项目目录：`/Users/edy/IdeaProjects/zhitian`

建议本地命令显式指定 Java 21，避免系统默认 Java 版本仍然是 11：

```bash
cd /Users/edy/IdeaProjects/zhitian

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

java -version
echo $JAVA_HOME
```

期望 `java -version` 输出 Java 21。

## 2. 运行全量测试

```bash
cd /Users/edy/IdeaProjects/zhitian

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin:$PATH \
mvn -Dmaven.repo.local=.m2/repository test
```

期望结果：

```text
BUILD SUCCESS
```

只测试应用模块及其依赖：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin:$PATH \
mvn -pl ai-app -am -Dmaven.repo.local=.m2/repository test
```

## 3. 测试 MySQL 持久化

本地 MySQL 环境默认配置：

```text
Host: 127.0.0.1
Port: 3306
Database: app_dev
User: dev
Password: devpass
Version: MySQL 8.0.35
```

先确认数据库可连接：

```bash
mysql -h 127.0.0.1 -P 3306 -u dev -pdevpass app_dev -e 'SELECT VERSION();'
```

单独运行 MySQL 持久化模块测试：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin:$PATH \
mvn -pl mysql-persistence -am -Dmaven.repo.local=.m2/repository test
```

测试会自动执行 `mysql-persistence/src/main/resources/db/mysql/schema.sql` 建表，并写入 `IT-` 前缀测试数据，结束后清理。
如果本地 MySQL 不可用，该集成测试会自动跳过。

应用切换到 MySQL 存储模式：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --breeding.storage.provider=mysql \
  --spring.datasource.url='jdbc:mysql://127.0.0.1:3306/app_dev?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true' \
  --spring.datasource.username=dev \
  --spring.datasource.password=devpass
```

也可以通过环境变量配置：

```bash
export BREEDING_STORAGE_PROVIDER=mysql
export MYSQL_URL='jdbc:mysql://127.0.0.1:3306/app_dev?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'
export MYSQL_USER=dev
export MYSQL_PASSWORD=devpass
```

当前 MySQL 存储覆盖的对象：

- 鸡群批次：`breeding_batches`
- 称重记录：`weight_records`
- 养殖标准：`breeding_standards`
- 料肉比记录：`fcr_records`
- 料肉比标准：`fcr_standards`
- AI 分析请求：`analysis_requests`
- AI 分析结果：`analysis_results`
- 可视化数据：`visualization_data_records`
- 异步任务记录：`task_records`

## 4. 本地打包

```bash
cd /Users/edy/IdeaProjects/zhitian

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin:$PATH \
mvn -Dmaven.repo.local=.m2/repository package
```

打包后应用 jar 位于：

```text
ai-app/target/ai-app-0.1.0-SNAPSHOT.jar
```

## 5. 启动应用

默认使用静态模型响应，不调用真实 OpenAI：

```bash
cd /Users/edy/IdeaProjects/zhitian

java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar
```

如果端口 `8080` 被占用：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --server.port=8081
```

注意：`--server.port`、`--breeding.ai.provider` 这类 Spring Boot 参数必须放在 `-jar xxx.jar` 后面。

## 6. 健康检查

```bash
curl http://localhost:8080/api/health
```

期望返回类似：

```json
{
  "status": "UP",
  "service": "breeding-ai-backend",
  "timestamp": "2026-05-25T00:00:00Z"
}
```

## 7. 测试 Base 应用分析接口

```bash
curl -X POST http://localhost:8080/api/lark/base-app/analysis-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "requestId": "LOCAL-REQ-001",
    "requesterOpenId": "ou_test",
    "batchId": "BATCH-001",
    "analysisType": "WEIGHT_TREND",
    "startDate": "2026-05-20",
    "endDate": "2026-05-22",
    "rawQuestion": "分析最近三天体重趋势"
  }'
```

期望返回包含：

```json
{
  "requestId": "LOCAL-REQ-001",
  "status": "COMPLETED",
  "riskLevel": "HIGH"
}
```

可选 `analysisType`：

- `WEIGHT_TREND`
- `UNIFORMITY`
- `FEED_CONVERSION_RATIO`
- `MARKET_READINESS`
- `KNOWLEDGE_QA`
- `COMPREHENSIVE`

## 8. 测试通用 Agent SSE 流式接口

通用 Agent 接口支持普通聊天，也支持在问题命中批次号、体重趋势、均匀度、料肉比等场景时调用后端数据分析工具。

先用静态模型模式启动，避免本地测试依赖真实模型：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --breeding.ai.provider=static \
  --breeding.ai.openai.enabled=false
```

调用 SSE 接口：

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

期望看到流式 SSE 事件：

```text
event:tool_call
data:{"toolName":"breeding_analysis",...}

event:tool_result
data:{"toolName":"breeding_analysis",...}

event:token
data:{"content":"..."}

event:done
data:{"status":"COMPLETED"}
```

普通聊天可以关闭工具调用：

```bash
curl -N -X POST http://localhost:8080/api/agent/chat/stream \
  -H 'Content-Type: application/json' \
  -H 'Accept: text/event-stream' \
  -d '{
    "conversationId": "local-002",
    "messages": [
      {
        "role": "user",
        "content": "你好，介绍一下你能做什么"
      }
    ],
    "enableTools": false
  }'
```

如果要直连 OpenAI 或代理服务商测试真实模型流式输出：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --breeding.ai.provider=openai \
  --breeding.ai.openai.enabled=true \
  --breeding.ai.openai.api-key="$OPENAI_API_KEY" \
  --breeding.ai.openai.base-url="$OPENAI_BASE_URL" \
  --breeding.ai.openai.model=gpt-5.4
```

## 9. 使用 OpenAI 代理服务商测试

如果你的模型通过服务商代理访问，需要配置 `base-url`、`api-key` 和 `model`。

推荐用环境变量保存密钥：

```bash
export OPENAI_API_KEY='你的代理服务商 key'
export OPENAI_BASE_URL='https://你的代理服务商地址/v1'
```

启动应用：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --breeding.ai.provider=openai \
  --breeding.ai.openai.model=你的模型名称 \
  --breeding.ai.openai.api-key="$OPENAI_API_KEY" \
  --breeding.ai.openai.base-url="$OPENAI_BASE_URL"
```

也可以写成 JVM 系统属性，但这些参数必须放在 `-jar` 前面：

```bash
java \
  -Dbreeding.ai.provider=openai \
  -Dbreeding.ai.openai.model=你的模型名称 \
  -Dbreeding.ai.openai.api-key="$OPENAI_API_KEY" \
  -Dbreeding.ai.openai.base-url="$OPENAI_BASE_URL" \
  -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar
```

可配置项：

```yaml
breeding:
  ai:
    provider: openai
    openai:
      api-key: your_proxy_key
      base-url: https://your-provider.example/v1
      model: your_model_name
      organization:
      project:
      timeout: 30s
      client-max-retries: 2
      max-attempts: 2
```

## 10. 配置飞书机器人长连接参数

飞书机器人应用和长连接消费参数统一通过配置文件或环境变量管理：

```bash
export LARK_BOT_APP_ID='cli_xxx'
export LARK_BOT_APP_SECRET='你的飞书机器人 app secret'
export LARK_BOT_VERIFICATION_TOKEN='事件订阅 verification token'
export LARK_BOT_ENCRYPT_KEY='事件订阅 encrypt key'
export LARK_BOT_OPEN_ID='机器人 open_id'
export LARK_BOT_CONSUMER_ENABLED='true'
export LARK_CLI_PATH='lark-cli'
export LARK_BOT_EVENT_IDENTITY='BOT'
```

对应配置项：

```yaml
breeding:
  lark:
    bot:
      app:
        app-id: ${LARK_BOT_APP_ID:}
        app-secret: ${LARK_BOT_APP_SECRET:}
        verification-token: ${LARK_BOT_VERIFICATION_TOKEN:}
        encrypt-key: ${LARK_BOT_ENCRYPT_KEY:}
        bot-open-id: ${LARK_BOT_OPEN_ID:}
      consumer:
        enabled: ${LARK_BOT_CONSUMER_ENABLED:false}
        cli-path: ${LARK_CLI_PATH:lark-cli}
        event-key: ${LARK_BOT_EVENT_KEY:im.message.receive_v1}
        identity: ${LARK_BOT_EVENT_IDENTITY:BOT}
        max-events: ${LARK_BOT_MAX_EVENTS:0}
        timeout: ${LARK_BOT_EVENT_TIMEOUT:}
        jq-expression: ${LARK_BOT_EVENT_JQ:}
        ready-timeout: ${LARK_BOT_READY_TIMEOUT:10s}
      queue-delay: 500ms
      queue-threads: 2
```

其中 `queue-delay` 和 `queue-threads` 用于控制机器人消息延迟串行消费，防止同一会话短时间内并发回复多条消息。

## 11. 常见问题

### Unrecognized option: --breeding.ai.provider=openai

原因：Spring Boot 参数放在了 `-jar` 前面，被 JVM 当成自己的参数解析。

错误示例：

```bash
java --breeding.ai.provider=openai -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar
```

正确示例：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --breeding.ai.provider=openai
```

### java -version 仍然是 11

当前 shell 没切到 Java 21。使用：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
java -version
```

### 端口 8080 被占用

换端口启动：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --server.port=8081
```

然后访问：

```bash
curl http://localhost:8081/api/health
```

### OpenAI 代理返回 401 或鉴权失败

检查：

- `breeding.ai.openai.api-key` 是否是代理服务商的 key。
- `breeding.ai.openai.base-url` 是否包含服务商要求的 `/v1`。
- `breeding.ai.openai.model` 是否是服务商支持的模型名称。

### OpenAI 代理超时

调大超时时间：

```bash
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --breeding.ai.provider=openai \
  --breeding.ai.openai.timeout=60s
```

## 12. 提交前检查

每次提交前建议运行：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin:$PATH \
mvn -Dmaven.repo.local=.m2/repository test

git status --short
```
