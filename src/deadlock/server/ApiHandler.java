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
 * API Handler for the /api/solve endpoint.
 * Parses incoming JSON POST requests containing system state,
 * runs the Banker's Algorithm, and returns a JSON response.
 */
public class ApiHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        // Process preflight OPTIONS request
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Validate request method
        if (!"POST".equals(exchange.getRequestMethod())) {
            String error = "{\"error\": \"Method not allowed.\"}";
            sendResponse(exchange, 405, error);
            return;
        }

        try {
            // Read body
            InputStream inputStream = exchange.getRequestBody();
            String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Parse state
            SystemState state = parseInput(requestBody);

            // Run algorithm
            BankersAlgorithm algorithm = new BankersAlgorithm();
            DeadlockResult result = algorithm.runSafetyAlgorithm(state);

            // Build response
            String response = buildResponse(result, state);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            // Handle errors
            String error = "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}";
            sendResponse(exchange, 400, error);
        }
    }

    /** Sends an HTTP response with the given status code and body. */
    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.close();
    }

    // --- JSON Parsing ---

    /**
     * Parses the incoming JSON string into a SystemState object.
     */
    private SystemState parseInput(String json) {
        json = json.trim();

        int n = getJsonInt(json, "n");
        int m = getJsonInt(json, "m");

        int[] totalResources = getJsonIntArray(json, "totalResources");
        int[][] allocation = getJsonInt2DArray(json, "allocation");
        int[][] request = getJsonInt2DArray(json, "request");

        // Compute available vector
        int[] available = new int[m];
        for (int j = 0; j < m; j++) {
            int totalAllocated = 0;
            for (int i = 0; i < n; i++) {
                totalAllocated += allocation[i][j];
            }
            available[j] = totalResources[j] - totalAllocated;
        }

        // Validate available vector
        for (int j = 0; j < m; j++) {
            if (available[j] < 0) {
                throw new RuntimeException("Allocation exceeds Total Resources for R" + j);
            }
        }

        return new SystemState(n, m, totalResources, allocation, request, available);
    }



    /** Constructs the JSON response string from the result. */
    private String buildResponse(DeadlockResult result, SystemState state) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Was the system safe?
        json.append("\"isSafe\":").append(result.isSafe()).append(",");

        // Available resources vector
        json.append("\"available\":[");
        for (int j = 0; j < state.getM(); j++) {
            json.append(state.getAvailable()[j]);
            if (j < state.getM() - 1) json.append(",");
        }
        json.append("],");

        // Safe execution sequence (empty if deadlocked)
        json.append("\"safeSequence\":[");
        List<Integer> seq = result.getSafeSequence();
        for (int i = 0; i < seq.size(); i++) {
            json.append(seq.get(i));
            if (i < seq.size() - 1) json.append(",");
        }
        json.append("],");

        // Deadlocked processes (empty if safe)
        json.append("\"deadlockedProcs\":[");
        List<Integer> procs = result.getDeadlockedProcs();
        for (int i = 0; i < procs.size(); i++) {
            json.append(procs.get(i));
            if (i < procs.size() - 1) json.append(",");
        }
        json.append("],");



        // Echo back input data
        json.append("\"n\":").append(state.getN()).append(",");
        json.append("\"m\":").append(state.getM()).append(",");

        json.append("\"totalResources\":[");
        for (int j = 0; j < state.getM(); j++) {
            json.append(state.getTotalResources()[j]);
            if (j < state.getM() - 1) json.append(",");
        }
        json.append("],");

        // Allocation matrix
        json.append("\"allocation\":[");
        for (int i = 0; i < state.getN(); i++) {
            json.append("[");
            for (int j = 0; j < state.getM(); j++) {
                json.append(state.getAllocation()[i][j]);
                if (j < state.getM() - 1) json.append(",");
            }
            json.append("]");
            if (i < state.getN() - 1) json.append(",");
        }
        json.append("],");

        // Request matrix
        json.append("\"request\":[");
        for (int i = 0; i < state.getN(); i++) {
            json.append("[");
            for (int j = 0; j < state.getM(); j++) {
                json.append(state.getRequest()[i][j]);
                if (j < state.getM() - 1) json.append(",");
            }
            json.append("]");
            if (i < state.getN() - 1) json.append(",");
        }
        json.append("]");

        json.append("}");
        return json.toString();
    }

    // --- JSON utility helpers ---

    /** Extracts an integer value for the given key from a JSON string. */
    private int getJsonInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) throw new RuntimeException("Missing required field: " + key);

        int colonIdx = json.indexOf(':', idx + pattern.length());
        int endIdx = colonIdx + 1;
        while (endIdx < json.length() && json.charAt(endIdx) == ' ') endIdx++;
        int start = endIdx;
        while (endIdx < json.length() && (Character.isDigit(json.charAt(endIdx)) || json.charAt(endIdx) == '-')) endIdx++;

        return Integer.parseInt(json.substring(start, endIdx).trim());
    }

    /** Extracts a 1D integer array for the given key from a JSON string. */
    private int[] getJsonIntArray(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) throw new RuntimeException("Missing required field: " + key);

        int bracketStart = json.indexOf('[', idx);
        int bracketEnd = json.indexOf(']', bracketStart);
        String arrayContent = json.substring(bracketStart + 1, bracketEnd).trim();

        if (arrayContent.isEmpty()) return new int[0];

        String[] parts = arrayContent.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    /** Extracts a 2D integer array (array of arrays) for the given key from a JSON string. */
    private int[][] getJsonInt2DArray(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) throw new RuntimeException("Missing required field: " + key);

        // Find the outer brackets of the 2D array
        int outerStart = json.indexOf('[', idx);
        int depth = 0;
        int outerEnd = outerStart;
        for (int i = outerStart; i < json.length(); i++) {
            if (json.charAt(i) == '[') depth++;
            if (json.charAt(i) == ']') depth--;
            if (depth == 0) { outerEnd = i; break; }
        }

        String content = json.substring(outerStart + 1, outerEnd).trim();

        // Count how many inner arrays (rows) there are
        int rowCount = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '[') rowCount++;
        }

        int[][] result = new int[rowCount][];
        int pos = 0;
        for (int row = 0; row < rowCount; row++) {
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

    /** Escapes special characters so strings are safe to embed in JSON. */
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
