package org.archbench.engine.api.dto;

import java.util.List;

public record ScenarioDto(
    String name,
    List<Node> nodes,
    List<Edge> edges
) {
    public record Node(String id, String type) {}
    public record Edge(String from, String to) {}
}
