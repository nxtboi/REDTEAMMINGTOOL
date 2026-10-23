package com.redteam.tool.ui;

import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;

/**
 * Shared Pure Java Frontend Layout & Cybersecurity Dark Mode Theme Shell.
 * Renders the left sidebar navigation, top enclave status header, CSS stylesheet,
 * and compliance footer across all separate Java frontend page classes.
 */
public final class CyberThemeLayout {

    private CyberThemeLayout() {}

    public static String renderShell(
            String pageTitle,
            String activeNavKey,
            ScanReportRecord activeReport,
            int totalStoredScans,
            String mainContentHtml
    ) {
        return """
                <!DOCTYPE html>
                <html lang="en" class="dark">
                <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>RED TEAMING TOOL — %s</title>
                    <style>
                        :root {
                            --bg-canvas: #0B0F17;
                            --bg-sidebar: #080C14;
                            --bg-card: #111827;
                            --bg-terminal: #070A10;
                            --border-slate: #1E293B;
                            --text-primary: #F8FAFC;
                            --text-secondary: #94A3B8;
                            --accent-sky: #38BDF8;
                            --accent-emerald: #10B981;
                            --accent-amber: #F59E0B;
                            --accent-red: #EF4444;
                        }
                        * { box-sizing: border-box; margin: 0; padding: 0; }
                        body {
                            background: var(--bg-canvas);
                            color: var(--text-primary);
                            font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            display: flex;
                            min-height: 100vh;
                        }
                        aside.sidebar {
                            width: 260px;
                            background: var(--bg-sidebar);
                            border-right: 1px solid var(--border-slate);
                            padding: 20px;
                            display: flex;
                            flex-direction: column;
                            justify-content: space-between;
                            flex-shrink: 0;
                        }
                        .brand {
                            font-size: 17px;
                            font-weight: 700;
                            letter-spacing: -0.02em;
                            color: var(--text-primary);
                            text-decoration: none;
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            padding-bottom: 14px;
                            border-bottom: 1px solid var(--border-slate);
                        }
                        .brand-dot {
                            width: 10px;
                            height: 10px;
                            border-radius: 50%%;
                            background: var(--accent-sky);
                            box-shadow: 0 0 10px rgba(56, 189, 248, 0.7);
                        }
                        .brand-sub { font-size: 11px; color: var(--text-secondary); margin-top: 6px; }
                        nav.nav-links { display: flex; flex-direction: column; gap: 6px; margin-top: 18px; }
                        a.nav-btn {
                            display: block;
                            padding: 10px 12px;
                            border-radius: 8px;
                            font-size: 12px;
                            font-weight: 500;
                            color: #CBD5E1;
                            text-decoration: none;
                            border: 1px solid transparent;
                            transition: all 0.15s ease;
                        }
                        a.nav-btn:hover { background: #0F172A; color: #FFFFFF; }
                        a.nav-btn.active {
                            background: rgba(56, 189, 248, 0.15);
                            color: #7DD3FC;
                            border-color: rgba(56, 189, 248, 0.4);
                            font-weight: 600;
                        }
                        .snapshot-box {
                            margin-top: 20px;
                            padding: 12px;
                            background: #020617;
                            border: 1px solid var(--border-slate);
                            border-radius: 8px;
                            font-family: 'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, monospace;
                            font-size: 11px;
                            line-height: 1.65;
                        }
                        .workspace { flex: 1; display: flex; flex-direction: column; min-width: 0; }
                        header.topbar {
                            padding: 14px 24px;
                            border-bottom: 1px solid var(--border-slate);
                            background: rgba(11, 15, 23, 0.95);
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            font-size: 12px;
                        }
                        main.container {
                            max-width: 1320px;
                            width: 100%%;
                            margin: 0 auto;
                            padding: 24px;
                            flex: 1;
                        }
                        footer.footerbar {
                            border-top: 1px solid var(--border-slate);
                            padding: 14px 24px;
                            font-size: 11px;
                            color: var(--text-secondary);
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                        }
                        .card {
                            background: var(--bg-card);
                            border: 1px solid var(--border-slate);
                            border-radius: 12px;
                            padding: 20px;
                            margin-bottom: 20px;
                        }
                        .grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
                        .grid-2 { display: grid; grid-template-columns: 7fr 5fr; gap: 20px; }
                        .mono { font-family: 'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, monospace; }
                        .btn-primary {
                            background: var(--accent-sky);
                            color: #020617;
                            font-weight: 600;
                            font-size: 12px;
                            padding: 8px 16px;
                            border-radius: 8px;
                            border: none;
                            text-decoration: none;
                            cursor: pointer;
                            display: inline-block;
                        }
                        .btn-primary:hover { background: #7DD3FC; }
                        table { width: 100%%; border-collapse: collapse; font-size: 12px; }
                        th, td { text-align: left; padding: 10px 8px; border-bottom: 1px solid var(--border-slate); vertical-align: top; }
                        th { color: var(--text-secondary); font-weight: 500; }
                        .badge-crit { color: var(--accent-red); font-weight: 700; }
                        .badge-high { color: var(--accent-amber); font-weight: 700; }
                        .badge-med { color: var(--accent-sky); }
                        .badge-ok { color: var(--accent-emerald); font-weight: 600; }
                        pre.code-diff-vuln {
                            background: rgba(239, 68, 68, 0.12);
                            border: 1px solid rgba(239, 68, 68, 0.45);
                            color: #FECACA;
                            padding: 12px;
                            border-radius: 8px;
                            font-family: 'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, monospace;
                            font-size: 12px;
                            overflow-x: auto;
                            line-height: 1.55;
                        }
                        pre.code-diff-safe {
                            background: rgba(16, 185, 129, 0.12);
                            border: 1px solid rgba(16, 185, 129, 0.45);
                            color: #A7F3D0;
                            padding: 12px;
                            border-radius: 8px;
                            font-family: 'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, monospace;
                            font-size: 12px;
                            overflow-x: auto;
                            line-height: 1.55;
                        }
                        input, select, textarea {
                            width: 100%%;
                            background: #020617;
                            border: 1px solid var(--border-slate);
                            color: var(--text-primary);
                            padding: 8px 12px;
                            border-radius: 8px;
                            font-family: 'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, monospace;
                            font-size: 12px;
                        }
                        @media (max-width: 960px) {
                            body { flex-direction: column; }
                            aside.sidebar { width: 100%%; border-right: none; border-bottom: 1px solid var(--border-slate); }
                            nav.nav-links { flex-direction: row; flex-wrap: wrap; }
                            .grid-4 { grid-template-columns: repeat(2, 1fr); }
                            .grid-2 { grid-template-columns: 1fr; }
                        }
                    </style>
                </head>
                <body>
                    <aside class="sidebar" aria-label="Primary Workspace Navigation">
                        <div>
                            <a href="/dashboard" class="brand">
                                <span class="brand-dot"></span>
                                <span>RED TEAMING TOOL</span>
                            </a>
                            <div class="brand-sub">Pure Java Full-Repo &amp; Kali Enclave</div>
                            <nav class="nav-links" aria-label="Primary Navigation">
                                <a href="/dashboard" class="nav-btn %s">Dashboard &amp; Analytics</a>
                                <a href="/scanner" class="nav-btn %s">Kali &amp; Sandbox Scanner</a>
                                <a href="/remediation" class="nav-btn %s">Code Remediation</a>
                                <a href="/inspector" class="nav-btn %s">Artifact Inspector</a>
                                <a href="/reports" class="nav-btn %s">Automated PDF Reports</a>
                            </nav>
                            <div class="snapshot-box">
                                <div style="color:#94A3B8;">Active Target Snapshot</div>
                                <div style="color:#F8FAFC;font-weight:600;margin-top:4px;">%s</div>
                                <div style="color:#38BDF8;">Lang: %s · Grade %s</div>
                                <div style="color:#94A3B8;">Scope: %d Files · %d Routes</div>
                                <div style="color:#10B981;">Stored Scans: %d</div>
                            </div>
                        </div>
                        <div style="margin-top:16px;">
                            <a href="/action/download-pdf?lang=%s" class="btn-primary" style="width:100%%;text-align:center;">
                                Export PDF Report
                            </a>
                            <p style="font-size:11px;color:#64748B;margin-top:10px;line-height:1.4;">
                                CFAA 18 U.S.C. § 1030 · ISO/IEC 27001 · OWASP ASVS v4.0.3
                            </p>
                        </div>
                    </aside>
                    <div class="workspace">
                        <header class="topbar">
                            <div class="mono" style="color:#94A3B8;">
                                RED TEAMING TOOL / <strong style="color:#F8FAFC;">%s</strong> · <span style="color:#38BDF8;">%s</span>
                            </div>
                            <div style="display:flex;gap:10px;align-items:center;">
                                <a href="/reports" class="btn-primary" style="background:#1E293B;color:#F8FAFC;border:1px solid #334155;">
                                    Threat Report Preview
                                </a>
                                <a href="/action/download-pdf?lang=%s" class="btn-primary">
                                    Export PDF Report
                                </a>
                            </div>
                        </header>
                        <main class="container">
                            %s
                        </main>
                        <footer class="footerbar">
                            <div>RED TEAMING TOOL · Pure Java Defensive Sandbox Assessment Platform (CFAA 18 U.S.C. § 1030 · ISO/IEC 27001 · OWASP ASVS v4.0.3)</div>
                            <div><a href="/action/download-pdf?lang=%s" style="color:#38BDF8;text-decoration:underline;">Download Binary PDF 1.4</a></div>
                        </footer>
                    </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(pageTitle),
                "dashboard".equals(activeNavKey) ? "active" : "",
                "scanner".equals(activeNavKey) ? "active" : "",
                "remediation".equals(activeNavKey) ? "active" : "",
                "inspector".equals(activeNavKey) ? "active" : "",
                "reports".equals(activeNavKey) ? "active" : "",
                escapeHtml(activeReport.targetName()),
                activeReport.detectedLanguage().toUpperCase(),
                activeReport.securityPostureGrade(),
                activeReport.projectFiles().size(),
                activeReport.crawledRoutes().size(),
                totalStoredScans,
                activeReport.detectedLanguage(),
                escapeHtml(pageTitle),
                escapeHtml(activeReport.targetName()),
                activeReport.detectedLanguage(),
                mainContentHtml,
                activeReport.detectedLanguage()
        );
    }

    public static String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
