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

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Standard Jakarta / javax HttpServlet implementation for RED TEAMING TOOL.
 * Handles HTTP request routing for /dashboard, /scanner, /remediation, /inspector, /reports,
 * and form action endpoints (/action/scan, /action/apply-patch, /action/download-pdf).
 */
@WebServlet(name = "RedTeamingServlet", urlPatterns = {"/", "/dashboard", "/scanner", "/remediation", "/inspector", "/reports", "/action/*"})
public class RedTeamingServlet extends HttpServlet {

    private static final List<ScanReportRecord> SCAN_HISTORY = new CopyOnWriteArrayList<>(
            KaliSuiteScannerEngine.buildInitialScanHistory()
    );
    private static volatile ScanReportRecord activeReport = SCAN_HISTORY.get(0);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        applySecurityHeaders(resp);

        if (path.startsWith("/action/scan")) {
            String mode = req.getParameter("scanMode");
            String targetName = req.getParameter("targetName");
            String payload = req.getParameter("sourceOrUrl");
            if (targetName == null) targetName = "UploadedRepository";
            if (payload == null) payload = "";
            if (mode == null) mode = "SAST_ARTIFACT";

            String nmapProfile = req.getParameter("nmapProfile");
            String niktoProfile = req.getParameter("niktoProfile");
            String sqlmapDbms = req.getParameter("sqlmapDbms");
            String crawlDepth = req.getParameter("crawlDepth");

            if (nmapProfile == null) nmapProfile = "FULL_SERVICE_DISCOVERY";
            if (niktoProfile == null) niktoProfile = "FULL_PASSIVE_WEB";
            if (sqlmapDbms == null) sqlmapDbms = "PostgreSQL 16";
            if (crawlDepth == null) crawlDepth = "DEPTH_4_ENTIRE_DOMAIN";

            EncryptedSandboxEnclave.Envelope env = EncryptedSandboxEnclave.sealPayload(
                    (targetName + "\n" + payload).getBytes(StandardCharsets.UTF_8)
            );
            ScanReportRecord generated = KaliSuiteScannerEngine.executeFullScopeScan(
                    mode, targetName, payload, nmapProfile, niktoProfile, sqlmapDbms, crawlDepth, env
            );
            activeReport = generated;
            SCAN_HISTORY.add(0, generated);
            resp.sendRedirect(req.getContextPath() + "/scanner");
            return;
        }

        if (path.startsWith("/action/select-report")) {
            String reportId = req.getParameter("reportId");
            if (reportId != null) {
                for (ScanReportRecord r : SCAN_HISTORY) {
                    if (r.reportId().equals(reportId)) {
                        activeReport = r;
                        break;
                    }
                }
            }
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        if (path.startsWith("/action/apply-patch")) {
            String findingId = req.getParameter("findingId");
            if (findingId == null) findingId = "ALL";
            activeReport = KaliSuiteScannerEngine.applyRemediationPatch(activeReport, findingId);
            for (int i = 0; i < SCAN_HISTORY.size(); i++) {
                if (SCAN_HISTORY.get(i).reportId().equals(activeReport.reportId())) {
                    SCAN_HISTORY.set(i, activeReport);
                    break;
                }
            }
            resp.sendRedirect(req.getContextPath() + "/remediation");
            return;
        }

        if (path.startsWith("/action/download-pdf")) {
            String lang = req.getParameter("lang");
            if (lang == null) lang = activeReport.detectedLanguage();
            byte[] pdfBytes = BinaryPdfReportBuilder.generatePdfBytes(activeReport, lang);
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + activeReport.reportId() + "_" + lang.toUpperCase() + "_Threat_Report.pdf\"");
            resp.setContentLength(pdfBytes.length);
            resp.getOutputStream().write(pdfBytes);
            return;
        }

        resp.setContentType("text/html; charset=utf-8");
        String html;
        if ("/scanner".equals(path)) {
            html = SandboxScannerPageView.renderPage(activeReport, null);
        } else if ("/remediation".equals(path)) {
            String findingId = req.getParameter("findingId");
            String lang = req.getParameter("lang");
            String file = req.getParameter("file");
            if (findingId == null) findingId = activeReport.findings().isEmpty() ? "" : activeReport.findings().get(0).id();
            if (lang == null) lang = activeReport.detectedLanguage();
            if (file == null) file = activeReport.projectFiles().isEmpty() ? activeReport.targetName() : activeReport.projectFiles().get(0).filePath();
            html = CodeRemediationPageView.renderPage(activeReport, findingId, lang, file);
        } else if ("/inspector".equals(path)) {
            html = ArtifactInspectorPageView.renderPage(activeReport);
        } else if ("/reports".equals(path)) {
            String lang = req.getParameter("lang");
            if (lang == null) lang = activeReport.detectedLanguage();
            html = PdfReportExportPageView.renderPage(activeReport, lang);
        } else {
            html = DashboardPageView.renderPage(activeReport, SCAN_HISTORY);
        }

        resp.getWriter().write(html);
    }

    private void applySecurityHeaders(HttpServletResponse resp) {
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        resp.setHeader("Referrer-Policy", "no-referrer");
    }
}
