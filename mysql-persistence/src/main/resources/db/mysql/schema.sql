CREATE TABLE IF NOT EXISTS breeding_batches (
    batch_id VARCHAR(64) PRIMARY KEY,
    organization_name VARCHAR(128) NOT NULL,
    breed_name VARCHAR(128) NOT NULL,
    feeding_mode VARCHAR(64) NOT NULL,
    entry_date DATE NOT NULL,
    planned_market_date DATE NULL,
    responsible_open_id VARCHAR(128) NOT NULL DEFAULT '',
    responsible_name VARCHAR(128) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS weight_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(64) NOT NULL,
    measured_date DATE NOT NULL,
    age_days INT NOT NULL,
    average_weight_kg DECIMAL(10,4) NOT NULL,
    uniformity_percent DECIMAL(10,4) NOT NULL,
    stock_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_weight_batch_date (batch_id, measured_date),
    KEY idx_weight_batch_date (batch_id, measured_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS breeding_standards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    breed_name VARCHAR(128) NOT NULL,
    feeding_mode VARCHAR(64) NOT NULL,
    start_age_days INT NOT NULL,
    end_age_days INT NOT NULL,
    min_weight_kg DECIMAL(10,4) NOT NULL,
    max_weight_kg DECIMAL(10,4) NOT NULL,
    min_uniformity_percent DECIMAL(10,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_breeding_standard_age (breed_name, feeding_mode, start_age_days, end_age_days),
    KEY idx_breeding_standard_lookup (breed_name, feeding_mode, start_age_days, end_age_days)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS fcr_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(64) NOT NULL,
    record_date DATE NOT NULL,
    age_days INT NOT NULL,
    feed_consumed_kg DECIMAL(10,4) NOT NULL,
    weight_gain_kg DECIMAL(10,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fcr_batch_date (batch_id, record_date),
    KEY idx_fcr_batch_date (batch_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS fcr_standards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    breed_name VARCHAR(128) NOT NULL,
    feeding_mode VARCHAR(64) NOT NULL,
    start_age_days INT NOT NULL,
    end_age_days INT NOT NULL,
    max_fcr DECIMAL(10,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fcr_standard_age (breed_name, feeding_mode, start_age_days, end_age_days),
    KEY idx_fcr_standard_lookup (breed_name, feeding_mode, start_age_days, end_age_days)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS analysis_requests (
    request_id VARCHAR(128) PRIMARY KEY,
    source VARCHAR(64) NOT NULL,
    requester_open_id VARCHAR(128) NOT NULL,
    batch_id VARCHAR(64) NOT NULL,
    analysis_type VARCHAR(64) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    raw_question TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_analysis_request_batch (batch_id, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS analysis_results (
    request_id VARCHAR(128) PRIMARY KEY,
    risk_level VARCHAR(64) NOT NULL,
    summary TEXT NOT NULL,
    reasons_json JSON NOT NULL,
    suggestions_json JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS visualization_data_records (
    record_id VARCHAR(128) PRIMARY KEY,
    request_id VARCHAR(128) NOT NULL,
    chart_type VARCHAR(64) NOT NULL,
    dimension_name VARCHAR(128) NOT NULL,
    metric_name VARCHAR(128) NOT NULL,
    data_json JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_visualization_request (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS task_records (
    task_id VARCHAR(128) PRIMARY KEY,
    source_id VARCHAR(128) NOT NULL DEFAULT '',
    status VARCHAR(32) NOT NULL,
    result_json JSON NULL,
    result_type VARCHAR(255) NOT NULL DEFAULT '',
    error_message TEXT NOT NULL,
    created_at TIMESTAMP NULL,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_task_source (source_id),
    KEY idx_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
