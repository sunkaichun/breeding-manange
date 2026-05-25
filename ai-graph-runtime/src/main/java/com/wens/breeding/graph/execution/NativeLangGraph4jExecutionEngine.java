package com.wens.breeding.graph.execution;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;

import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.graph.AnalysisGraph;
import com.wens.breeding.graph.AnalysisGraphException;

public final class NativeLangGraph4jExecutionEngine implements AiExecutionEngine, AnalysisGraph {
    private static final String CONTEXT_KEY = "context";
    private static final String TRACES_KEY = "traces";

    private final List<AiExecutionNode> nodes;
    private final CompiledGraph<AgentState> compiledGraph;

    public NativeLangGraph4jExecutionEngine(List<AiExecutionNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("nodes must not be empty");
        }
        this.nodes = List.copyOf(nodes);
        this.compiledGraph = compile(this.nodes);
    }

    @Override
    public AiExecutionResult execute(AnalysisRequest request) {
        Objects.requireNonNull(request, "request");
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(CONTEXT_KEY, new AiExecutionContext(request));
        initialState.put(TRACES_KEY, new ArrayList<AiExecutionTrace>());

        try {
            Optional<AgentState> finalState = compiledGraph.invoke(initialState);
            if (!finalState.isPresent()) {
                throw new AnalysisGraphException("LangGraph4j execution finished without a final state");
            }

            AiExecutionContext context = context(finalState.get());
            if (!context.hasAnalysisResult()) {
                throw new AnalysisGraphException("LangGraph4j execution finished without an analysis result");
            }
            return new AiExecutionResult(AiFramework.LANGGRAPH4J, context.getAnalysisResult(), traces(finalState.get()));
        } catch (AnalysisGraphException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AnalysisGraphException("LangGraph4j execution failed", exception);
        }
    }

    @Override
    public AnalysisResult run(AnalysisRequest request) {
        return execute(request).getAnalysisResult();
    }

    private static CompiledGraph<AgentState> compile(List<AiExecutionNode> nodes) {
        try {
            StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);
            String previous = START;
            for (AiExecutionNode node : nodes) {
                graph.addNode(node.name(), node_async(state -> executeNode(state, node)));
                graph.addEdge(previous, node.name());
                previous = node.name();
            }
            graph.addEdge(previous, END);
            return graph.compile();
        } catch (GraphStateException exception) {
            throw new AnalysisGraphException("Unable to compile LangGraph4j graph", exception);
        }
    }

    private static Map<String, Object> executeNode(AgentState state, AiExecutionNode node) {
        AiExecutionContext context = context(state);
        List<AiExecutionTrace> traces = new ArrayList<>(traces(state));
        AiExecutionNodeResult result = node.execute(context);
        traces.add(new AiExecutionTrace(node.name(), result.getStatus(), result.getMessage()));

        Map<String, Object> update = new HashMap<>();
        update.put(CONTEXT_KEY, context);
        update.put(TRACES_KEY, traces);
        return update;
    }

    private static AiExecutionContext context(AgentState state) {
        return state.value(CONTEXT_KEY)
                .filter(AiExecutionContext.class::isInstance)
                .map(AiExecutionContext.class::cast)
                .orElseThrow(() -> new AnalysisGraphException("LangGraph4j state is missing execution context"));
    }

    @SuppressWarnings("unchecked")
    private static List<AiExecutionTrace> traces(AgentState state) {
        return state.value(TRACES_KEY)
                .filter(List.class::isInstance)
                .map(value -> (List<AiExecutionTrace>) value)
                .orElseGet(ArrayList::new);
    }
}
