package org.archbench.engine.api;

import java.util.List;

import org.archbench.engine.api.dto.ScenarioDto;
import org.archbench.engine.api.dto.SimulationResultDto;
import org.archbench.engine.core.SimulationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulateController {

    private final SimulationService simulationService;

    public SimulateController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/simulate")
    public SimulationResultDto simulate(@RequestBody ScenarioDto scenario) {
        List<ScenarioDto.Node> normalizedNodes = simulationService.normalizeNodes(scenario.nodes());
        int latencyP50 = simulationService.calculateLatencyP50(normalizedNodes, scenario.edges());
        int latencyP95 = simulationService.calculateLatencyP95(latencyP50, normalizedNodes);
        int throughput = simulationService.calculateThroughput(normalizedNodes);
        double costPerHour = simulationService.calculateCost(normalizedNodes);
        return new SimulationResultDto(
            latencyP50,
            latencyP95,
            throughput,
            costPerHour,
            "ok"
        );
    }

}
