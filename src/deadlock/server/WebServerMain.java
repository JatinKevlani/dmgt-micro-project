package deadlock.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.awt.Desktop;
import java.net.URI;

/**
 * WebServer — Lightweight HTTP server for the Deadlock Detection Simulator.
 * 
 * Uses Java's built-in com.sun.net.httpserver — no external dependencies.
 * Serves the web UI on http://localhost:8080
 */
public class WebServerMain {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API endpoint: POST /api/solve
        server.createContext("/api/solve", new ApiHandler());

        // Static files: everything else
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   DEADLOCK DETECTION SIMULATOR — WEB SERVER STARTED     ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out.println("║   URL: http://localhost:" + PORT + "                            ║");
        System.out.println("║                                                          ║");
        System.out.println("║   Press Ctrl+C to stop the server.                       ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Try to open browser automatically
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + PORT));
                System.out.println("  → Browser opened automatically.");
            }
        } catch (Exception e) {
            System.out.println("  → Open http://localhost:" + PORT + " in your browser.");
        }
    }
}
