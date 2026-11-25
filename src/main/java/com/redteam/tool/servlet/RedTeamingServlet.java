package com.redteam.tool.servlet;

import com.redteam.tool.engine.BinaryPdfReportBuilder;
import com.redteam.tool.engine.EncryptedSandboxEnclave;
import com.redteam.tool.engine.KaliSuiteScannerEngine;
import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.ui.ArtifactInspectorPageView;
import com.redteam.tool.ui.CodeRemediationPageView;
import com.redteam.tool.ui.DashboardPageView;
import com.redteam.tool.ui.PdfReportExportPageView;
import com.redteam.tool.ui.SandboxScannerPageView;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Standard Servlet & HTTP Request Dispatcher for RED TEAMING TOOL.
 * Handles HTTP routing for /dashboard, /scanner, /remediation, /inspector, /reports,
 * and form action endpoints (/action/scan, /action/select-report, /action/apply-patch, /action/download-pdf).
 */
public final class RedTeamingServlet {

    private static final List<ScanReportRecord> SCAN_HISTORY;
    private static volatile ScanReportRecord activeReport;

    static {
        com.redteam.tool.dao.ScanHistoryDao.initializeDatabaseTable();
        List<ScanReportRecord> initial = KaliSuiteScannerEngine.buildInitialScanHistory();
        SCAN_HISTORY = new CopyOnWriteArrayList<>(com.redteam.tool.dao.ScanHistoryDao.loadAllReports(initial));
        activeReport = SCAN_HISTORY.isEmpty() ? initial.get(0) : SCAN_HISTORY.get(0);
    }

    private RedTeamingServlet() {}

    public static void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String rawQuery = uri.getRawQuery();

        Map<String, String> params = parseParameters(rawQuery);
        if ("POST".equalsIgnoreCase(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            params.putAll(parseParameters(body));
        }

        applySecurityHeaders(exchange);

        if (path.startsWith("/action/scan")) {
            String mode = params.getOrDefault("scanMode", "SAST_ARTIFACT");
            String targetName = params.getOrDefault("targetName", "aegis-commerce-microservice");
            String payload = params.getOrDefault("sourceOrUrl", "");

            String nmapProfile = params.getOrDefault("nmapProfile", "FULL_SERVICE_DISCOVERY");
            String niktoProfile = params.getOrDefault("niktoProfile", "FULL_PASSIVE_WEB");
            String sqlmapDbms = params.getOrDefault("sqlmapDbms", "PostgreSQL 16");
            String crawlDepth = params.getOrDefault("crawlDepth", "DEPTH_4_ENTIRE_DOMAIN");

            EncryptedSandboxEnclave.Envelope env = EncryptedSandboxEnclave.sealPayload(
                    (targetName + "\n" + payload).getBytes(StandardCharsets.UTF_8)
            );
            ScanReportRecord generated = KaliSuiteScannerEngine.executeFullScopeScan(
                    mode, targetName, payload, nmapProfile, niktoProfile, sqlmapDbms, crawlDepth, env
            );
            activeReport = generated;
            SCAN_HISTORY.add(0, generated);
            com.redteam.tool.dao.ScanHistoryDao.saveScanReport(generated);

            redirectTo(exchange, "/scanner");
            return;
        }

        if (path.startsWith("/action/select-report")) {
            String reportId = params.get("reportId");
            if (reportId != null) {
                for (ScanReportRecord r : SCAN_HISTORY) {
                    if (r.reportId().equals(reportId)) {
                        activeReport = r;
                        break;
                    }
                }
            }
            redirectTo(exchange, "/dashboard");
            return;
        }

        if (path.startsWith("/action/apply-patch")) {
            String findingId = params.getOrDefault("findingId", "ALL");
            activeReport = KaliSuiteScannerEngine.applyRemediationPatch(activeReport, findingId);
            for (int i = 0; i < SCAN_HISTORY.size(); i++) {
                if (SCAN_HISTORY.get(i).reportId().equals(activeReport.reportId())) {
                    SCAN_HISTORY.set(i, activeReport);
                    break;
                }
            }
            redirectTo(exchange, "/remediation");
            return;
        }

        if (path.startsWith("/action/download-pdf")) {
            String lang = params.getOrDefault("lang", activeReport.detectedLanguage());
            byte[] pdfBytes = BinaryPdfReportBuilder.generatePdfBytes(activeReport, lang);
            exchange.getResponseHeaders().set("Content-Type", "application/pdf");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + activeReport.reportId() + "_" + lang.toUpperCase() + "_Threat_Report.pdf\"");
            exchange.sendResponseHeaders(200, pdfBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(pdfBytes);
            }
            return;
        }

        String html;
        if ("/scanner".equals(path)) {
            html = SandboxScannerPageView.renderPage(activeReport, null);
        } else if ("/remediation".equals(path)) {
            String findingId = params.getOrDefault("findingId", activeReport.findings().isEmpty() ? "" : activeReport.findings().get(0).id());
            String lang = params.getOrDefault("lang", activeReport.detectedLanguage());
            String file = params.getOrDefault("file", activeReport.projectFiles().isEmpty() ? activeReport.targetName() : activeReport.projectFiles().get(0).filePath());
            html = CodeRemediationPageView.renderPage(activeReport, findingId, lang, file);
        } else if ("/inspector".equals(path)) {
            html = ArtifactInspectorPageView.renderPage(activeReport);
        } else if ("/reports".equals(path)) {
            String lang = params.getOrDefault("lang", activeReport.detectedLanguage());
            html = PdfReportExportPageView.renderPage(activeReport, lang);
        } else {
            html = DashboardPageView.renderPage(activeReport, SCAN_HISTORY);
        }

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseParameters(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;
        for (String pair : raw.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    private static void redirectTo(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
    }

    private static void applySecurityHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        headers.set("Referrer-Policy", "no-referrer");
    }
}
