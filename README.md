# Breeding AI Backend

Java AI backend for the Feishu Base breeding management project.

## Current Milestone

The current milestone upgrades the backend to a Java 21 / Spring Boot 3.5
multi-module project with:

- Spring Boot application module: `ai-app`
- AI graph runtime module: `ai-graph-runtime`
- Feishu Base client module: `lark-base-client`
- Feishu IM client module: `lark-im-client`
- Feishu bot long-connection adapter module: `lark-bot-long-connection-adapter`
- Analysis domain module: `analysis-domain`
- RAG knowledge module: `rag-knowledge`
- Visualization service module: `visualization-service`
- Task orchestration module: `task-orchestrator`
- Authorization and security module: `auth-security`

## Build

Use a workspace-local Maven repository if the default `~/.m2` path is restricted:

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin:$PATH \
mvn -Dmaven.repo.local=.m2/repository test
```

The app module exposes a basic health endpoint:

```text
GET /api/health
```

## OpenAI Model Integration

The app can use the official OpenAI Java SDK through the shared `LlmGateway`
abstraction. It is disabled by default for local tests. Enable the direct SDK
adapter with:

```bash
export OPENAI_API_KEY=your_api_key
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --breeding.ai.provider=openai \
  --breeding.ai.openai.model=gpt-5.2 \
  --breeding.ai.openai.base-url=https://your-provider.example/v1
```

For backward compatibility, `--breeding.ai.openai.enabled=true` also selects
the direct OpenAI SDK adapter when `breeding.ai.provider` remains `static`.
The OpenAI SDK adapter is fully configuration driven:

- `breeding.ai.openai.api-key`
- `breeding.ai.openai.base-url`
- `breeding.ai.openai.model`
- `breeding.ai.openai.organization`
- `breeding.ai.openai.project`
- `breeding.ai.openai.timeout`
- `breeding.ai.openai.client-max-retries`

## AI Execution Layer

The analysis runtime now has a traceable execution layer under
`ai-graph-runtime/src/main/java/com/wens/breeding/graph/execution`.

- `AiExecutionEngine` is the stable execution port.
- `NativeLangGraph4jExecutionEngine` runs the breeding analysis graph through
  LangGraph4j's native `StateGraph` runtime.
- `NodeBasedAiExecutionEngine` remains as a lightweight node runner and still
  implements `AnalysisGraph` for the existing bot and Base app callers.
- `BreedingAnalysisExecutionGraphFactory.nativeLangGraph4j(...)` builds the
  current breeding analysis graph with request persistence, batch loading,
  rule analysis, and result persistence nodes.
- `LangChain4jLlmGateway`, `SpringAiLlmGateway`, and `OpenAiLlmGateway` adapt
  external model clients into the same project `LlmGateway` port.
- `breeding.ai.provider` selects `static`, `openai`, or `spring-ai` at runtime.

## Notes

The project targets Java 21 and Spring Boot 3.5.x so the AI execution layer can
use current LangGraph4j, LangChain4j, and Spring AI integrations.
