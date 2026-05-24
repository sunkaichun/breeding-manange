# Breeding AI Backend

Java AI backend for the Feishu Base breeding management project.

## Current Milestone

`T-AI-01` initializes a Java 11 compatible Maven multi-module project with:

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
mvn -Dmaven.repo.local=.m2/repository test
```

The app module exposes a basic health endpoint:

```text
GET /api/health
```

## OpenAI Model Integration

The app can use the official OpenAI Java SDK through the shared `LlmGateway`
abstraction. It is disabled by default for local tests. Enable it with:

```bash
export OPENAI_API_KEY=your_api_key
java -jar ai-app/target/ai-app-0.1.0-SNAPSHOT.jar \
  --breeding.ai.openai.enabled=true \
  --breeding.ai.openai.model=gpt-5.2
```

## AI Execution Layer

The analysis runtime now has a traceable execution layer under
`ai-graph-runtime/src/main/java/com/wens/breeding/graph/execution`.

- `AiExecutionEngine` is the stable execution port.
- `NodeBasedAiExecutionEngine` runs analysis as stateful nodes and still
  implements `AnalysisGraph` for the existing bot and Base app callers.
- `BreedingAnalysisExecutionGraphFactory.langGraphStyle(...)` builds the
  current breeding analysis graph with request persistence, batch loading,
  rule analysis, and result persistence nodes.
- `AiFrameworkRegistry` records the planned framework roles for LangGraph4j,
  LangChain4j, Spring AI, and the direct OpenAI SDK adapter.

The current local runtime is Java 11, so native LangGraph4j, LangChain4j, and
Spring AI dependencies are not enabled in Maven yet. Their current integration
line should be activated after moving the project to Java 17 and Spring Boot 3.

## Notes

The project targets Java 11 and Spring Boot 2.7.x because the local runtime is JDK 11.
