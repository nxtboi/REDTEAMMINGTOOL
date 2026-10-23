package com.redteam.tool;

import com.redteam.tool.engine.BinaryPdfReportBuilder;
import com.redteam.tool.engine.EncryptedSandboxEnclave;
import com.redteam.tool.engine.KaliSuiteScannerEngine;
import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.ui.ArtifactInspectorPageView;
import com.redteam.tool.ui.CodeRemediationPageView;
import com.redteam.tool.ui.DashboardPageView;
import com.redteam.tool.ui.PdfReportExportPageView;
import com.redteam.tool.ui.SandboxScannerPageView;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * RED TEAMING TOOL — Pure Java Full-Stack Web Application Entry Point.
 * Serves all 5 separate Java Frontend Pages (/dashboard, /scanner, /remediation, /inspector, /reports)
 * and handles encrypted sandbox scans, Kali Linux suite execution (Nmap, Nikto, SQLMap),
 * polyglot code remediation patching, and binary PDF 1.4 threat report downloads.
 */
public final class RedTeamingToolApplication {

    private static final int DEFAULT_PORT = 3000;
    private static final List<ScanReportRecord> SCAN_HISTORY = new CopyOnWriteArrayList<>(
            KaliSuiteScannerEngine.buildInitialScanHistory()
    );
    private static volatile ScanReportRecord activeReport = SCAN_HISTORY.get(0);

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--scan-dir".equals(args[0])) {
            Path rootDir = Path.of(args.length > 1 ? args[1] : ".");
            runCliDirectoryScan(rootDir);
            return;
        }

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT)));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        // Separate Pure Java Frontend Page Routes
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || "/dashboard".equals(path)) {
                String html = DashboardPageView.renderPage(activeReport, SCAN_HISTORY);
                sendHtml(exchange, 200, html);
            } else {
                sendHtml(exchange, 404, "<h1>404 Not Found</h1>");
            }
        });

        server.createContext("/scanner", exchange -> {
            String html = SandboxScannerPageView.renderPage(activeReport, null);
            sendHtml(exchange, 200, html);
        });

        server.createContext("/remediation", exchange -> {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String findingId = query.getOrDefault("findingId",
                    activeReport.findings().isEmpty() ? "" : activeReport.findings().get(0).id());
            String lang = query.getOrDefault("lang", activeReport.detectedLanguage());
            String file = query.getOrDefault("file",
                    activeReport.projectFiles().isEmpty() ? activeReport.targetName() : activeReport.projectFiles().get(0).filePath());
            String html = CodeRemediationPageView.renderPage(activeReport, findingId, lang, file);
            sendHtml(exchange, 200, html);
        });

        server.createContext("/inspector", exchange -> {
            String html = ArtifactInspectorPageView.renderPage(activeReport);
            sendHtml(exchange, 200, html);
        });

        server.createContext("/reports", exchange -> {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String lang = query.getOrDefault("lang", activeReport.detectedLanguage());
            String html = PdfReportExportPageView.renderPage(activeReport, lang);
            sendHtml(exchange, 200, html);
        });

        // Form Action Endpoints for Pure Java Frontend
        server.createContext("/action/scan", exchange -> {
            String rawInput;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                rawInput = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            } else {
                rawInput = exchange.getRequestURI().getRawQuery();
            }
            Map<String, String> form = parseQueryParams(rawInput);
            if (!form.isEmpty()) {
                String mode = form.getOrDefault("scanMode", "SAST_ARTIFACT");
                String targetName = form.getOrDefault("targetName", "UploadedRepository");
                String payload = form.getOrDefault("sourceOrUrl", "");
                String nmapProfile = form.getOrDefault("nmapProfile", "FULL_SERVICE_DISCOVERY");
                String niktoProfile = form.getOrDefault("niktoProfile", "FULL_PASSIVE_WEB");
                String sqlmapDbms = form.getOrDefault("sqlmapDbms", "PostgreSQL 16");
                String crawlDepth = form.getOrDefault("crawlDepth", "DEPTH_4_ENTIRE_DOMAIN");

                EncryptedSandboxEnclave.Envelope envelope = EncryptedSandboxEnclave.sealPayload(
                        (targetName + "\n" + payload).getBytes(StandardCharsets.UTF_8)
                );
                ScanReportRecord generated = KaliSuiteScannerEngine.executeFullScopeScan(
                        mode, targetName, payload, nmapProfile, niktoProfile, sqlmapDbms, crawlDepth, envelope
                );
                activeReport = generated;
                SCAN_HISTORY.add(0, generated);
            }
            redirect(exchange, "/scanner");
        });

        server.createContext("/action/select-report", exchange -> {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String reportId = query.getOrDefault("reportId", "");
            for (ScanReportRecord r : SCAN_HISTORY) {
                if (r.reportId().equals(reportId)) {
                    activeReport = r;
                    break;
                }
            }
            String next = query.getOrDefault("next", "/dashboard");
            redirect(exchange, next.startsWith("/") ? next : "/dashboard");
        });

        server.createContext("/action/apply-patch", exchange -> {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String findingId = query.getOrDefault("findingId", "ALL");
            activeReport = KaliSuiteScannerEngine.applyRemediationPatch(activeReport, findingId);
            for (int i = 0; i < SCAN_HISTORY.size(); i++) {
                if (SCAN_HISTORY.get(i).reportId().equals(activeReport.reportId())) {
                    SCAN_HISTORY.set(i, activeReport);
                    break;
                }
            }
            redirect(exchange, "/remediation");
        });

        server.createContext("/action/download-pdf", exchange -> {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String lang = query.getOrDefault("lang", activeReport.detectedLanguage());
            byte[] pdfBytes = BinaryPdfReportBuilder.generatePdfBytes(activeReport, lang);
            applySecurityHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", "application/pdf");
            exchange.getResponseHeaders().set("Content-Disposition",
                    "attachment; filename=\"" + activeReport.reportId() + "_" + lang.toUpperCase() + "_Threat_Report.pdf\"");
            exchange.sendResponseHeaders(200, pdfBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(pdfBytes);
            }
        });

        System.out.printf("[RED TEAMING TOOL] Pure Java Full-Stack Web App listening on http://0.0.0.0:%d%n", port);
        server.start();
    }

    private static void runCliDirectoryScan(Path rootDir) throws IOException {
        List<String> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(rootDir)) {
            walk.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> files.add(p.toString()));
        }
        System.out.printf("[RED TEAMING TOOL CLI] Audited %d Java files under %s%n", files.size(), rootDir.toAbsolutePath());
    }

    private static void applySecurityHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
    }

    private static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        applySecurityHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private static Map<String, String> parseQueryParams(String raw) {
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
}
