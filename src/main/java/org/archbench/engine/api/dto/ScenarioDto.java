package org.archbench.engine.api.dto;

import java.util.List;

public record ScenarioDto(
    String name,
    Workload workload,
    List<Node> nodes,
    List<Edge> edges
) {
    public record Workload(
        Integer rps,
        Integer p95TargetMs
    ) {}

    public record Node(
        String id,
        String type,
        Integer latencyMs,
        Double varianceFactor,
        Integer capacityRps,
        Double failureRate,
        Double costPerHour,
        DbConfig dbConfig
    ) {}

    public record DbConfig(
        String engine,
        List<DbTable> tables
    ) {}

    public record DbTable(
        String name,
        String sizeClass,
        List<String> indexes,
        List<DbColumn> columns
    ) {}

    public record DbColumn(
        String name,
        String type
    ) {}

    public record Edge(
        String from,
        String to
    ) {}
}
