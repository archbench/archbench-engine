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
            new ScenarioDto.Node("client", "client", null, null, null, null, null, null),
            new ScenarioDto.Node("api", "service", null, null, null, null, null, null),
            new ScenarioDto.Node("cache", "cache", null, null, null, null, null, null),
            new ScenarioDto.Node("db", "database", null, null, null, null, null, null)
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
                && node.dbConfig() == null
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

    @Test
    void databaseAdjustmentFactorsAffectLatencyAndCapacity() {
        ScenarioDto.DbConfig dbConfig = new ScenarioDto.DbConfig(
            "mongo",
            List.of(
                new ScenarioDto.DbTable(
                    "urls",
                    "L",
                    List.of("short_code"),
                    List.of(new ScenarioDto.DbColumn("short_code", "string"))
                )
            )
        );

        List<ScenarioDto.Node> baselineNodes = simulationService.normalizeNodes(List.of(
            new ScenarioDto.Node("api", "service", null, null, null, null, null, null),
            new ScenarioDto.Node("db", "database", 20, null, 1800, null, null, null)
        ));

        ScenarioDto.Node baselineDb = baselineNodes.stream()
            .filter(node -> "db".equals(node.id()))
            .findFirst()
            .orElseThrow();

        List<ScenarioDto.Node> adjustedNodes = simulationService.normalizeNodes(List.of(
            new ScenarioDto.Node("api", "service", null, null, null, null, null, null),
            new ScenarioDto.Node("db", "database", 20, null, 1800, null, null, dbConfig)
        ));

        ScenarioDto.Node adjustedDb = adjustedNodes.stream()
            .filter(node -> "db".equals(node.id()))
            .findFirst()
            .orElseThrow();

        double expectedLatencyMultiplier = 1.2 /* size L */ * 0.9 /* index */ * 0.95 /* mongo */;
        double expectedCapacityMultiplier = 0.8 /* size L */ * 1.05 /* mongo */;
        int expectedLatency = (int) Math.round(20 * expectedLatencyMultiplier);
        int expectedCapacity = (int) Math.round(1800 * expectedCapacityMultiplier);

        assertEquals(expectedLatency, adjustedDb.latencyMs());
        assertEquals(expectedCapacity, adjustedDb.capacityRps());
        assertTrue(adjustedDb.latencyMs() > baselineDb.latencyMs());
        assertTrue(adjustedDb.capacityRps() < baselineDb.capacityRps());

        int adjustedLatencyP50 = simulationService.calculateLatencyP50(adjustedNodes, List.of());
        assertEquals(adjustedNodes.stream().mapToInt(ScenarioDto.Node::latencyMs).sum(), adjustedLatencyP50);

        int adjustedThroughput = simulationService.calculateThroughput(adjustedNodes);
        assertEquals(expectedCapacity, adjustedThroughput);
    }
}
