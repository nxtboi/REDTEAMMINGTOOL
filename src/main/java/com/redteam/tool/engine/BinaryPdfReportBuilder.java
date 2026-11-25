package com.redteam.tool.engine;

import com.redteam.tool.engine.KaliSuiteScannerEngine.ScanReportRecord;
import com.redteam.tool.engine.KaliSuiteScannerEngine.VulnerabilityFindingRecord;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Zero-Dependency Pure Java Binary PDF 1.4 Threat Report Builder (ISO 32000-1).
 * Generates a valid binary PDF document with cross-reference table and trailer.
 */
public final class BinaryPdfReportBuilder {

    private BinaryPdfReportBuilder() {}

    public static byte[] generatePdfBytes(ScanReportRecord report, String lang) {
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 12 Tf\n50 760 Td\n");
        stream.append("(RED TEAMING TOOL - AUTOMATED THREAT AUDIT REPORT) Tj\n");
        stream.append("0 -18 Td\n/F1 9 Tf\n");
        stream.append("(Report ID: ").append(escapePdf(report.reportId()))
              .append(" | Target: ").append(escapePdf(report.targetName())).append(") Tj\n");
        stream.append("0 -14 Td\n");
        stream.append("(Enclave: ").append(escapePdf(report.envelope().sandboxEnclaveId()))
              .append(" | Cipher: AES-256-GCM | Risk Score: ").append(report.overallRiskScore()).append("/100) Tj\n");

        for (VulnerabilityFindingRecord f : report.findings()) {
            stream.append("0 -18 Td\n");
            stream.append("([").append(f.severity()).append("] ")
                  .append(escapePdf(f.id())).append(" - ")
                  .append(escapePdf(f.cwe())).append(" in ")
                  .append(escapePdf(f.filePath())).append(") Tj\n");
        }
        stream.append("ET\n");

        byte[] contentBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);
        List<Integer> offsets = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeAscii(out, "%PDF-1.4\n");
        offsets.add(out.size());
        writeAscii(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        offsets.add(out.size());
        writeAscii(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        offsets.add(out.size());
        writeAscii(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");
        offsets.add(out.size());
        writeAscii(out, "4 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
        out.writeBytes(contentBytes);
        writeAscii(out, "\nendstream\nendobj\n");
        offsets.add(out.size());
        writeAscii(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n");

        int xrefStart = out.size();
        writeAscii(out, "xref\n0 6\n0000000000 65535 f \n");
        for (int off : offsets) {
            writeAscii(out, String.format("%010d 00000 n \n", off));
        }
        writeAscii(out, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF\n");
        return out.toByteArray();
    }

    private static void writeAscii(ByteArrayOutputStream out, String text) {
        out.writeBytes(text.getBytes(StandardCharsets.US_ASCII));
    }

    private static String escapePdf(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
