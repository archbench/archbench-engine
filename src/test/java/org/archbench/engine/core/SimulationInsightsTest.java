package org.archbench.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.archbench.engine.api.dto.ScenarioDto;
import org.junit.jupiter.api.Test;

class SimulationInsightsTest {

    private final SimulationInsights insights = new SimulationInsights();

    @Test
    void scoreRewardsMeetingTargets() {
        ScenarioDto scenario = scenarioWithTargets(1000, 120);

        int score = insights.calculateScore(scenario, 110, 1200, 0.01);

        assertEquals(100, score);
    }

    @Test
    void scorePenalizesLatencyThroughputAndReliabilityMisses() {
        ScenarioDto scenario = scenarioWithTargets(2000, 90);

        int score = insights.calculateScore(scenario, 180, 800, 0.08);

        assertTrue(score < 60, "Expected a low score when all targets fail, got " + score);
    }

    @Test
    void hintsReflectDetectedIssues() {
        ScenarioDto scenario = scenarioWithTargets(1500, 100);

        List<String> hints = insights.generateHints(scenario, 140, 900, 0.07);

        assertEquals(3, hints.size());
        assertTrue(hints.get(0).contains("Throughput"));
        assertTrue(hints.get(1).contains("p95 latency"));
        assertTrue(hints.get(2).contains("Failure rate"));
    }

    @Test
    void statusChangesWhenTargetsAreMissed() {
        ScenarioDto scenario = scenarioWithTargets(1200, 120);

        String degraded = insights.deriveStatus(scenario, 200, 900, 0.02);
        String ok = insights.deriveStatus(scenario, 100, 1500, 0.01);

        assertEquals("degraded", degraded);
        assertEquals("ok", ok);
    }

    private ScenarioDto scenarioWithTargets(Integer rps, Integer p95) {
        return new ScenarioDto(
            "demo",
            new ScenarioDto.Workload(rps, p95),
            List.of(),
            List.of()
        );
    }
}
