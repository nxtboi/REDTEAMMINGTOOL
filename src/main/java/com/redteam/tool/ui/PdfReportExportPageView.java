package com.redteam.tool.ui;

import com.redteam.tool.engine.KaliSuiteScannerEngine.LanguageRemediationRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.VulnerabilityFindingRecord;

/**
 * Page 5 (Pure Java Frontend): Automated Threat Report Preview & Binary PDF 1.4 Export Page.
 * Renders the executive compliance attestation, enclave cryptographic envelope summary,
 * Kali tool telemetry, and downloadable ISO 32000-1 binary PDF 1.4 report link.
 */
public final class PdfReportExportPageView {

    private PdfReportExportPageView() {}

    public static String renderPage(ScanReportRecord report, String reportLang) {
        StringBuilder findingCards = new StringBuilder();
        int idx = 1;
        for (VulnerabilityFindingRecord f : report.findings()) {
            LanguageRemediationRecord rem = f.remediationForLanguage(reportLang);
            findingCards.append("""
                    <div style="padding:14px;border:1px solid #1E293B;border-radius:8px;margin-bottom:12px;background:#020617;">
                        <div class="mono" style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:6px;">
                            <strong>%d. [%s] %s (%s) — %s</strong>
                            <span style="color:#94A3B8;">%s · CVSS %.1f · %s:%d</span>
                        </div>
                        <p style="font-size:12px;color:#CBD5E1;margin-bottom:10px;">%s</p>
                        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
                            <pre class="code-diff-vuln">%s</pre>
                            <pre class="code-diff-safe">%s</pre>
                        </div>
                    </div>
                    """.formatted(
                    idx++,
                    f.severity(),
                    CyberThemeLayout.escapeHtml(f.id()),
                    CyberThemeLayout.escapeHtml(f.toolSource()),
                    CyberThemeLayout.escapeHtml(f.title()),
                    CyberThemeLayout.escapeHtml(f.cwe()),
                    f.cvssScore(),
                    CyberThemeLayout.escapeHtml(f.filePath()),
                    f.lineStart(),
                    CyberThemeLayout.escapeHtml(rem.explanation()),
                    CyberThemeLayout.escapeHtml(rem.vulnerableSnippet()),
                    CyberThemeLayout.escapeHtml(rem.remediatedSnippet())
            ));
        }

        String mainHtml = """
                <div class="card" style="display:flex;justify-content:space-between;align-items:center;">
                    <div>
                        <h1 style="font-size:20px;font-weight:600;">
                            Automated Threat Report &amp; Multi-Language Remediation Export (PDF 1.4)
                        </h1>
                        <p style="font-size:12px;color:#94A3B8;margin-top:4px;">
                            Report ID: <span class="mono" style="color:#F8FAFC;">%s</span> · Target: <span class="mono" style="color:#38BDF8;">%s</span>
                        </p>
                    </div>
                    <div style="display:flex;gap:8px;align-items:center;">
                        <a href="/reports?lang=java" class="btn-primary" style="padding:6px 12px;">JAVA</a>
                        <a href="/reports?lang=python" class="btn-primary" style="padding:6px 12px;">PYTHON</a>
                        <a href="/reports?lang=javascript" class="btn-primary" style="padding:6px 12px;">JAVASCRIPT</a>
                        <a href="/action/download-pdf?lang=%s" class="btn-primary">
                            Download Binary PDF (%s)
                        </a>
                    </div>
                </div>

                <div class="card">
                    <div style="display:flex;justify-content:space-between;border-bottom:1px solid #1E293B;padding-bottom:12px;margin-bottom:14px;">
                        <div>
                            <div class="mono" style="font-size:14px;font-weight:700;">
                                RED TEAMING TOOL — DEFENSIVE SANDBOX &amp; KALI SUITE THREAT AUDIT REPORT
                            </div>
                            <div style="font-size:12px;color:#94A3B8;margin-top:4px;">
                                Compliance Attestation: OWASP ASVS v4.0.3 · ISO/IEC 27001 · CFAA 18 U.S.C. § 1030
                            </div>
                        </div>
                        <div class="mono" style="text-align:right;">
                            <div style="font-weight:700;">Risk Score: %d/100 (Grade %s)</div>
                            <div style="font-size:11px;color:#94A3B8;">%s</div>
                        </div>
                    </div>

                    %s
                </div>
                """.formatted(
                CyberThemeLayout.escapeHtml(report.reportId()),
                CyberThemeLayout.escapeHtml(report.targetName()),
                CyberThemeLayout.escapeHtml(reportLang),
                reportLang.toUpperCase(),
                report.overallRiskScore(),
                report.securityPostureGrade(),
                CyberThemeLayout.escapeHtml(report.timestampIso()),
                findingCards.toString()
        );

        return CyberThemeLayout.renderShell(
                "Automated PDF Threat Reports",
                "reports",
                report,
                4,
                mainHtml
        );
    }
}
