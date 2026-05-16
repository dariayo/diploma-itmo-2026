CREATE TABLE IF NOT EXISTS dbo.pilot_directory (
    tab_no INTEGER PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    rank_name VARCHAR(64),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS dbo.pilot_aircraft_directory (
    aircraft_type VARCHAR(32) PRIMARY KEY,
    aircraft_family VARCHAR(64),
    max_range_nm INTEGER
);

CREATE TABLE IF NOT EXISTS dbo.pilot_route_directory (
    route_code VARCHAR(32) PRIMARY KEY,
    departure_airport VARCHAR(8),
    arrival_airport VARCHAR(8),
    distance_nm INTEGER
);

CREATE TABLE IF NOT EXISTS dbo.pilot_weather_snapshots (
    id BIGSERIAL PRIMARY KEY,
    source_flight_id BIGINT NOT NULL,
    icao_code VARCHAR(8) NOT NULL,
    visibility_m INTEGER,
    wind_direction VARCHAR(8),
    wind_speed INTEGER,
    weather_phenomena VARCHAR(128),
    raw_report TEXT,
    observed_at TIMESTAMP,
    CONSTRAINT fk_pilot_weather_source
        FOREIGN KEY (source_flight_id)
        REFERENCES dbo.pilot_flight_source (idmrshr)
);

INSERT INTO dbo.pilot_directory (tab_no, full_name, active)
SELECT DISTINCT tab_no, COALESCE(captain, CONCAT('Пилот ', tab_no)), TRUE
FROM dbo.pilot_flight_metrics
WHERE tab_no IS NOT NULL
ON CONFLICT (tab_no) DO NOTHING;

INSERT INTO dbo.pilot_aircraft_directory (aircraft_type, aircraft_family)
SELECT DISTINCT ts, ts
FROM dbo.pilot_flight_metrics
WHERE ts IS NOT NULL
ON CONFLICT (aircraft_type) DO NOTHING;

INSERT INTO dbo.pilot_route_directory (route_code, distance_nm)
SELECT route, MAX(distance)
FROM dbo.pilot_flight_metrics
WHERE route IS NOT NULL
GROUP BY route
ON CONFLICT (route_code) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_pilot_source_tab_no
    ON dbo.pilot_flight_source (tab_no);

CREATE INDEX IF NOT EXISTS idx_pilot_source_route
    ON dbo.pilot_flight_source (route);

CREATE INDEX IF NOT EXISTS idx_pilot_source_aircraft
    ON dbo.pilot_flight_source (ts);

CREATE INDEX IF NOT EXISTS idx_pilot_metrics_tab_no
    ON dbo.pilot_flight_metrics (tab_no);

CREATE INDEX IF NOT EXISTS idx_pilot_metrics_route
    ON dbo.pilot_flight_metrics (route);

CREATE INDEX IF NOT EXISTS idx_pilot_metrics_aircraft
    ON dbo.pilot_flight_metrics (ts);

CREATE INDEX IF NOT EXISTS idx_pilot_weather_source_flight
    ON dbo.pilot_weather_snapshots (source_flight_id);
