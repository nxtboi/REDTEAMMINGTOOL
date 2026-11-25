package com.redteam.tool.dao;

import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.engine.EncryptedSandboxEnclave;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL-backed JDBC Data Access Object (DAO) for RED TEAMING TOOL scan history.
 * Utilizes standard JDBC Connection, PreparedStatement, and ResultSet.
 */
public final class ScanHistoryDao {

    private static final String DEFAULT_JDBC_URL = System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:mysql://localhost:3306/redteam_db?useSSL=false&serverTimezone=UTC");
    private static final String DB_USER = System.getenv().getOrDefault("JDBC_DB_USER", "root");
    private static final String DB_PASS = System.getenv().getOrDefault("JDBC_DB_PASS", "");

    private ScanHistoryDao() {}

    public static void initializeDatabaseTable() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS scan_reports (
                    report_id VARCHAR(64) PRIMARY KEY,
                    timestamp_iso VARCHAR(64),
                    scan_mode VARCHAR(64),
                    target_name VARCHAR(255),
                    detected_language VARCHAR(32),
                    risk_score INT,
                    grade VARCHAR(8),
                    source_code TEXT,
                    enclave_id VARCHAR(64),
                    payload_sha256 VARCHAR(64)
                )
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (Exception ex) {
            // Fallback or log if MySQL is not running locally during development
        }
    }

    public static Connection getConnection() throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DEFAULT_JDBC_URL, DB_USER, DB_PASS);
        } catch (Exception ex) {
            // Fallback to embedded SQLite or H2 / in-memory if MySQL driver is absent
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite::memory:");
        }
    }

    public static void saveScanReport(ScanReportRecord report) {
        String sql = """
                INSERT INTO scan_reports (report_id, timestamp_iso, scan_mode, target_name, detected_language, risk_score, grade, source_code, enclave_id, payload_sha256)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE risk_score = VALUES(risk_score), grade = VALUES(grade)
                """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, report.reportId());
            pstmt.setString(2, report.timestampIso());
            pstmt.setString(3, report.scanMode());
            pstmt.setString(4, report.targetName());
            pstmt.setString(5, report.detectedLanguage());
            pstmt.setInt(6, report.overallRiskScore());
            pstmt.setString(7, report.securityPostureGrade());
            pstmt.setString(8, report.sourceCode());
            pstmt.setString(9, report.envelope().sandboxEnclaveId());
            pstmt.setString(10, report.envelope().payloadSha256());
            pstmt.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public static List<ScanReportRecord> loadAllReports(List<ScanReportRecord> fallbackHistory) {
        List<ScanReportRecord> list = new ArrayList<>();
        String sql = "SELECT report_id, timestamp_iso, scan_mode, target_name, detected_language, risk_score, grade, source_code, enclave_id, payload_sha256 FROM scan_reports ORDER BY timestamp_iso DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String reportId = rs.getString("report_id");
                String timestampIso = rs.getString("timestamp_iso");
                String scanMode = rs.getString("scan_mode");
                String targetName = rs.getString("target_name");
                String lang = rs.getString("detected_language");
                int riskScore = rs.getInt("risk_score");
                String grade = rs.getString("grade");
                String sourceCode = rs.getString("source_code");
                String enclaveId = rs.getString("enclave_id");
                String sha256 = rs.getString("payload_sha256");

                EncryptedSandboxEnclave.Envelope env = new EncryptedSandboxEnclave.Envelope(
                        "AES-256-GCM", enclaveId, "KMS-EPHEMERAL-DEMO", sha256, "001122334455"
                );
                list.add(new ScanReportRecord(
                        reportId, timestampIso, scanMode, targetName, lang, sourceCode,
                        riskScore, grade, env, List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
                ));
            }
        } catch (Exception ignored) {
        }

        return list.isEmpty() ? fallbackHistory : list;
    }
}
