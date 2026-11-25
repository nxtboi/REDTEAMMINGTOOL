package com.redteam.tool.ui;

import com.redteam.tool.engine.KaliSuiteScannerEngine.LanguageRemediationRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.ProjectFileRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.VulnerabilityFindingRecord;

/**
 * Page 3 (Pure Java Frontend): Polyglot Vulnerability Highlighting & Code Remediation Studio Page.
 * Renders the findings queue, side-by-side highlighted vulnerable code vs. tailored secure code
 * (Java 21, Python 3.12, JavaScript / Node.js), and the multi-file annotated source buffer.
 */
public final class CodeRemediationPageView {

    private CodeRemediationPageView() {}

    public static String renderPage(
            ScanReportRecord report,
            String selectedFindingId,
            String selectedLang,
            String selectedFilePath
    ) {
        VulnerabilityFindingRecord activeFinding = report.findings().isEmpty() ? null : report.findings().get(0);
        for (VulnerabilityFindingRecord f : report.findings()) {
            if (f.id().equals(selectedFindingId)) {
                activeFinding = f;
                break;
            }
        }

        if (activeFinding == null) {
            return CyberThemeLayout.renderShell("Code Remediation Studio", "remediation", report, 4,
                    "<div class='card'>No findings present in current scan report.</div>");
        }

        LanguageRemediationRecord langRem = activeFinding.remediationForLanguage(selectedLang);

        StringBuilder queueHtml = new StringBuilder();
        for (VulnerabilityFindingRecord f : report.findings()) {
            boolean isCurrent = f.id().equals(activeFinding.id());
            queueHtml.append("""
                    <div style="padding:10px;border:1px solid %s;border-radius:8px;margin-bottom:8px;background:%s;">
                        <div class="mono" style="font-size:11px;display:flex;justify-content:space-between;">
                            <span class="badge-crit">%s · %s · %s</span>
                            <span>L%d · %s</span>
                        </div>
                        <div style="font-size:12px;font-weight:600;margin-top:4px;">%s</div>
                        <div class="mono" style="font-size:11px;color:#10B981;margin-top:4px;">%s</div>
                        <div style="margin-top:6px;">
                            <a href="/remediation?findingId=%s&amp;lang=%s" style="color:#38BDF8;font-size:11px;">Inspect Finding →</a>
                        </div>
                    </div>
                    """.formatted(
                    isCurrent ? "#38BDF8" : "#1E293B",
                    isCurrent ? "rgba(56,189,248,0.10)" : "#020617",
                    f.severity(), f.cwe(), f.toolSource(),
                    f.lineStart(), f.status(),
                    CyberThemeLayout.escapeHtml(f.title()),
                    CyberThemeLayout.escapeHtml(f.filePath()),
                    CyberThemeLayout.escapeHtml(f.id()),
                    CyberThemeLayout.escapeHtml(selectedLang)
            ));
        }

        StringBuilder moduleTabs = new StringBuilder();
        ProjectFileRecord activeFile = report.projectFiles().isEmpty() ? null : report.projectFiles().get(0);
        for (ProjectFileRecord pf : report.projectFiles()) {
            if (pf.filePath().equals(selectedFilePath)) {
                activeFile = pf;
            }
            moduleTabs.append("""
                    <a href="/remediation?findingId=%s&amp;lang=%s&amp;file=%s"
                       class="mono"
                       style="padding:5px 10px;border:1px solid #1E293B;border-radius:6px;font-size:11px;color:#38BDF8;text-decoration:none;margin-right:6px;">
                        %s (%d)
                    </a>
                    """.formatted(
                    CyberThemeLayout.escapeHtml(activeFinding.id()),
                    CyberThemeLayout.escapeHtml(selectedLang),
                    CyberThemeLayout.escapeHtml(pf.filePath()),
                    CyberThemeLayout.escapeHtml(pf.filePath()),
                    pf.findingsCount()
            ));
        }

        String sourceBuffer = activeFile != null ? activeFile.content() : report.sourceCode();

        String mainHtml = """
                <div class="card" style="display:flex;justify-content:space-between;align-items:center;">
                    <div>
                        <h1 style="font-size:20px;font-weight:600;">
                            Polyglot Vulnerability Highlighting &amp; Code Remediation Studio
                        </h1>
                        <p style="font-size:12px;color:#94A3B8;margin-top:4px;">
                            Inspect highlighted vulnerable code snippets and tailored secure fixes across Java 21, Python 3.12, and JavaScript (Node.js)
                        </p>
                    </div>
                    <a href="/action/apply-patch?findingId=ALL" class="btn-primary">Apply All Verified Patches</a>
                </div>

                <div style="display:grid;grid-template-columns:4fr 8fr;gap:20px;">
                    <div class="card">
                        <h2 style="font-size:14px;font-weight:600;margin-bottom:12px;">
                            Findings Queue (%d)
                        </h2>
                        %s
                    </div>

                    <div>
                        <div class="card">
                            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:14px;">
                                <div>
                                    <div class="mono" style="font-size:12px;color:#38BDF8;">
                                        %s · %s · Tool: %s · %s · CVSS %.1f
                                    </div>
                                    <h2 style="font-size:17px;font-weight:600;margin-top:4px;">%s</h2>
                                </div>
                                <a href="/action/apply-patch?findingId=%s" class="btn-primary">Apply Remediation Patch</a>
                            </div>

                            <div style="margin-bottom:14px;padding:10px;background:#020617;border:1px solid #1E293B;border-radius:8px;display:flex;justify-content:space-between;align-items:center;">
                                <span style="font-size:12px;">Tailored Remediation Language: <strong class="mono" style="color:#38BDF8;">%s</strong></span>
                                <div>
                                    <a href="/remediation?findingId=%s&amp;lang=java" class="btn-primary" style="padding:4px 10px;margin-right:4px;">Java 21</a>
                                    <a href="/remediation?findingId=%s&amp;lang=python" class="btn-primary" style="padding:4px 10px;margin-right:4px;">Python 3.12</a>
                                    <a href="/remediation?findingId=%s&amp;lang=javascript" class="btn-primary" style="padding:4px 10px;">JavaScript (Node.js)</a>
                                </div>
                            </div>

                            <p style="font-size:12px;color:#CBD5E1;margin-bottom:14px;line-height:1.6;">
                                <strong>Remediation Engineering Rationale:</strong> %s
                            </p>

                            <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px;">
                                <div>
                                    <div class="mono" style="font-size:11px;color:#FCA5A5;margin-bottom:6px;">
                                        - HIGHLIGHTED VULNERABLE SNIPPET (%s)
                                    </div>
                                    <pre class="code-diff-vuln">%s</pre>
                                </div>
                                <div>
                                    <div class="mono" style="font-size:11px;color:#6EE7B7;margin-bottom:6px;">
                                        + CORRECTED SECURE VERSION (%s)
                                    </div>
                                    <pre class="code-diff-safe">%s</pre>
                                </div>
                            </div>
                        </div>

                        <div class="card">
                            <h3 style="font-size:14px;font-weight:600;margin-bottom:10px;">
                                Annotated Sandbox Repository Module Buffer
                            </h3>
                            <div style="margin-bottom:12px;">%s</div>
                            <pre class="mono" style="background:#020617;padding:14px;border-radius:8px;border:1px solid #1E293B;font-size:12px;overflow-x:auto;line-height:1.6;">%s</pre>
                        </div>
                    </div>
                </div>
                """.formatted(
                report.findings().size(),
                queueHtml.toString(),
                CyberThemeLayout.escapeHtml(activeFinding.id()),
                CyberThemeLayout.escapeHtml(activeFinding.cwe()),
                CyberThemeLayout.escapeHtml(activeFinding.toolSource()),
                CyberThemeLayout.escapeHtml(activeFinding.filePath()),
                activeFinding.cvssScore(),
                CyberThemeLayout.escapeHtml(activeFinding.title()),
                CyberThemeLayout.escapeHtml(activeFinding.id()),
                CyberThemeLayout.escapeHtml(langRem.label()),
                CyberThemeLayout.escapeHtml(activeFinding.id()),
                CyberThemeLayout.escapeHtml(activeFinding.id()),
                CyberThemeLayout.escapeHtml(activeFinding.id()),
                CyberThemeLayout.escapeHtml(langRem.explanation()),
                selectedLang.toUpperCase(),
                CyberThemeLayout.escapeHtml(langRem.vulnerableSnippet()),
                CyberThemeLayout.escapeHtml(langRem.label()),
                CyberThemeLayout.escapeHtml(langRem.remediatedSnippet()),
                moduleTabs.toString(),
                CyberThemeLayout.escapeHtml(sourceBuffer)
        );

        return CyberThemeLayout.renderShell(
                "Code Remediation Studio",
                "remediation",
                report,
                4,
                mainHtml
        );
    }
}
