package org.archbench.engine.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SimulateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void simulateWithValidScenarioReturnsMetrics() throws Exception {
        String scenarioJson = """
            {
              "name": "api-scenario",
              "nodes": [
                { "id": "client", "type": "client" },
                { "id": "api", "type": "service" }
              ],
              "edges": [
                { "from": "client", "to": "api" }
              ]
            }
            """;

        mockMvc.perform(post("/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(scenarioJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.latencyMsP50").isNumber())
            .andExpect(jsonPath("$.latencyMsP95").isNumber())
            .andExpect(jsonPath("$.throughputRps").isNumber())
            .andExpect(jsonPath("$.costPerHour").isNumber())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.score").isNumber())
            .andExpect(jsonPath("$.hints").isArray());
    }

    @Test
    void simulateWithInvalidScenarioReturnsProblemDetail() throws Exception {
        String invalidScenarioJson = """
            {
              "name": "invalid",
              "nodes": [
                { "id": "client", "type": "client" }
              ],
              "edges": [
                { "from": "client", "to": "ghost" }
              ]
            }
            """;

        mockMvc.perform(post("/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidScenarioJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Invalid scenario"))
            .andExpect(jsonPath("$.detail").value("Edge 'to' not found: ghost"));
    }
}
