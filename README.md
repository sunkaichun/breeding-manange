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

## Notes

The project targets Java 11 and Spring Boot 2.7.x because the local runtime is JDK 11.
