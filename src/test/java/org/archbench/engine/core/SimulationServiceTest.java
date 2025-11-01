package org.archbench.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.archbench.engine.api.dto.ScenarioDto;
import org.junit.jupiter.api.Test;

public class SimulationServiceTest {

    private final SimulationService simulationService = new SimulationService();

    @Test
    public void urlShortenerScenarioProducesDeterministicMetrics() {
        List<ScenarioDto.Node> nodes = List.of(
            new ScenarioDto.Node("client", "client", null, null, null, null, null),
            new ScenarioDto.Node("api", "service", null, null, null, null, null),
            new ScenarioDto.Node("cache", "cache", null, null, null, null, null),
            new ScenarioDto.Node("db", "database", null, null, null, null, null)
        );
        List<ScenarioDto.Edge> edges = List.of(
            new ScenarioDto.Edge("client", "api"),
            new ScenarioDto.Edge("api", "cache"),
            new ScenarioDto.Edge("api", "db")
        );

        List<ScenarioDto.Node> normalizedNodes = simulationService.normalizeNodes(nodes);
        assertEquals(nodes.size(), normalizedNodes.size());
        assertTrue(normalizedNodes.stream().allMatch(node ->
            node.latencyMs() != null
                && node.varianceFactor() != null
                && node.capacityRps() != null
                && node.failureRate() != null
                && node.costPerHour() != null
        ));

        int latencyP50 = simulationService.calculateLatencyP50(normalizedNodes, edges);
        assertEquals(23, latencyP50);

        int latencyP95 = simulationService.calculateLatencyP95(latencyP50, normalizedNodes);
        assertEquals(33, latencyP95);
        assertTrue(latencyP95 >= latencyP50);

        int throughput = simulationService.calculateThroughput(normalizedNodes);
        assertEquals(2000, throughput);

        double failureRate = simulationService.calculateFailureRate(normalizedNodes);
        assertTrue(failureRate >= 0.0);
        assertTrue(failureRate < 1.0);
        assertEquals(0.0179, failureRate, 1e-4);

        double cost = simulationService.calculateCost(normalizedNodes);
        assertEquals(0.37, cost, 1e-6);
    }
}
