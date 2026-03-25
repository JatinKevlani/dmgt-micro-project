package deadlock.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import deadlock.core.BankersAlgorithm;
import deadlock.model.DeadlockResult;
import deadlock.model.SystemState;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ApiHandler — REST API endpoint for the Banker's Algorithm.
 * 
 * POST /api/solve
 *   Request:  JSON with n, m, totalResources, allocation, request
 *   Response: JSON with isSafe, safeSequence, deadlockedProcs, traceLog
 * 
 * Manual JSON parsing — no external libraries needed.
 */
public class ApiHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        // Handle preflight OPTIONS
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            String error = "{\"error\": \"Only POST method is allowed\"}";
            sendResponse(exchange, 405, error);
            return;
        }

        try {
            // Read request body
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Parse JSON input
            SystemState state = parseInput(body);

            // Run algorithm
            BankersAlgorithm algorithm = new BankersAlgorithm();
            DeadlockResult result = algorithm.runSafetyAlgorithm(state);

            // Build JSON response
            String response = buildResponse(result, state);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            String error = "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}";
            sendResponse(exchange, 400, error);
        }
    }

    // ─── Send HTTP Response ─────────────────────────────────────

    private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // ─── Parse JSON Input (manual — no libraries) ───────────────

    private SystemState parseInput(String json) {
        // Remove whitespace around structure characters
        json = json.trim();

        int n = getJsonInt(json, "n");
        int m = getJsonInt(json, "m");

        int[] totalResources = getJsonIntArray(json, "totalResources");
        int[][] allocation = getJsonInt2DArray(json, "allocation");
        int[][] request = getJsonInt2DArray(json, "request");

        // Compute available = total - columnSum(allocation)
        int[] available = new int[m];
        for (int j = 0; j < m; j++) {
            int sum = 0;
            for (int i = 0; i < n; i++) {
                sum += allocation[i][j];
            }
            available[j] = totalResources[j] - sum;
        }

        // Validate
        for (int j = 0; j < m; j++) {
            if (available[j] < 0) {
                throw new RuntimeException("Allocation exceeds Total Resources for R" + j);
            }
        }

        return new SystemState(n, m, totalResources, allocation, request, available);
    }

    // ─── Build JSON Response ────────────────────────────────────

    private String buildResponse(DeadlockResult result, SystemState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // isSafe
        sb.append("\"isSafe\":").append(result.isSafe()).append(",");

        // available
        sb.append("\"available\":[");
        for (int j = 0; j < state.getM(); j++) {
            sb.append(state.getAvailable()[j]);
            if (j < state.getM() - 1) sb.append(",");
        }
        sb.append("],");

        // safeSequence
        sb.append("\"safeSequence\":[");
        List<Integer> seq = result.getSafeSequence();
        for (int i = 0; i < seq.size(); i++) {
            sb.append(seq.get(i));
            if (i < seq.size() - 1) sb.append(",");
        }
        sb.append("],");

        // deadlockedProcs
        sb.append("\"deadlockedProcs\":[");
        List<Integer> procs = result.getDeadlockedProcs();
        for (int i = 0; i < procs.size(); i++) {
            sb.append(procs.get(i));
            if (i < procs.size() - 1) sb.append(",");
        }
        sb.append("],");

        // traceLog
        sb.append("\"traceLog\":[");
        List<String> trace = result.getTraceLog();
        for (int i = 0; i < trace.size(); i++) {
            sb.append("\"").append(escapeJson(trace.get(i))).append("\"");
            if (i < trace.size() - 1) sb.append(",");
        }
        sb.append("],");

        // ─── RAG data: n, m, allocation, request, totalResources ──
        sb.append("\"n\":").append(state.getN()).append(",");
        sb.append("\"m\":").append(state.getM()).append(",");

        // totalResources
        sb.append("\"totalResources\":[");
        for (int j = 0; j < state.getM(); j++) {
            sb.append(state.getTotalResources()[j]);
            if (j < state.getM() - 1) sb.append(",");
        }
        sb.append("],");

        // allocation 2D
        sb.append("\"allocation\":[");
        for (int i = 0; i < state.getN(); i++) {
            sb.append("[");
            for (int j = 0; j < state.getM(); j++) {
                sb.append(state.getAllocation()[i][j]);
                if (j < state.getM() - 1) sb.append(",");
            }
            sb.append("]");
            if (i < state.getN() - 1) sb.append(",");
        }
        sb.append("],");

        // request 2D
        sb.append("\"request\":[");
        for (int i = 0; i < state.getN(); i++) {
            sb.append("[");
            for (int j = 0; j < state.getM(); j++) {
                sb.append(state.getRequest()[i][j]);
                if (j < state.getM() - 1) sb.append(",");
            }
            sb.append("]");
            if (i < state.getN() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    // ─── JSON Parsing Helpers ───────────────────────────────────

    private int getJsonInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) throw new RuntimeException("Missing field: " + key);

        int colonIdx = json.indexOf(':', idx + pattern.length());
        int endIdx = colonIdx + 1;
        while (endIdx < json.length() && json.charAt(endIdx) == ' ') endIdx++;
        int start = endIdx;
        while (endIdx < json.length() && (Character.isDigit(json.charAt(endIdx)) || json.charAt(endIdx) == '-')) endIdx++;

        return Integer.parseInt(json.substring(start, endIdx).trim());
    }

    private int[] getJsonIntArray(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) throw new RuntimeException("Missing field: " + key);

        int bracketStart = json.indexOf('[', idx);
        int bracketEnd = json.indexOf(']', bracketStart);
        String arrStr = json.substring(bracketStart + 1, bracketEnd).trim();

        if (arrStr.isEmpty()) return new int[0];

        String[] parts = arrStr.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    private int[][] getJsonInt2DArray(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) throw new RuntimeException("Missing field: " + key);

        // Find the outer [ for 2D array
        int outerStart = json.indexOf('[', idx);
        // Find the matching outer ]
        int depth = 0;
        int outerEnd = outerStart;
        for (int i = outerStart; i < json.length(); i++) {
            if (json.charAt(i) == '[') depth++;
            if (json.charAt(i) == ']') depth--;
            if (depth == 0) { outerEnd = i; break; }
        }

        String content = json.substring(outerStart + 1, outerEnd).trim();

        // Count inner arrays
        int count = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '[') count++;
        }

        int[][] result = new int[count][];
        int pos = 0;
        for (int row = 0; row < count; row++) {
            int start = content.indexOf('[', pos);
            int end = content.indexOf(']', start);
            String rowStr = content.substring(start + 1, end).trim();
            String[] parts = rowStr.split(",");
            result[row] = new int[parts.length];
            for (int j = 0; j < parts.length; j++) {
                result[row][j] = Integer.parseInt(parts[j].trim());
            }
            pos = end + 1;
        }

        return result;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\u2192", "→")
                .replace("\u2264", "≤")
                .replace("\u2714", "✔")
                .replace("\u2717", "✗")
                .replace("\u2500", "─");
    }
}
