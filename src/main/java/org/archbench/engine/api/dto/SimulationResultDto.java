package org.archbench.engine.api.dto;

public record SimulationResultDto( 
        int latencyMsP50,
        int latencyMsP95,
        int throughputRps,
        double costPerHour,
        String status) {
    
}
