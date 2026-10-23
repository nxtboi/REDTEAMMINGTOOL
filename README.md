# RED TEAMING TOOL

**Full-Scope Defensive Sandbox Vulnerability Scanner, Kali Linux Security Suite & Polyglot Code Remediation Platform (100% Pure Java Web Application)**

---

## Pure Java Multi-Page Frontend & Engine Architecture

Every page, layout shell, security engine, and PDF generator in **RED TEAMING TOOL** is written strictly in **Java** under `src/main/java/com/redteam/tool/`:

| Route | Java Class | Responsibility |
| :--- | :--- | :--- |
| `GET /dashboard` | [`DashboardPageView.java`](src/main/java/com/redteam/tool/ui/DashboardPageView.java) | **Page 1 — Dashboard & Analytics**: Renders the 4-metric KPI strip, server-side SVG Risk Distribution Bar Chart, AES-256-GCM Enclave Telemetry card, and Stored Scan History table. |
| `GET /scanner` | [`SandboxScannerPageView.java`](src/main/java/com/redteam/tool/ui/SandboxScannerPageView.java) | **Page 2 — Kali & Sandbox Scanner**: Renders the multi-mode scan form (`SAST_ARTIFACT`, `NMAP_NETWORK`, `NIKTO_WEBSERVER`, `SQLMAP_AUDIT`), Nmap/Nikto/SQLMap parameter selectors, Full-Repository / Entire-Website Scope table, and Kali tool output tables. |
| `GET /remediation` | [`CodeRemediationPageView.java`](src/main/java/com/redteam/tool/ui/CodeRemediationPageView.java) | **Page 3 — Code Remediation Studio**: Renders the interactive vulnerability queue, side-by-side vulnerable vs. remediated diff blocks (`Java 21`, `Python 3.12`, `JavaScript / Node.js`), one-click patch controls, and multi-file source viewer. |
| `GET /inspector` | [`ArtifactInspectorPageView.java`](src/main/java/com/redteam/tool/ui/ArtifactInspectorPageView.java) | **Page 4 — Artifact & AST Inspector**: Renders the JVM Classfile & Constant Pool disassembly, AST Method Sink table, and CycloneDX 1.5 Software Bill of Materials (SBOM). |
| `GET /reports` | [`PdfReportExportPageView.java`](src/main/java/com/redteam/tool/ui/PdfReportExportPageView.java) | **Page 5 — Automated PDF Reports**: Renders the executive threat report preview and triggers binary `%PDF-1.4` report downloads (`/action/download-pdf`). |
| Shared Shell | [`CyberThemeLayout.java`](src/main/java/com/redteam/tool/ui/CyberThemeLayout.java) | **Shared Dark-Mode Cybersecurity UI Shell**: Renders the complete CSS design system (`#0B0F17` obsidian canvas), 260px left-sidebar navigation, active target snapshot card, and compliance footer. |
| HTTP Server | [`RedTeamingToolApplication.java`](src/main/java/com/redteam/tool/RedTeamingToolApplication.java) | **Main Java HTTP Server**: Serves `/dashboard`, `/scanner`, `/remediation`, `/inspector`, `/reports`, and `/action/*` endpoints on port `3000`. |
| Vercel Exporter | [`VercelDeploymentExporter.java`](src/main/java/com/redteam/tool/VercelDeploymentExporter.java) | **Pure Java Vercel Bundle Generator**: Compiles and renders all 5 Java frontend pages and binary PDF 1.4 reports into `dist/` during Vercel builds. |

---

## Deploying to Vercel

This repository is pre-configured for one-click **Vercel** deployment via [`vercel.json`](vercel.json) and [`VercelDeploymentExporter.java`](src/main/java/com/redteam/tool/VercelDeploymentExporter.java):

1. Push this repository to **GitHub**.
2. Go to [vercel.com/new](https://vercel.com/new) and import your `RED TEAMING TOOL` repository.
3. Vercel automatically reads [`vercel.json`](vercel.json):
   - **Build Command**: `npm run build` or `npm run vercel-build` (uses `build.js` and `VercelDeploymentExporter` to compile Java sources and output the rendered pages and binary PDF 1.4 report into `dist/`).
   - **Output Directory**: `dist`
   - **Multi-Page Rewrites**: Routes `/`, `/dashboard`, `/scanner`, `/remediation`, `/inspector`, `/reports`, and `/action/download-pdf` cleanly across your Vercel deployment.

---

## Running Locally with Pure Java (`javac` / `java`)

```bash
mkdir -p /tmp/redteam-classes
javac --release 17 -d /tmp/redteam-classes \
  src/main/java/com/redteam/tool/*.java \
  src/main/java/com/redteam/tool/ui/*.java \
  src/main/java/com/redteam/tool/engine/*.java

PORT=3000 java -cp /tmp/redteam-classes com.redteam.tool.RedTeamingToolApplication
```

Open **`http://localhost:3000/dashboard`** in your browser.

---

## Legal & Compliance Notice

**RED TEAMING TOOL** is engineered strictly for **authorized defensive security auditing, static code analysis, and educational vulnerability remediation** in accordance with:

- **Computer Fraud and Abuse Act (CFAA — 18 U.S.C. § 1030)**
- **ISO/IEC 27001 & ISO/IEC 27034 Application Security Controls**
- **OWASP Application Security Verification Standard (ASVS v4.0.3)**
