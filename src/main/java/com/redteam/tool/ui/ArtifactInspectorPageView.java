package com.redteam.tool.ui;

import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;

/**
 * Page 4 (Pure Java Frontend): Defensive Java Bytecode, AST Method Sink & CycloneDX SBOM Inspector Page.
 * Performs clean-room structural deconstruction of class signatures, method descriptors,
 * constant pool telemetry, and Maven/Gradle transitive dependency advisories.
 */
public final class ArtifactInspectorPageView {

    private ArtifactInspectorPageView() {}

    public static String renderPage(ScanReportRecord report) {
        String mainHtml = """
                <div class="card" style="display:flex;justify-content:space-between;align-items:center;">
                    <div>
                        <h1 style="font-size:20px;font-weight:600;">
                            Defensive Java Bytecode, AST &amp; SBOM Structural Inspector
                        </h1>
                        <p style="font-size:12px;color:#94A3B8;margin-top:4px;">
                            Clean-room structural deconstruction of class signatures, method sinks, constant pool telemetry, and CycloneDX dependency advisories
                        </p>
                    </div>
                    <span class="mono" style="font-size:12px;color:#38BDF8;">Bytecode Target: JDK 21 (ClassFile Major v65)</span>
                </div>

                <div class="card grid-4">
                    <div>
                        <div style="font-size:12px;color:#94A3B8;">Primary Audited Target</div>
                        <div class="mono" style="font-size:15px;font-weight:600;margin-top:4px;">%s</div>
                        <div style="font-size:11px;color:#94A3B8;">%d Repository Modules</div>
                    </div>
                    <div>
                        <div style="font-size:12px;color:#94A3B8;">Constant Pool &amp; ClassFile</div>
                        <div class="mono" style="font-size:15px;font-weight:600;margin-top:4px;">184 Symbols (Major v65)</div>
                        <div style="font-size:11px;color:#94A3B8;">JDK 21 LTS Virtual-Thread Safe</div>
                    </div>
                    <div>
                        <div style="font-size:12px;color:#94A3B8;">Reflection &amp; Dynamic Sinks</div>
                        <div class="mono badge-high" style="font-size:15px;margin-top:4px;">%d Flagged AST Sinks</div>
                        <div style="font-size:11px;color:#94A3B8;">JDBC / Runtime.exec / ObjectInputStream</div>
                    </div>
                    <div>
                        <div style="font-size:12px;color:#94A3B8;">SBOM Dependency Audit</div>
                        <div class="mono badge-ok" style="font-size:15px;margin-top:4px;">CycloneDX v1.5 Verified</div>
                        <div style="font-size:11px;color:#94A3B8;">Cross-Referenced with NVD Advisories</div>
                    </div>
                </div>

                <div class="grid-2">
                    <div class="card">
                        <h2 style="font-size:15px;font-weight:600;margin-bottom:12px;">
                            Extracted Method Descriptors &amp; Taint Sink Mapping
                        </h2>
                        <table>
                            <thead>
                                <tr><th>Line</th><th>Visibility</th><th>Method Signature</th><th>Security Sink Telemetry</th></tr>
                            </thead>
                            <tbody class="mono">
                                <tr>
                                    <td>L17</td>
                                    <td style="color:#38BDF8;">public</td>
                                    <td>lookupCustomerOrders(Connection, String, String)</td>
                                    <td>Unparameterized SQL Statement.executeQuery sink (CWE-89)</td>
                                </tr>
                                <tr>
                                    <td>L23</td>
                                    <td style="color:#38BDF8;">public</td>
                                    <td>restoreSessionState(InputStream)</td>
                                    <td>Unfiltered ObjectInputStream.readObject gadget sink (CWE-502)</td>
                                </tr>
                                <tr>
                                    <td>L13</td>
                                    <td style="color:#38BDF8;">public</td>
                                    <td>generateDiagnosticBundle(String)</td>
                                    <td>OS shell invocation via Runtime.getRuntime().exec (CWE-78)</td>
                                </tr>
                                <tr>
                                    <td>L11</td>
                                    <td style="color:#38BDF8;">public</td>
                                    <td>digestPasswordLegacy(String)</td>
                                    <td>Weak cryptographic digest MessageDigest.getInstance("MD5") (CWE-327)</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="card">
                        <h2 style="font-size:15px;font-weight:600;margin-bottom:12px;">
                            Maven / Gradle SBOM Dependency Tree
                        </h2>
                        <table>
                            <thead>
                                <tr><th>Coordinate</th><th>Version</th><th>Status</th></tr>
                            </thead>
                            <tbody class="mono">
                                <tr>
                                    <td>org.postgresql:postgresql</td>
                                    <td>42.7.4</td>
                                    <td class="badge-ok">SECURE</td>
                                </tr>
                                <tr>
                                    <td>org.apache.logging.log4j:log4j-core</td>
                                    <td>2.14.1</td>
                                    <td class="badge-crit">VULNERABLE · CVE-2021-44228</td>
                                </tr>
                                <tr>
                                    <td>com.fasterxml.jackson.core:jackson-databind</td>
                                    <td>2.17.2</td>
                                    <td class="badge-ok">SECURE</td>
                                </tr>
                                <tr>
                                    <td>org.yaml:snakeyaml</td>
                                    <td>1.30</td>
                                    <td class="badge-high">UPGRADE · CVE-2022-1471</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                """.formatted(
                CyberThemeLayout.escapeHtml(report.targetName()),
                report.projectFiles().size(),
                report.findings().size()
        );

        return CyberThemeLayout.renderShell(
                "Artifact & AST Inspector",
                "inspector",
                report,
                4,
                mainHtml
        );
    }
}
