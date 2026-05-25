package com.wens.breeding.app.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(AgentChatController.class)
class AgentChatControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentChatService agentChatService;

    @Test
    void streamsSseEvents() throws Exception {
        doAnswer(invocation -> {
            AgentEventSink sink = invocation.getArgument(1);
            sink.onToken("hello");
            return null;
        }).when(agentChatService).stream(any(AgentChatRequest.class), any(AgentEventSink.class));

        MvcResult result = mockMvc.perform(post("/api/agent/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content("{"
                                + "\"conversationId\":\"local-001\","
                                + "\"messages\":[{\"role\":\"user\",\"content\":\"hello\"}],"
                                + "\"enableTools\":false"
                                + "}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("event:token")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hello")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("event:done")));
    }
}
