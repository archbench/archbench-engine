package org.archbench.engine.core;

import java.util.HashSet;
import java.util.Set;

import org.archbench.engine.api.dto.ScenarioDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ScenarioValidator {
    
    public void validate(ScenarioDto scenario){
        if (scenario == null) bad("Request body is null");
        if (scenario.name() == null || scenario.name().isBlank()) bad("Missing name");
        if (scenario.nodes() == null || scenario.nodes().isEmpty()) bad("Missing nodes");
        if (scenario.edges() == null) bad("Missing edges");

        Set<String> nodeIds = new HashSet<>();
        for (var n : scenario.nodes()) {
            if (n == null) bad("Node entry is null");
            if (n.id() == null || n.id().isBlank()) bad("Node id is missing/blank");
            if (!nodeIds.add(n.id())) bad("Duplicate node id: " + n.id());
            if (n.type() == null || n.type().isBlank()) bad("Node '" + n.id() + "' has missing 'type'");
        }

        for (var e : scenario.edges()) {
            if (e == null) bad("Edge entry is null");
            if (e.from() == null || e.from().isBlank()) bad("Edge missing 'from'");
            if (e.to() == null || e.to().isBlank()) bad("Edge missing 'to'");
            if (e.from().equals(e.to())) bad("Self-loop edge not allowed: " + e.from());
            if (!nodeIds.contains(e.from())) bad("Edge 'from' not found: " + e.from());
            if (!nodeIds.contains(e.to())) bad("Edge 'to' not found: " + e.to());
        }

    }

    public static void bad(String detail){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, detail);
    }

}
