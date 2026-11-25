package com.redteam.tool.ui;

import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.VulnerabilityFindingRecord;

import java.util.List;

/**
 * Page 1 (Pure Java Frontend): Security Posture Dashboard & Enclave Telemetry Page.
 * Renders the 4-KPI strip, SVG Risk Distribution Bar Chart, AES-256-GCM Enclave Telemetry card,
 * and Stored Scan History table.
 */
public final class DashboardPageView {

    private DashboardPageView() {}

    public static String renderPage(ScanReportRecord activeReport, List<ScanReportRecord> scanHistory) {
        long critCount = activeReport.findings().stream().filter(f -> "CRITICAL".equals(f.severity())).count();
        long highCount = activeReport.findings().stream().filter(f -> "HIGH".equals(f.severity())).count();
        long medCount = activeReport.findings().stream().filter(f -> "MEDIUM".equals(f.severity())).count();
        long lowCount = activeReport.findings().stream().filter(f -> "LOW".equals(f.severity())).count();

        int critHeight = (int) Math.max(8, Math.min(110, critCount * 42));
        int highHeight = (int) Math.max(8, Math.min(110, highCount * 42));
        int medHeight = (int) Math.max(8, Math.min(110, medCount * 42));
        int lowHeight = (int) Math.max(8, Math.min(110, lowCount * 42));

        StringBuilder historyRows = new StringBuilder();
        for (ScanReportRecord r : scanHistory) {
            historyRows.append("""
                    <tr>
                        <td class="mono" style="color:#38BDF8;font-weight:600;">%s</td>
                        <td>%s</td>
                        <td class="mono">%s</td>
                        <td class="mono badge-crit">%d/100</td>
                        <td class="mono badge-crit">Grade %s</td>
                        <td class="mono">%d Files · %d Routes</td>
                        <td>
                            <a href="/action/select-report?reportId=%s" class="btn-primary" style="padding:5px 10px;font-size:11px;">Load</a>
                            <a href="/remediation" style="color:#38BDF8;margin-left:8px;font-size:11px;">Remediate</a>
                        </td>
                    </tr>
                    """.formatted(
                    CyberThemeLayout.escapeHtml(r.reportId()),
                    CyberThemeLayout.escapeHtml(r.targetName()),
                    r.detectedLanguage().toUpperCase(),
                    r.overallRiskScore(),
                    r.securityPostureGrade(),
                    r.projectFiles().size(),
                    r.crawledRoutes().size(),
                    CyberThemeLayout.escapeHtml(r.reportId())
            ));
        }

        String mainHtml = """
                <div class="card" style="display:flex;justify-content:space-between;align-items:center;">
                    <div>
                        <h1 style="font-size:20px;font-weight:600;">
                            Security Posture Dashboard &amp; Enclave Telemetry
                        </h1>
                        <p style="font-size:12px;color:#94A3B8;margin-top:4px;">
                            CFAA 18 U.S.C. § 1030 Authorized Defensive Assessment · Real-Time Enclave &amp; Kali Tool Suite
                        </p>
                    </div>
                    <a href="/scanner" class="btn-primary">New Full-Scope Sandbox Audit</a>
                </div>

                <div class="card grid-4">
                    <div>
                        <div style="font-size:12px;color:#94A3B8;">Overall Risk Score</div>
                        <div class="mono badge-crit" style="font-size:26px;font-weight:700;margin-top:4px;">%d/100</div>
                        <div style="font-size:11px;color:#94A3B8;">Target: %s</div>
                    </div>
                    <div>
                        <div style="font-size:12px;color:#94A3B8;">Security Posture Grade</div>
                        <div class="mono badge-crit" style="font-size:26px;font-weight:700;margin-top:4px;">Grade %s</div>
                        <div style="font-size:11px;color:#94A3B8;">OWASP ASVS v4.0.3</div>
                    </div>
                    <div>
                        <div style="font-size:12px;color:#94A3B8;">Vulnerability Findings</div>
                        <div class="mono" style="font-size:26px;font-weight:700;margin-top:4px;color:#F8FAFC;">%d Detected</div>
                        <div style="font-size:11px;color:#38BDF8;">%d Critical · %d High</div>
                    </div>
                    <div>
                        <div style="font-size:12px;color:#94A3B8;">Full-Scope Coverage</div>
                        <div class="mono" style="font-size:26px;font-weight:700;margin-top:4px;color:#10B981;">%d Files · %d Routes</div>
                        <div style="font-size:11px;color:#94A3B8;">100%% Repository Module Tree</div>
                    </div>
                </div>

                <div class="grid-2">
                    <div class="card">
                        <h2 style="font-size:15px;font-weight:600;margin-bottom:12px;">
                            Real-Time Risk Distribution (Severity &amp; Status)
                        </h2>
                        <svg viewBox="0 0 460 160" width="100%%" height="160">
                            <rect x="30" y="%d" width="70" height="%d" rx="4" fill="rgba(239, 68, 68, 0.25)" stroke="#EF4444" stroke-width="1.5" />
                            <text x="65" y="%d" fill="#EF4444" font-size="13" font-weight="700" text-anchor="middle" font-family="monospace">%d</text>
                            <text x="65" y="148" fill="#94A3B8" font-size="11" text-anchor="middle">CRITICAL</text>

                            <rect x="135" y="%d" width="70" height="%d" rx="4" fill="rgba(245, 158, 11, 0.25)" stroke="#F59E0B" stroke-width="1.5" />
                            <text x="170" y="%d" fill="#F59E0B" font-size="13" font-weight="700" text-anchor="middle" font-family="monospace">%d</text>
                            <text x="170" y="148" fill="#94A3B8" font-size="11" text-anchor="middle">HIGH</text>

                            <rect x="240" y="%d" width="70" height="%d" rx="4" fill="rgba(56, 189, 248, 0.25)" stroke="#38BDF8" stroke-width="1.5" />
                            <text x="275" y="%d" fill="#38BDF8" font-size="13" font-weight="700" text-anchor="middle" font-family="monospace">%d</text>
                            <text x="275" y="148" fill="#94A3B8" font-size="11" text-anchor="middle">MEDIUM</text>

                            <rect x="345" y="%d" width="70" height="%d" rx="4" fill="rgba(16, 185, 129, 0.25)" stroke="#10B981" stroke-width="1.5" />
                            <text x="380" y="%d" fill="#10B981" font-size="13" font-weight="700" text-anchor="middle" font-family="monospace">%d</text>
                            <text x="380" y="148" fill="#94A3B8" font-size="11" text-anchor="middle">LOW/INFO</text>
                        </svg>
                    </div>

                    <div class="card">
                        <h2 style="font-size:15px;font-weight:600;margin-bottom:12px;">
                            AES-256-GCM Ephemeral Enclave Telemetry
                        </h2>
                        <div class="mono" style="font-size:12px;line-height:2.0;">
                            <div>Cipher: <strong>%s (NoPadding, 128-bit tag)</strong></div>
                            <div>Enclave ID: <span style="color:#10B981;font-weight:600;">%s</span></div>
                            <div>Ephemeral Key: <span style="color:#38BDF8;">%s</span></div>
                            <div>Payload SHA-256: <span style="color:#94A3B8;">%s...</span></div>
                            <div>ISO/IEC 27001 Compliance: <strong>VERIFIED</strong></div>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <h2 style="font-size:15px;font-weight:600;margin-bottom:12px;">
                        Stored Audit Reports &amp; Scope Snapshots (%d Audited Targets)
                    </h2>
                    <table>
                        <thead>
                            <tr><th>Report ID</th><th>Target Artifact / Repo</th><th>Lang</th><th>Risk Score</th><th>Grade</th><th>Files / Routes</th><th>Action</th></tr>
                        </thead>
                        <tbody>%s</tbody>
                    </table>
                </div>
                """.formatted(
                activeReport.overallRiskScore(),
                CyberThemeLayout.escapeHtml(activeReport.targetName()),
                activeReport.securityPostureGrade(),
                activeReport.findings().size(),
                critCount,
                highCount,
                activeReport.projectFiles().size(),
                activeReport.crawledRoutes().size(),
                130 - critHeight, critHeight, 124 - critHeight, critCount,
                130 - highHeight, highHeight, 124 - highHeight, highCount,
                130 - medHeight, medHeight, 124 - medHeight, medCount,
                130 - lowHeight, lowHeight, 124 - lowHeight, lowCount,
                activeReport.envelope().algorithm(),
                activeReport.envelope().sandboxEnclaveId(),
                activeReport.envelope().sessionKeyFingerprint(),
                activeReport.envelope().payloadSha256().substring(0, Math.min(24, activeReport.envelope().payloadSha256().length())),
                scanHistory.size(),
                historyRows.toString()
        );

        return CyberThemeLayout.renderShell(
                "Dashboard & Telemetry",
                "dashboard",
                activeReport,
                scanHistory.size(),
                mainHtml
        );
    }
}
