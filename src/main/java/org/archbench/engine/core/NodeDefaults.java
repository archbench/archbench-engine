package org.archbench.engine.core;

public record NodeDefaults(
    int latencyMs,
    double varianceFactor,
    int capacityRps,
    double failureRate,
    double costPerHour
) {}
