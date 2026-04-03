package deadlock.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.awt.Desktop;
import java.net.URI;

/**
 * Starts the embedded HTTP server to provide a web interface for the Simulator.
 */
public class WebServerMain {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Configure API and static file serving
        server.createContext("/api/solve", new ApiHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("  Web server started on http://localhost:" + PORT);

        // Attempt to open the default web browser automatically
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + PORT));
                System.out.println("  Browser opened automatically.");
            }
        } catch (Exception e) {
            System.out.println("  Could not launch browser. Please navigate to http://localhost:" + PORT);
        }
    }
}
