package org.archbench.engine.api.dto;

import java.util.List;

public record SimulationResultDto( 
        int latencyMsP50,
        int latencyMsP95,
        int throughputRps,
        double costPerHour,
        String status,
        Integer score,
        List<String> hints) {
}
