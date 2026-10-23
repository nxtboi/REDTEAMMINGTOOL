package com.redteam.tool.ui;

import com.redteam.tool.engine.KaliSuiteScannerEngine.NiktoAdvisory;
import com.redteam.tool.engine.KaliSuiteScannerEngine.NmapPortResult;
import com.redteam.tool.engine.KaliSuiteScannerEngine.ProjectFileRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.SqlMapAuditRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.VulnerabilityFindingRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.WebsiteRouteRecord;

/**
 * Page 2 (Pure Java Frontend): Full-Repository & Entire-Website Kali Sandbox Scanner Page.
 * Renders the multi-file repository / full-domain submission form, Nmap/Nikto/SQLMap configuration
 * controls, Full-Scope Coverage tables (Scanned Repository Files & Crawled Website Routes),
 * Kali tool output tables, and the Enclave Terminal Audit Stream.
 */
public final class SandboxScannerPageView {

    private SandboxScannerPageView() {}

    public static String renderPage(ScanReportRecord report, String validationError) {
        StringBuilder fileRows = new StringBuilder();
        for (ProjectFileRecord pf : report.projectFiles()) {
            fileRows.append("""
                    <tr>
                        <td class="mono" style="color:#38BDF8;">%s</td>
                        <td class="mono">%s</td>
                        <td class="mono">%d</td>
                        <td class="mono">%d issues</td>
                    </tr>
                    """.formatted(
                    CyberThemeLayout.escapeHtml(pf.filePath()),
                    pf.language().toUpperCase(),
                    pf.linesOfCode(),
                    pf.findingsCount()
            ));
        }

        StringBuilder routeRows = new StringBuilder();
        for (WebsiteRouteRecord cr : report.crawledRoutes()) {
            routeRows.append("""
                    <tr>
                        <td class="mono" style="color:#38BDF8;">%s %s</td>
                        <td class="mono">%d</td>
                        <td class="mono">%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    cr.httpMethod(),
                    CyberThemeLayout.escapeHtml(cr.routePath()),
                    cr.statusCode(),
                    cr.status(),
                    CyberThemeLayout.escapeHtml(cr.summary())
            ));
        }

        StringBuilder nmapRows = new StringBuilder();
        for (NmapPortResult p : report.nmapResults()) {
            nmapRows.append("""
                    <tr>
                        <td class="mono" style="color:#38BDF8;font-weight:600;">%d/%s</td>
                        <td class="mono">%s</td>
                        <td class="mono"><strong>%s</strong> · %s</td>
                        <td>%s</td>
                        <td class="mono">%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    p.port(), p.protocol(), p.state().toUpperCase(),
                    CyberThemeLayout.escapeHtml(p.service()),
                    CyberThemeLayout.escapeHtml(p.versionBanner()),
                    CyberThemeLayout.escapeHtml(p.tlsPosture()),
                    p.riskLevel(),
                    CyberThemeLayout.escapeHtml(p.remediationNote())
            ));
        }

        StringBuilder niktoRows = new StringBuilder();
        for (NiktoAdvisory n : report.niktoResults()) {
            niktoRows.append("""
                    <tr>
                        <td class="mono" style="color:#38BDF8;">%s</td>
                        <td class="mono">%s %s (%d)</td>
                        <td class="mono">%s</td>
                        <td>%s</td>
                        <td class="mono" style="color:#10B981;">%s</td>
                    </tr>
                    """.formatted(
                    n.osvdbId(), n.httpMethod(), CyberThemeLayout.escapeHtml(n.uriPath()), n.statusCode(),
                    n.severity(),
                    CyberThemeLayout.escapeHtml(n.summary()),
                    CyberThemeLayout.escapeHtml(n.remediationConfig())
            ));
        }

        StringBuilder sqlmapRows = new StringBuilder();
        for (SqlMapAuditRecord s : report.sqlmapResults()) {
            sqlmapRows.append("""
                    <tr>
                        <td class="mono" style="color:#38BDF8;">%s</td>
                        <td class="mono">%s · %s</td>
                        <td class="mono">%s</td>
                        <td class="mono">%s</td>
                        <td class="mono">%.1f</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    CyberThemeLayout.escapeHtml(s.parameterName()),
                    s.location(), s.dataType(), s.dbmsBackend(), s.taintStatus(),
                    s.cvssScore(), CyberThemeLayout.escapeHtml(s.remediationSummary())
            ));
        }

        StringBuilder findingRows = new StringBuilder();
        for (VulnerabilityFindingRecord f : report.findings()) {
            findingRows.append("""
                    <tr>
                        <td class="mono"><span class="badge-crit">[%s · CVSS %.1f]</span><br/>%s · %s</td>
                        <td class="mono" style="color:#10B981;">%s:%d</td>
                        <td><strong>%s</strong><br/><span style="color:#94A3B8;">%s</span></td>
                        <td>
                            <a href="/remediation?findingId=%s" class="btn-primary" style="padding:6px 10px;font-size:11px;">
                                Inspect &amp; Remediate
                            </a>
                        </td>
                    </tr>
                    """.formatted(
                    f.severity(), f.cvssScore(), f.cwe(), f.toolSource(),
                    CyberThemeLayout.escapeHtml(f.filePath()), f.lineStart(),
                    CyberThemeLayout.escapeHtml(f.title()),
                    CyberThemeLayout.escapeHtml(f.description()),
                    CyberThemeLayout.escapeHtml(f.id())
            ));
        }

        String mainHtml = """
                <div class="grid-2">
                    <form method="GET" action="/action/scan" class="card">
                        <h1 style="font-size:18px;font-weight:600;margin-bottom:6px;">
                            Full-Repository &amp; Entire-Website Kali Sandbox Scanner
                        </h1>
                        <p style="font-size:12px;color:#94A3B8;margin-bottom:14px;">
                            Recursively audit entire multi-file software repositories (GitHub / local folders) or spider entire website domains with Nmap, Nikto &amp; SQLMap
                        </p>

                        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:12px;">
                            <div>
                                <label style="font-size:11px;color:#CBD5E1;display:block;margin-bottom:4px;">Scan Mode</label>
                                <select name="scanMode">
                                    <option value="SAST_ARTIFACT">Full-Repository SAST + Kali Suite</option>
                                    <option value="NMAP_NETWORK">Nmap Network &amp; Port Discovery</option>
                                    <option value="NIKTO_WEBSERVER">Nikto Entire-Website Spider</option>
                                    <option value="SQLMAP_AUDIT">SQLMap Full-Site Parameter Audit</option>
                                </select>
                            </div>
                            <div>
                                <label style="font-size:11px;color:#CBD5E1;display:block;margin-bottom:4px;">Entire-Website Spider Scope</label>
                                <select name="crawlDepth">
                                    <option value="DEPTH_4_ENTIRE_DOMAIN">Entire Domain (Depth 4 · 8 Routes)</option>
                                    <option value="SITEMAP_AND_API_TREE">Sitemap + REST/GraphQL Tree (6 Routes)</option>
                                    <option value="DEPTH_2_STANDARD">Core Endpoints (Depth 2 · 4 Routes)</option>
                                </select>
                            </div>
                        </div>

                        <div style="margin-bottom:12px;">
                            <label style="font-size:11px;color:#CBD5E1;display:block;margin-bottom:4px;">
                                Target Repository Identifier, GitHub URL (https://github.com/owner/repo), or Root Website Domain
                            </label>
                            <input type="text" name="targetName" value="%s" />
                        </div>

                        <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:10px;margin-bottom:12px;">
                            <div>
                                <label style="font-size:11px;color:#CBD5E1;display:block;margin-bottom:4px;">Nmap Profile</label>
                                <select name="nmapProfile">
                                    <option value="FULL_SERVICE_DISCOVERY">Service &amp; Banner (-sV)</option>
                                    <option value="TOP_100_PORTS">Fast Top 100 Ports (-F)</option>
                                    <option value="TLS_CIPHER_ENUM">TLS Cipher Posture</option>
                                </select>
                            </div>
                            <div>
                                <label style="font-size:11px;color:#CBD5E1;display:block;margin-bottom:4px;">Nikto Profile</label>
                                <select name="niktoProfile">
                                    <option value="FULL_PASSIVE_WEB">Full Passive Posture</option>
                                    <option value="MISCONFIG_AND_HEADERS">Headers &amp; Misconfig</option>
                                    <option value="SERVLET_AND_ACTUATOR_AUDIT">Actuator &amp; Servlet</option>
                                </select>
                            </div>
                            <div>
                                <label style="font-size:11px;color:#CBD5E1;display:block;margin-bottom:4px;">SQLMap DBMS Dialect</label>
                                <select name="sqlmapDbms">
                                    <option value="PostgreSQL 16">PostgreSQL 16</option>
                                    <option value="MySQL 8.0">MySQL 8.0</option>
                                    <option value="Oracle 21c">Oracle 21c</option>
                                </select>
                            </div>
                        </div>

                        <div style="margin-bottom:12px;">
                            <label style="font-size:11px;color:#CBD5E1;display:block;margin-bottom:4px;">
                                Source Code / Multi-File Manifest or Target Root URL
                            </label>
                            <textarea name="sourceOrUrl" rows="5">%s</textarea>
                        </div>

                        <div style="display:flex;justify-content:space-between;align-items:center;margin-top:14px;">
                            <span class="mono" style="font-size:11px;color:#94A3B8;">
                                CFAA 18 U.S.C. § 1030 &amp; ISO/IEC 27001 Authorized Scope
                            </span>
                            <button type="submit" class="btn-primary">Run Full-Scope Sandbox Audit</button>
                        </div>
                    </form>

                    <div class="card">
                        <h2 style="font-size:16px;font-weight:600;margin-bottom:10px;">
                            Sandbox Cryptographic Envelope (SEALED)
                        </h2>
                        <div class="mono" style="font-size:12px;line-height:2.0;">
                            <div>Algorithm: <strong>%s</strong></div>
                            <div>Full-Scope Coverage: <span style="color:#38BDF8;">%d Repo Files · %d Web Routes</span></div>
                            <div>Enclave ID: %s</div>
                            <div>KMS Ephemeral Key: %s</div>
                            <div>Risk Index / Grade: <span class="badge-crit">%d/100 · Grade %s</span></div>
                            <div style="word-break:break-all;font-size:11px;color:#94A3B8;margin-top:6px;">
                                Aggregate SHA-256: %s
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <h2 style="font-size:16px;font-weight:600;margin-bottom:12px;">
                        Full-Scope Coverage: Scanned Software Repository Modules (%d Files) &amp; Crawled Website Routes (%d Endpoints)
                    </h2>
                    <div class="grid-2">
                        <div>
                            <h3 style="font-size:13px;color:#CBD5E1;margin-bottom:8px;">Scanned Repository Modules</h3>
                            <table>
                                <thead><tr><th>File Path</th><th>Lang</th><th>LOC</th><th>Findings</th></tr></thead>
                                <tbody>%s</tbody>
                            </table>
                        </div>
                        <div>
                            <h3 style="font-size:13px;color:#CBD5E1;margin-bottom:8px;">Entire-Website Spider Endpoints</h3>
                            <table>
                                <thead><tr><th>Route</th><th>HTTP</th><th>Posture</th><th>Summary</th></tr></thead>
                                <tbody>%s</tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <h2 style="font-size:16px;font-weight:600;margin-bottom:12px;">
                        Kali Linux Assessment Suite Output (Nmap · Nikto · SQLMap)
                    </h2>
                    <h3 style="font-size:13px;color:#38BDF8;margin:10px 0 6px;">1. Nmap Network &amp; Port Discovery</h3>
                    <table>
                        <thead><tr><th>Port/Proto</th><th>State</th><th>Service &amp; Banner</th><th>TLS Posture</th><th>Risk</th><th>Remediation</th></tr></thead>
                        <tbody>%s</tbody>
                    </table>

                    <h3 style="font-size:13px;color:#F59E0B;margin:16px 0 6px;">2. Nikto Web Server &amp; Header Scanner</h3>
                    <table>
                        <thead><tr><th>Advisory ID</th><th>Method · URI</th><th>Severity</th><th>Misconfiguration</th><th>Recommended Header / Config Fix</th></tr></thead>
                        <tbody>%s</tbody>
                    </table>

                    <h3 style="font-size:13px;color:#EF4444;margin:16px 0 6px;">3. SQLMap Parameter Taint &amp; SQLi Verifier</h3>
                    <table>
                        <thead><tr><th>Parameter</th><th>Location · Type</th><th>DBMS</th><th>Taint Status</th><th>CVSS</th><th>Parameterization Guidance</th></tr></thead>
                        <tbody>%s</tbody>
                    </table>
                </div>

                <div class="card">
                    <h2 style="font-size:16px;font-weight:600;margin-bottom:12px;">
                        Detected Vulnerability Findings Across Entire Target (%d Total)
                    </h2>
                    <table>
                        <thead><tr><th>Severity · CWE · Tool</th><th>Module Path</th><th>Finding &amp; Root Cause</th><th>Action</th></tr></thead>
                        <tbody>%s</tbody>
                    </table>
                </div>
                """.formatted(
                CyberThemeLayout.escapeHtml(report.targetName()),
                CyberThemeLayout.escapeHtml(report.sourceCode()),
                report.envelope().algorithm(),
                report.projectFiles().size(),
                report.crawledRoutes().size(),
                report.envelope().sandboxEnclaveId(),
                report.envelope().sessionKeyFingerprint(),
                report.overallRiskScore(),
                report.securityPostureGrade(),
                report.envelope().payloadSha256(),
                report.projectFiles().size(),
                report.crawledRoutes().size(),
                fileRows.toString(),
                routeRows.toString(),
                nmapRows.toString(),
                niktoRows.toString(),
                sqlmapRows.toString(),
                report.findings().size(),
                findingRows.toString()
        );

        return CyberThemeLayout.renderShell(
                "Kali & Sandbox Scanner",
                "scanner",
                report,
                4,
                mainHtml
        );
    }
}
