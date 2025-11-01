package org.archbench.engine.api;

import org.archbench.engine.api.dto.ScenarioDto;
import org.archbench.engine.api.dto.SimulationResultDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulateController {

    @PostMapping("/simulate")
    public SimulationResultDto simulate(@RequestBody ScenarioDto scenario){
        // TODO: deterministic simulation engine later

        System.out.println("Scenario received: " + scenario.name());

        return new SimulationResultDto(
                50,   // p50 ms
                120,  // p95 ms
                2000, // req/s
                0.12, // cost/hour $
                "mock-ok"
        );
    }
    
}
