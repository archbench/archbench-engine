package org.archbench.engine.api.dto;

public record SimulationResultDto( 
        int latencyMsP50,
        int latencyMsP95,
        int throughputRps,
        double failureRate,
        double costPerHour,
        String status) {
    
}
