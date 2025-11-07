package org.archbench.engine.api;

import java.util.List;

import org.archbench.engine.api.dto.ScenarioDto;
import org.archbench.engine.api.dto.SimulationResultDto;
import org.archbench.engine.core.ScenarioValidator;
import org.archbench.engine.core.SimulationInsights;
import org.archbench.engine.core.SimulationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulateController {

    private final SimulationService simulationService;
    private final ScenarioValidator scenarioValidator;
    private final SimulationInsights simulationInsights;

    public SimulateController(
        SimulationService simulationService,
        ScenarioValidator scenarioValidator,
        SimulationInsights simulationInsights
    ) {
        this.simulationService = simulationService;
        this.scenarioValidator = scenarioValidator;
        this.simulationInsights = simulationInsights;
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
        String status = simulationInsights.deriveStatus(scenario, latencyP95, throughput, failureRate);
        int score = simulationInsights.calculateScore(scenario, latencyP95, throughput, failureRate);
        List<String> hints = simulationInsights.generateHints(scenario, latencyP95, throughput, failureRate);
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
