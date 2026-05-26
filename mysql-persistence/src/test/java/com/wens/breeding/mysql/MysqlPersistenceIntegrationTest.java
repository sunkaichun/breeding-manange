package com.wens.breeding.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wens.breeding.analysis.model.AnalysisRequest;
import com.wens.breeding.analysis.model.AnalysisResult;
import com.wens.breeding.analysis.model.AnalysisType;
import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.RequestSource;
import com.wens.breeding.analysis.model.RiskLevel;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.lark.base.VisualizationDataRecord;
import com.wens.breeding.task.TaskRecord;
import com.wens.breeding.task.TaskStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class MysqlPersistenceIntegrationTest {
    private static final String URL = env("MYSQL_URL",
            "jdbc:mysql://127.0.0.1:3306/app_dev?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
    private static final String USER = env("MYSQL_USER", "dev");
    private static final String PASSWORD = env("MYSQL_PASSWORD", "devpass");

    private JdbcTemplate jdbcTemplate;
    private MysqlBreedingBaseClient breedingClient;
    private MysqlAiBaseWriteClient writeClient;
    private MysqlTaskStore<AnalysisResult> taskStore;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(canConnect(), "Local MySQL is not available; skipping MySQL integration test.");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USER, PASSWORD);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        new MysqlSchemaInitializer(dataSource).initialize();
        this.breedingClient = new MysqlBreedingBaseClient(jdbcTemplate);
        this.writeClient = new MysqlAiBaseWriteClient(jdbcTemplate, new ObjectMapper());
        this.taskStore = new MysqlTaskStore<>(jdbcTemplate, new ObjectMapper(), AnalysisResult.class);
        clean();
    }

    @AfterEach
    void tearDown() {
        if (jdbcTemplate != null) {
            clean();
        }
    }

    @Test
    void persistsAndReadsBreedingAnalysisData() {
        BreedingBatch batch = new BreedingBatch(
                "IT-BATCH-001",
                "Integration Org",
                "Integration Breed",
                "Mixed",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-03-01"),
                "ou_it",
                "Integration Owner");
        WeightRecord weightRecord = new WeightRecord(
                "IT-BATCH-001",
                LocalDate.parse("2026-02-01"),
                31,
                new BigDecimal("1.2300"),
                new BigDecimal("82.5000"),
                1200);
        BreedingStandard standard = new BreedingStandard(
                "Integration Breed",
                "Mixed",
                30,
                40,
                new BigDecimal("1.1000"),
                new BigDecimal("1.4000"),
                new BigDecimal("80.0000"));
        FcrRecord fcrRecord = new FcrRecord(
                "IT-BATCH-001",
                LocalDate.parse("2026-02-01"),
                31,
                new BigDecimal("150.0000"),
                new BigDecimal("80.0000"));
        FcrStandard fcrStandard = new FcrStandard(
                "Integration Breed",
                "Mixed",
                30,
                40,
                new BigDecimal("1.9000"));

        breedingClient.saveBatch(batch);
        breedingClient.saveWeightRecord(weightRecord);
        breedingClient.saveBreedingStandard(standard);
        breedingClient.saveFcrRecord(fcrRecord);
        breedingClient.saveFcrStandard(fcrStandard);

        assertTrue(breedingClient.findBatchById("IT-BATCH-001").isPresent());
        assertEquals(1, breedingClient.listWeightRecords(
                "IT-BATCH-001",
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-01")).size());
        assertTrue(breedingClient.findStandard("Integration Breed", "Mixed", 31).isPresent());
        assertEquals(1, breedingClient.listFcrRecords(
                "IT-BATCH-001",
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-01")).size());
        assertTrue(breedingClient.findFcrStandard("Integration Breed", "Mixed", 31).isPresent());

        AnalysisRequest request = new AnalysisRequest(
                "IT-REQ-001",
                RequestSource.MANUAL_TEST,
                "ou_it",
                "IT-BATCH-001",
                AnalysisType.WEIGHT_TREND,
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-01"),
                "integration test");
        AnalysisResult result = new AnalysisResult(
                "IT-REQ-001",
                RiskLevel.HIGH,
                "Integration summary",
                List.of("reason"),
                List.of("suggestion"));
        VisualizationDataRecord visualization = new VisualizationDataRecord(
                "IT-REQ-001",
                "line",
                "date",
                "weight",
                "{\"points\":[]}");

        assertEquals("IT-REQ-001", writeClient.createAnalysisRequest(request));
        assertEquals("IT-REQ-001", writeClient.saveAnalysisResult(result));
        String visualizationId = writeClient.saveVisualizationData(visualization);

        assertTrue(writeClient.findAnalysisRequest("IT-REQ-001").isPresent());
        assertEquals(RiskLevel.HIGH, writeClient.findAnalysisResult("IT-REQ-001").get().getRiskLevel());
        assertTrue(writeClient.findVisualizationDataRecord(visualizationId).isPresent());
    }

    @Test
    void persistsTaskLifecycle() {
        AnalysisResult result = new AnalysisResult(
                "IT-REQ-TASK",
                RiskLevel.LOW,
                "Task completed",
                List.of("stable"),
                List.of("continue"));

        TaskRecord<AnalysisResult> accepted = taskStore.createAccepted(
                "IT-TASK-001",
                "IT-SOURCE-001",
                Instant.parse("2026-05-26T00:00:00Z"));
        TaskRecord<AnalysisResult> running = taskStore.markRunning(
                "IT-TASK-001",
                Instant.parse("2026-05-26T00:01:00Z"));
        TaskRecord<AnalysisResult> completed = taskStore.markCompleted(
                "IT-TASK-001",
                result,
                Instant.parse("2026-05-26T00:02:00Z"));

        assertEquals(TaskStatus.ACCEPTED, accepted.getStatus());
        assertEquals(TaskStatus.RUNNING, running.getStatus());
        assertEquals(TaskStatus.COMPLETED, completed.getStatus());
        TaskRecord<AnalysisResult> reloaded = taskStore.findByTaskId("IT-TASK-001").get();
        assertEquals("IT-REQ-TASK", reloaded.getResult().getRequestId());
        assertEquals(RiskLevel.LOW, reloaded.getResult().getRiskLevel());
    }

    private void clean() {
        jdbcTemplate.update("DELETE FROM task_records WHERE task_id LIKE 'IT-%'");
        jdbcTemplate.update("DELETE FROM visualization_data_records WHERE request_id LIKE 'IT-%'");
        jdbcTemplate.update("DELETE FROM analysis_results WHERE request_id LIKE 'IT-%'");
        jdbcTemplate.update("DELETE FROM analysis_requests WHERE request_id LIKE 'IT-%'");
        jdbcTemplate.update("DELETE FROM fcr_records WHERE batch_id LIKE 'IT-%'");
        jdbcTemplate.update("DELETE FROM weight_records WHERE batch_id LIKE 'IT-%'");
        jdbcTemplate.update("DELETE FROM fcr_standards WHERE breed_name = 'Integration Breed'");
        jdbcTemplate.update("DELETE FROM breeding_standards WHERE breed_name = 'Integration Breed'");
        jdbcTemplate.update("DELETE FROM breeding_batches WHERE batch_id LIKE 'IT-%'");
    }

    private static boolean canConnect() {
        try (Connection ignored = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
