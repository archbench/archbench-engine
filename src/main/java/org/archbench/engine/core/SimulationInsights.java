package org.archbench.engine.core;

import java.util.ArrayList;
import java.util.List;

import org.archbench.engine.api.dto.ScenarioDto;
import org.springframework.stereotype.Component;

@Component
public class SimulationInsights {

    private static final double FAILURE_DEGRADED_THRESHOLD = 0.05;

    public int calculateScore(ScenarioDto scenario, int latencyMsP95, int throughputRps, double failureRate) {
        Targets targets = Targets.fromScenario(scenario);
        double throughputScore = targets.rpsTarget() == null || targets.rpsTarget() <= 0
            ? 1.0
            : clamp((double) throughputRps / targets.rpsTarget());
        double latencyScore = targets.p95Target() == null || targets.p95Target() <= 0 || latencyMsP95 <= 0
            ? 1.0
            : clamp((double) targets.p95Target() / latencyMsP95);
        double reliabilityScore = clamp(1.0 - failureRate * 5.0);
        double weighted = throughputScore * 0.45 + latencyScore * 0.45 + reliabilityScore * 0.10;
        return (int) Math.round(weighted * 100.0);
    }

    public List<String> generateHints(ScenarioDto scenario, int latencyMsP95, int throughputRps, double failureRate) {
        Targets targets = Targets.fromScenario(scenario);
        List<String> hints = new ArrayList<>();

        if (targets.rpsTarget() != null && targets.rpsTarget() > throughputRps) {
            hints.add(String.format(
                "Throughput is below the workload target (%d rps < %d rps); increase capacity on the slowest nodes or introduce caching/sharding.",
                throughputRps,
                targets.rpsTarget()
            ));
        }

        if (targets.p95Target() != null && latencyMsP95 > targets.p95Target()) {
            hints.add(String.format(
                "p95 latency exceeds the workload target (%d ms > %d ms); reduce per-node latency or insert faster caching layers.",
                latencyMsP95,
                targets.p95Target()
            ));
        }

        if (failureRate > FAILURE_DEGRADED_THRESHOLD) {
            hints.add(String.format(
                "Failure rate is above %.0f%% (%.2f%% observed); add redundancy or backpressure to stabilize the path.",
                FAILURE_DEGRADED_THRESHOLD * 100,
                failureRate * 100
            ));
        }

        return hints;
    }

    public String deriveStatus(ScenarioDto scenario, int latencyMsP95, int throughputRps, double failureRate) {
        Targets targets = Targets.fromScenario(scenario);
        boolean throughputMiss = targets.rpsTarget() != null && throughputRps < targets.rpsTarget();
        boolean latencyMiss = targets.p95Target() != null && latencyMsP95 > targets.p95Target();
        boolean failureHigh = failureRate > FAILURE_DEGRADED_THRESHOLD;
        return throughputMiss || latencyMiss || failureHigh ? "degraded" : "ok";
    }

    private static double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }

    private record Targets(Integer rpsTarget, Integer p95Target) {
        static Targets fromScenario(ScenarioDto scenario) {
            if (scenario == null || scenario.workload() == null) {
                return new Targets(null, null);
            }
            ScenarioDto.Workload workload = scenario.workload();
            return new Targets(workload.rps(), workload.p95TargetMs());
        }
    }
}
