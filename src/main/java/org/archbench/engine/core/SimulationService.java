package org.archbench.engine.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.archbench.engine.api.dto.ScenarioDto;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    private static final Map<String, NodeDefaults> DEFAULTS = Map.ofEntries(
        Map.entry("client", new NodeDefaults(2, 1.1, Integer.MAX_VALUE, 0.001, 0.00)),
        Map.entry("service", new NodeDefaults(8, 1.5, 3000, 0.005, 0.05)),
        Map.entry("cache", new NodeDefaults(1, 1.1, 50000, 0.002, 0.02)),
        Map.entry("database", new NodeDefaults(12, 2.0, 2000, 0.010, 0.30)),
        Map.entry("queue", new NodeDefaults(2, 1.2, 10000, 0.003, 0.01)),
        Map.entry("gateway", new NodeDefaults(6, 1.4, 4000, 0.004, 0.06)),
        Map.entry("cdn", new NodeDefaults(3, 1.1, 60000, 0.001, 0.05)),
        Map.entry("objectstore", new NodeDefaults(15, 2.2, 1500, 0.008, 0.25)),
        Map.entry("search", new NodeDefaults(10, 1.8, 2500, 0.006, 0.20)),
        Map.entry("stream", new NodeDefaults(6, 1.4, 5000, 0.005, 0.10)),
        Map.entry("lb", new NodeDefaults(3, 1.1, 50000, 0.001, 0.03)),
        Map.entry("worker", new NodeDefaults(7, 1.6, 3500, 0.004, 0.07))
    );

    public List<ScenarioDto.Node> normalizeNodes(List<ScenarioDto.Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }
        return nodes.stream()
            .filter(Objects::nonNull)
            .map(this::applyDefaults)
            .toList();
    }

    public int calculateLatencyP50(List<ScenarioDto.Node> nodes, List<ScenarioDto.Edge> edges) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }
        return nodes.stream()
            .mapToInt(ScenarioDto.Node::latencyMs)
            .sum();
    }

    public int calculateLatencyP95(int latencyP50, List<ScenarioDto.Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return latencyP50;
        }
        double averageVariance = nodes.stream()
            .mapToDouble(ScenarioDto.Node::varianceFactor)
            .average()
            .orElse(1.0);
        return (int) Math.round(latencyP50 * averageVariance);
    }

    public int calculateThroughput(List<ScenarioDto.Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }
        return nodes.stream()
            .mapToInt(ScenarioDto.Node::capacityRps)
            .min()
            .orElse(0);
    }

    public double calculateFailureRate(List<ScenarioDto.Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0.0;
        }
        double survivalProduct = nodes.stream()
            .mapToDouble(node -> 1.0 - node.failureRate())
            .reduce(1.0, (left, right) -> left * right);
        return 1.0 - survivalProduct;
    }

    public double calculateCost(List<ScenarioDto.Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0.0;
        }
        return nodes.stream()
            .mapToDouble(ScenarioDto.Node::costPerHour)
            .sum();
    }

    private ScenarioDto.Node applyDefaults(ScenarioDto.Node node) {
        NodeDefaults defaults = DEFAULTS.get(node.type());
        if (defaults == null) {
            throw new IllegalArgumentException("No defaults configured for node type: " + node.type());
        }
        Integer latency = node.latencyMs() != null ? node.latencyMs() : defaults.latencyMs();
        Double variance = node.varianceFactor() != null ? node.varianceFactor() : defaults.varianceFactor();
        Integer capacity = node.capacityRps() != null ? node.capacityRps() : defaults.capacityRps();
        Double failureRate = node.failureRate() != null ? node.failureRate() : defaults.failureRate();
        Double cost = node.costPerHour() != null ? node.costPerHour() : defaults.costPerHour();

        return new ScenarioDto.Node(
            node.id(),
            node.type(),
            latency,
            variance,
            capacity,
            failureRate,
            cost,
            node.dbConfig()
        );
    }
}
