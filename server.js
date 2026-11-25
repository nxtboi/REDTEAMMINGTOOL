const fs = require('fs');
const http = require('http');
const path = require('path');
const { spawn, spawnSync } = require('child_process');

const PORT = Number(process.env.PORT || 3000);
const CLASSES_DIR = '/tmp/redteam-classes';

function collectJavaFiles() {
  const base = path.resolve(__dirname, 'src/main/java/com/redteam/tool');
  const files = [];
  for (const sub of ['', 'ui', 'engine', 'dao', 'servlet']) {
    const dir = sub ? path.join(base, sub) : base;
    if (!fs.existsSync(dir)) continue;
    for (const entry of fs.readdirSync(dir)) {
      if (entry.endsWith('.java')) {
        files.push(path.join(dir, entry));
      }
    }
  }
  return files;
}

function tryStartJavaServer() {
  const javacCheck = spawnSync('javac', ['-version'], { encoding: 'utf-8' });
  const javaCheck = spawnSync('java', ['-version'], { encoding: 'utf-8' });
  if (javacCheck.status !== 0 || javaCheck.status !== 0) {
    return false;
  }

  fs.mkdirSync(CLASSES_DIR, { recursive: true });
  const javaFiles = collectJavaFiles();
  const compile = spawnSync('javac', ['--release', '17', '-d', CLASSES_DIR, ...javaFiles], {
    stdio: 'inherit',
  });
  if (compile.status !== 0) {
    console.error('[RED TEAMING TOOL] javac compilation failed; falling back to prebuilt bundle.');
    return false;
  }

  console.log(`[RED TEAMING TOOL] Starting Pure Java HTTP server on port ${PORT}...`);
  const child = spawn(
    'java',
    ['-cp', CLASSES_DIR, 'com.redteam.tool.RedTeamingToolApplication'],
    {
      env: { ...process.env, PORT: String(PORT) },
      stdio: 'inherit',
    }
  );

  child.on('exit', (code) => {
    process.exit(code ?? 0);
  });

  process.on('SIGTERM', () => child.kill('SIGTERM'));
  process.on('SIGINT', () => child.kill('SIGINT'));
  return true;
}

function startFallbackHttpServer() {
  const distDir = path.resolve(__dirname, 'dist');
  if (!fs.existsSync(path.join(distDir, 'index.html'))) {
    const build = spawnSync(process.execPath, [path.resolve(__dirname, 'build.js')], {
      stdio: 'inherit',
    });
    if (build.status !== 0) {
      console.error('[RED TEAMING TOOL] Failed to generate dist/ output.');
      process.exit(1);
    }
  }

  const routeMap = {
    '/': 'dashboard/index.html',
    '/dashboard': 'dashboard/index.html',
    '/scanner': 'scanner/index.html',
    '/remediation': 'remediation/index.html',
    '/inspector': 'inspector/index.html',
    '/reports': 'reports/index.html',
    '/action/scan': 'scanner/index.html',
    '/action/select-report': 'dashboard/index.html',
    '/action/apply-patch': 'remediation/index.html',
    '/action/download-pdf': 'reports/RED_TEAMING_TOOL_Threat_Report.pdf',
  };

  const server = http.createServer((req, res) => {
    const urlObj = new URL(req.url || '/', `http://127.0.0.1:${PORT}`);
    const pathname = urlObj.pathname.replace(/\/+$/, '') || '/';
    const mapped = routeMap[pathname];

    let targetFile = mapped
      ? path.join(distDir, mapped)
      : path.join(distDir, pathname.replace(/^\/+/, ''));

    if (!targetFile.startsWith(distDir) || !fs.existsSync(targetFile) || fs.statSync(targetFile).isDirectory()) {
      targetFile = path.join(distDir, 'dashboard/index.html');
    }

    const ext = path.extname(targetFile).toLowerCase();
    const contentType =
      ext === '.pdf'
        ? 'application/pdf'
        : ext === '.css'
        ? 'text/css; charset=utf-8'
        : ext === '.js'
        ? 'application/javascript; charset=utf-8'
        : 'text/html; charset=utf-8';

    const headers = {
      'Content-Type': contentType,
      'X-Content-Type-Options': 'nosniff',
      'Referrer-Policy': 'no-referrer',
    };

    if (ext === '.pdf') {
      headers['Content-Disposition'] = 'attachment; filename="RED_TEAMING_TOOL_Threat_Report.pdf"';
    }

    const content = fs.readFileSync(targetFile);
    res.writeHead(200, headers);
    res.end(content);
  });

  server.listen(PORT, '0.0.0.0', () => {
    console.log(`[RED TEAMING TOOL] Web server listening on http://0.0.0.0:${PORT}`);
  });
}

if (!tryStartJavaServer()) {
  startFallbackHttpServer();
}
