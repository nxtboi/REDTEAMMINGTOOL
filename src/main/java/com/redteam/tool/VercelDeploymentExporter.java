package com.redteam.tool;

import com.redteam.tool.engine.BinaryPdfReportBuilder;
import com.redteam.tool.engine.KaliSuiteScannerEngine;
import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.ui.ArtifactInspectorPageView;
import com.redteam.tool.ui.CodeRemediationPageView;
import com.redteam.tool.ui.DashboardPageView;
import com.redteam.tool.ui.PdfReportExportPageView;
import com.redteam.tool.ui.SandboxScannerPageView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Pure Java Vercel Deployment Static & Binary PDF Bundle Exporter.
 * Executed during Vercel builds (`npm run vercel-build`) to compile and render all 5
 * separate Java frontend page views and binary PDF 1.4 threat reports into `dist/`.
 */
public final class VercelDeploymentExporter {

    private VercelDeploymentExporter() {}

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(args.length > 0 ? args[0] : "dist");
        Files.createDirectories(outDir);

        List<ScanReportRecord> scanHistory = KaliSuiteScannerEngine.buildInitialScanHistory();
        ScanReportRecord primaryReport = scanHistory.get(0);
        String defaultFindingId = primaryReport.findings().isEmpty()
                ? ""
                : primaryReport.findings().get(0).id();
        String defaultFilePath = primaryReport.projectFiles().isEmpty()
                ? primaryReport.targetName()
                : primaryReport.projectFiles().get(0).filePath();

        // 1. Render Page 1: DashboardPageView (/ and /dashboard)
        String dashboardHtml = DashboardPageView.renderPage(primaryReport, scanHistory);
        writeUtf8(outDir.resolve("index.html"), dashboardHtml);
        writeUtf8(outDir.resolve("dashboard/index.html"), dashboardHtml);

        // 2. Render Page 2: SandboxScannerPageView (/scanner)
        String scannerHtml = SandboxScannerPageView.renderPage(primaryReport, null);
        writeUtf8(outDir.resolve("scanner/index.html"), scannerHtml);

        // 3. Render Page 3: CodeRemediationPageView (/remediation)
        String remediationHtml = CodeRemediationPageView.renderPage(
                primaryReport,
                defaultFindingId,
                primaryReport.detectedLanguage(),
                defaultFilePath
        );
        writeUtf8(outDir.resolve("remediation/index.html"), remediationHtml);

        // 4. Render Page 4: ArtifactInspectorPageView (/inspector)
        String inspectorHtml = ArtifactInspectorPageView.renderPage(primaryReport);
        writeUtf8(outDir.resolve("inspector/index.html"), inspectorHtml);

        // 5. Render Page 5: PdfReportExportPageView (/reports)
        String reportsHtml = PdfReportExportPageView.renderPage(
                primaryReport,
                primaryReport.detectedLanguage()
        );
        writeUtf8(outDir.resolve("reports/index.html"), reportsHtml);

        // 6. Generate Binary PDF 1.4 Threat Reports via BinaryPdfReportBuilder
        byte[] pdfBytes = BinaryPdfReportBuilder.generatePdfBytes(primaryReport, "java");
        Path pdfOut = outDir.resolve("reports/RED_TEAMING_TOOL_Threat_Report.pdf");
        Files.createDirectories(pdfOut.getParent());
        Files.write(pdfOut, pdfBytes);

        System.out.printf(
                "[RED TEAMING TOOL — VERCEL EXPORTER] Exported 5 Java frontend pages and PDF 1.4 bundle (%d bytes) to %s%n",
                pdfBytes.length,
                outDir.toAbsolutePath()
        );
    }

    private static void writeUtf8(Path target, String content) throws IOException {
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        Files.writeString(target, content, StandardCharsets.UTF_8);
    }
}
