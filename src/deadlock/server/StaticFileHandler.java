package deadlock.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.*;

/**
 * Serves static files for the web UI.
 */
public class StaticFileHandler implements HttpHandler {

    private final String webRoot;

    public StaticFileHandler() {
        this.webRoot = System.getProperty("user.dir") + File.separator + "web";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Route to index.html for root path
        if ("/".equals(path) || path.isEmpty()) {
            path = "/index.html";
        }

        // Prevent directory traversal
        if (path.contains("..")) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        File file = new File(webRoot + path.replace("/", File.separator));

        if (!file.exists() || file.isDirectory()) {
            String notFound = "<!DOCTYPE html><html><body><h1>404 Not Found</h1></body></html>";
            byte[] bytes = notFound.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(404, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
            return;
        }

        // Set content type based on extension
        String mime = guessContentType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", mime);

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        exchange.sendResponseHeaders(200, fileBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(fileBytes);
        os.close();
    }

    /** Maps file extensions to their corresponding MIME types. */
    private String guessContentType(String filename) {
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
