package org.archbench.engine.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.archbench.engine.api.dto.ScenarioDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ScenarioValidatorTest {

    private final ScenarioValidator validator = new ScenarioValidator();

    @Test
    void duplicateTableNamesProduce400() {
        ScenarioDto scenario = baseScenario(
            new ScenarioDto.DbConfig(
                "postgres",
                List.of(
                    new ScenarioDto.DbTable("users", null, null, null),
                    new ScenarioDto.DbTable("users", null, null, null)
                )
            )
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> validator.validate(scenario));
        assertTrue(ex.getReason().contains("duplicate table name"));
    }

    @Test
    void duplicateColumnNamesProduce400() {
        ScenarioDto scenario = baseScenario(
            new ScenarioDto.DbConfig(
                "postgres",
                List.of(
                    new ScenarioDto.DbTable(
                        "users",
                        null,
                        null,
                        List.of(
                            new ScenarioDto.DbColumn("id", "string"),
                            new ScenarioDto.DbColumn("id", "string")
                        )
                    )
                )
            )
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> validator.validate(scenario));
        assertTrue(ex.getReason().contains("duplicate column name"));
    }

    @Test
    void unsupportedEngineProduces400() {
        ScenarioDto scenario = baseScenario(
            new ScenarioDto.DbConfig(
                "oracle",
                List.of(
                    new ScenarioDto.DbTable("users", null, null, null)
                )
            )
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> validator.validate(scenario));
        assertTrue(ex.getReason().contains("unsupported database engine"));
    }

    private ScenarioDto baseScenario(ScenarioDto.DbConfig dbConfig) {
        ScenarioDto.Node dbNode = new ScenarioDto.Node(
            "db",
            "database",
            null,
            null,
            null,
            null,
            null,
            dbConfig
        );
        return new ScenarioDto(
            "example",
            null,
            List.of(dbNode),
            List.of()
        );
    }
}
