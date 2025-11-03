package org.archbench.engine.api;

import java.util.List;

import org.archbench.engine.api.dto.ScenarioDto;
import org.archbench.engine.api.dto.SimulationResultDto;
import org.archbench.engine.core.ScenarioValidator;
import org.archbench.engine.core.SimulationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulateController {

    private final SimulationService simulationService;
    private final ScenarioValidator scenarioValidator;

    public SimulateController(SimulationService simulationService, ScenarioValidator scenarioValidator) {
        this.simulationService = simulationService;
        this.scenarioValidator = scenarioValidator;
    }

    @PostMapping("/simulate")
    public SimulationResultDto simulate(@RequestBody ScenarioDto scenario) {
        scenarioValidator.validate(scenario);
        List<ScenarioDto.Node> normalizedNodes = simulationService.normalizeNodes(scenario.nodes());
        int latencyP50 = simulationService.calculateLatencyP50(normalizedNodes, scenario.edges());
        int latencyP95 = simulationService.calculateLatencyP95(latencyP50, normalizedNodes);
        int throughput = simulationService.calculateThroughput(normalizedNodes);
        double failureRate = simulationService.calculateFailureRate(normalizedNodes);
        double costPerHour = simulationService.calculateCost(normalizedNodes);
        String status = failureRate > 0.05 ? "degraded" : "ok";
        int score = Math.max(0, Math.min(100, 100 - (int) Math.round(failureRate * 100)));
        List<String> hints = failureRate > 0.05
            ? List.of("Failure rate exceeds 5%; consider adding redundancy.")
            : List.of();
        return new SimulationResultDto(
            latencyP50,
            latencyP95,
            throughput,
            costPerHour,
            status,
            score,
            hints
        );
    }

}
