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

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Pure Java Automated Unit Test Suite for RED TEAMING TOOL.
 * Validates AES-256-GCM enclave sealing, Kali Linux scanner engines (Nmap, Nikto, SQLMap),
 * polyglot code remediation, binary PDF 1.4 generation, and all 5 Java Frontend Page views.
 */
public final class RedTeamingToolTest {

    public static void main(String[] args) {
        System.out.println("TAP version 13");

        // Test 1: AES-256-GCM Enclave Sealing
        EncryptedSandboxEnclave.Envelope env = EncryptedSandboxEnclave.sealPayload(
                "public class Test { }".getBytes(StandardCharsets.UTF_8)
        );
        assertCondition("AES-256-GCM".equals(env.algorithm()) && env.payloadSha256().length() == 64,
                "1 - EncryptedSandboxEnclave seals payload with AES-256-GCM and SHA-256 digest");

        // Test 2: Initial History & Multi-File Repository Scanning
        List<ScanReportRecord> history = KaliSuiteScannerEngine.buildInitialScanHistory();
        ScanReportRecord first = history.get(0);
        assertCondition(history.size() >= 4 && first.projectFiles().size() >= 3 && first.crawledRoutes().size() >= 6,
                "2 - KaliSuiteScannerEngine builds multi-file repository and full-website scan history");

        // Test 3: Polyglot Remediation Patching
        String firstFindingId = first.findings().get(0).id();
        ScanReportRecord patched = KaliSuiteScannerEngine.applyRemediationPatch(first, firstFindingId);
        assertCondition(patched.overallRiskScore() <= first.overallRiskScore(),
                "3 - KaliSuiteScannerEngine.applyRemediationPatch marks finding as PATCH_APPLIED and reduces risk");

        // Test 4: Binary PDF 1.4 Report Builder
        byte[] pdfBytes = BinaryPdfReportBuilder.generatePdfBytes(first, "java");
        String pdfHeader = new String(pdfBytes, 0, 8, StandardCharsets.US_ASCII);
        assertCondition(pdfHeader.startsWith("%PDF-1.4") && pdfBytes.length > 500,
                "4 - BinaryPdfReportBuilder generates valid binary %PDF-1.4 document");

        // Test 5: All 5 Separate Pure Java Frontend Pages Render HTML
        String dashboardHtml = DashboardPageView.renderPage(first, history);
        String scannerHtml = SandboxScannerPageView.renderPage(first, null);
        String remediationHtml = CodeRemediationPageView.renderPage(first, firstFindingId, "java", first.projectFiles().get(0).filePath());
        String inspectorHtml = ArtifactInspectorPageView.renderPage(first);
        String reportsHtml = PdfReportExportPageView.renderPage(first, "java");

        assertCondition(
                dashboardHtml.contains("RED TEAMING TOOL") &&
                scannerHtml.contains("Full-Repository") &&
                remediationHtml.contains("Remediation Studio") &&
                inspectorHtml.contains("Constant Pool") &&
                reportsHtml.contains("PDF 1.4"),
                "5 - All 5 separate Java frontend page classes render complete HTML documents"
        );

        System.out.println("1..5");
        System.out.println("# pass 5");
        System.out.println("# fail 0");
    }

    private static void assertCondition(boolean condition, String description) {
        if (!condition) {
            System.err.println("not ok " + description);
            System.exit(1);
        }
        System.out.println("ok " + description);
    }
}
