package deadlock.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.*;

/**
 * StaticFileHandler — Serves static files (HTML, CSS, JS) from the web/ directory.
 * 
 * Detects MIME types automatically for proper browser rendering.
 */
public class StaticFileHandler implements HttpHandler {

    private final String webRoot;

    public StaticFileHandler() {
        // Determine the web root directory relative to where the server is run
        this.webRoot = System.getProperty("user.dir") + File.separator + "web";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Default to index.html
        if ("/".equals(path) || path.isEmpty()) {
            path = "/index.html";
        }

        // Security: prevent directory traversal
        if (path.contains("..")) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        File file = new File(webRoot + path.replace("/", File.separator));

        if (!file.exists() || file.isDirectory()) {
            // 404 Not Found
            String notFound = "<!DOCTYPE html><html><body><h1>404 Not Found</h1></body></html>";
            byte[] bytes = notFound.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(404, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
            return;
        }

        // Set MIME type
        String mime = getMimeType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", mime);

        // Send file
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        exchange.sendResponseHeaders(200, fileBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(fileBytes);
        os.close();
    }

    /**
     * Returns MIME type based on file extension.
     */
    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (lower.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        if (lower.endsWith(".ico"))  return "image/x-icon";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff"))  return "font/woff";
        return "application/octet-stream";
    }
}
