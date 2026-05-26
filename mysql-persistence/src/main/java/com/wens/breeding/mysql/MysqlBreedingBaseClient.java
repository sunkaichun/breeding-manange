package com.wens.breeding.mysql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.wens.breeding.analysis.model.BreedingBatch;
import com.wens.breeding.analysis.model.BreedingStandard;
import com.wens.breeding.analysis.model.FcrRecord;
import com.wens.breeding.analysis.model.FcrStandard;
import com.wens.breeding.analysis.model.WeightRecord;
import com.wens.breeding.lark.base.BreedingBaseClient;
import com.wens.breeding.lark.base.FcrBaseClient;

import org.springframework.jdbc.core.JdbcTemplate;

public final class MysqlBreedingBaseClient implements BreedingBaseClient, FcrBaseClient {
    private final JdbcTemplate jdbcTemplate;

    public MysqlBreedingBaseClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    @Override
    public Optional<BreedingBatch> findBatchById(String batchId) {
        List<BreedingBatch> rows = jdbcTemplate.query(
                "SELECT batch_id, organization_name, breed_name, feeding_mode, entry_date, "
                        + "planned_market_date, responsible_open_id, responsible_name "
                        + "FROM breeding_batches WHERE batch_id = ?",
                MysqlBreedingBaseClient::mapBatch,
                requireText(batchId, "batchId"));
        return rows.stream().findFirst();
    }

    @Override
    public List<WeightRecord> listWeightRecords(String batchId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return jdbcTemplate.query(
                "SELECT batch_id, measured_date, age_days, average_weight_kg, uniformity_percent, stock_count "
                        + "FROM weight_records WHERE batch_id = ? AND measured_date BETWEEN ? AND ? "
                        + "ORDER BY measured_date ASC",
                MysqlBreedingBaseClient::mapWeightRecord,
                requireText(batchId, "batchId"),
                Date.valueOf(startDate),
                Date.valueOf(endDate));
    }

    @Override
    public Optional<BreedingStandard> findStandard(String breedName, String feedingMode, int ageDays) {
        if (ageDays < 0) {
            throw new IllegalArgumentException("ageDays must be non-negative");
        }
        List<BreedingStandard> rows = jdbcTemplate.query(
                "SELECT breed_name, feeding_mode, start_age_days, end_age_days, min_weight_kg, "
                        + "max_weight_kg, min_uniformity_percent FROM breeding_standards "
                        + "WHERE breed_name = ? AND feeding_mode = ? AND ? BETWEEN start_age_days AND end_age_days "
                        + "ORDER BY start_age_days DESC LIMIT 1",
                MysqlBreedingBaseClient::mapBreedingStandard,
                requireText(breedName, "breedName"),
                requireText(feedingMode, "feedingMode"),
                ageDays);
        return rows.stream().findFirst();
    }

    @Override
    public List<FcrRecord> listFcrRecords(String batchId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return jdbcTemplate.query(
                "SELECT batch_id, record_date, age_days, feed_consumed_kg, weight_gain_kg "
                        + "FROM fcr_records WHERE batch_id = ? AND record_date BETWEEN ? AND ? "
                        + "ORDER BY record_date ASC",
                MysqlBreedingBaseClient::mapFcrRecord,
                requireText(batchId, "batchId"),
                Date.valueOf(startDate),
                Date.valueOf(endDate));
    }

    @Override
    public Optional<FcrStandard> findFcrStandard(String breedName, String feedingMode, int ageDays) {
        if (ageDays < 0) {
            throw new IllegalArgumentException("ageDays must be non-negative");
        }
        List<FcrStandard> rows = jdbcTemplate.query(
                "SELECT breed_name, feeding_mode, start_age_days, end_age_days, max_fcr FROM fcr_standards "
                        + "WHERE breed_name = ? AND feeding_mode = ? AND ? BETWEEN start_age_days AND end_age_days "
                        + "ORDER BY start_age_days DESC LIMIT 1",
                MysqlBreedingBaseClient::mapFcrStandard,
                requireText(breedName, "breedName"),
                requireText(feedingMode, "feedingMode"),
                ageDays);
        return rows.stream().findFirst();
    }

    public void saveBatch(BreedingBatch batch) {
        BreedingBatch value = Objects.requireNonNull(batch, "batch");
        jdbcTemplate.update(
                "INSERT INTO breeding_batches "
                        + "(batch_id, organization_name, breed_name, feeding_mode, entry_date, planned_market_date, "
                        + "responsible_open_id, responsible_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE organization_name = VALUES(organization_name), "
                        + "breed_name = VALUES(breed_name), feeding_mode = VALUES(feeding_mode), "
                        + "entry_date = VALUES(entry_date), planned_market_date = VALUES(planned_market_date), "
                        + "responsible_open_id = VALUES(responsible_open_id), responsible_name = VALUES(responsible_name)",
                value.getBatchId(),
                value.getOrganizationName(),
                value.getBreedName(),
                value.getFeedingMode(),
                Date.valueOf(value.getEntryDate()),
                value.getPlannedMarketDate() == null ? null : Date.valueOf(value.getPlannedMarketDate()),
                value.getResponsibleOpenId(),
                value.getResponsibleName());
    }

    public void saveWeightRecord(WeightRecord record) {
        WeightRecord value = Objects.requireNonNull(record, "record");
        jdbcTemplate.update(
                "INSERT INTO weight_records "
                        + "(batch_id, measured_date, age_days, average_weight_kg, uniformity_percent, stock_count) "
                        + "VALUES (?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE age_days = VALUES(age_days), "
                        + "average_weight_kg = VALUES(average_weight_kg), uniformity_percent = VALUES(uniformity_percent), "
                        + "stock_count = VALUES(stock_count)",
                value.getBatchId(),
                Date.valueOf(value.getMeasuredDate()),
                value.getAgeDays(),
                value.getAverageWeightKg(),
                value.getUniformityPercent(),
                value.getStockCount());
    }

    public void saveBreedingStandard(BreedingStandard standard) {
        BreedingStandard value = Objects.requireNonNull(standard, "standard");
        jdbcTemplate.update(
                "INSERT INTO breeding_standards "
                        + "(breed_name, feeding_mode, start_age_days, end_age_days, min_weight_kg, max_weight_kg, "
                        + "min_uniformity_percent) VALUES (?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE min_weight_kg = VALUES(min_weight_kg), "
                        + "max_weight_kg = VALUES(max_weight_kg), min_uniformity_percent = VALUES(min_uniformity_percent)",
                value.getBreedName(),
                value.getFeedingMode(),
                value.getStartAgeDays(),
                value.getEndAgeDays(),
                value.getMinWeightKg(),
                value.getMaxWeightKg(),
                value.getMinUniformityPercent());
    }

    public void saveFcrRecord(FcrRecord record) {
        FcrRecord value = Objects.requireNonNull(record, "record");
        jdbcTemplate.update(
                "INSERT INTO fcr_records "
                        + "(batch_id, record_date, age_days, feed_consumed_kg, weight_gain_kg) "
                        + "VALUES (?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE age_days = VALUES(age_days), "
                        + "feed_consumed_kg = VALUES(feed_consumed_kg), weight_gain_kg = VALUES(weight_gain_kg)",
                value.getBatchId(),
                Date.valueOf(value.getRecordDate()),
                value.getAgeDays(),
                value.getFeedConsumedKg(),
                value.getWeightGainKg());
    }

    public void saveFcrStandard(FcrStandard standard) {
        FcrStandard value = Objects.requireNonNull(standard, "standard");
        jdbcTemplate.update(
                "INSERT INTO fcr_standards (breed_name, feeding_mode, start_age_days, end_age_days, max_fcr) "
                        + "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE max_fcr = VALUES(max_fcr)",
                value.getBreedName(),
                value.getFeedingMode(),
                value.getStartAgeDays(),
                value.getEndAgeDays(),
                value.getMaxFcr());
    }

    private static BreedingBatch mapBatch(ResultSet rs, int rowNum) throws SQLException {
        Date plannedMarketDate = rs.getDate("planned_market_date");
        return new BreedingBatch(
                rs.getString("batch_id"),
                rs.getString("organization_name"),
                rs.getString("breed_name"),
                rs.getString("feeding_mode"),
                rs.getDate("entry_date").toLocalDate(),
                plannedMarketDate == null ? null : plannedMarketDate.toLocalDate(),
                rs.getString("responsible_open_id"),
                rs.getString("responsible_name"));
    }

    private static WeightRecord mapWeightRecord(ResultSet rs, int rowNum) throws SQLException {
        return new WeightRecord(
                rs.getString("batch_id"),
                rs.getDate("measured_date").toLocalDate(),
                rs.getInt("age_days"),
                rs.getBigDecimal("average_weight_kg"),
                rs.getBigDecimal("uniformity_percent"),
                rs.getInt("stock_count"));
    }

    private static BreedingStandard mapBreedingStandard(ResultSet rs, int rowNum) throws SQLException {
        return new BreedingStandard(
                rs.getString("breed_name"),
                rs.getString("feeding_mode"),
                rs.getInt("start_age_days"),
                rs.getInt("end_age_days"),
                rs.getBigDecimal("min_weight_kg"),
                rs.getBigDecimal("max_weight_kg"),
                rs.getBigDecimal("min_uniformity_percent"));
    }

    private static FcrRecord mapFcrRecord(ResultSet rs, int rowNum) throws SQLException {
        return new FcrRecord(
                rs.getString("batch_id"),
                rs.getDate("record_date").toLocalDate(),
                rs.getInt("age_days"),
                rs.getBigDecimal("feed_consumed_kg"),
                rs.getBigDecimal("weight_gain_kg"));
    }

    private static FcrStandard mapFcrStandard(ResultSet rs, int rowNum) throws SQLException {
        return new FcrStandard(
                rs.getString("breed_name"),
                rs.getString("feeding_mode"),
                rs.getInt("start_age_days"),
                rs.getInt("end_age_days"),
                rs.getBigDecimal("max_fcr"));
    }

    private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
