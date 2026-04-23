package com.smartcampus;

import com.smartcampus.config.AppConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Smart Campus System — Main entry point.
 *
 * Starts an embedded Grizzly HTTP server at http://localhost:8080/api/v1/
 *
 * The full base URI (including the /api/v1 path) is passed to Grizzly.
 * AppConfig extends ResourceConfig and carries @ApplicationPath("/api/v1")
 * which Jersey uses to confirm the path — Grizzly honours the URI passed here.
 *
 * Run: java -jar target/smart-campus-api-1.0.0.jar
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /** Full base URI for the REST API — Jersey paths are relative to this */
    public static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";

    public static HttpServer startServer() throws IOException {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), new AppConfig(), false);
        server.start();
        return server;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final HttpServer server = startServer();

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       Smart Campus System  —  REST API           ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  Base URL  : http://localhost:8080/api/v1        ║");
        System.out.println("║  Discovery : GET  /api/v1                        ║");
        System.out.println("║  Rooms     : /api/v1/rooms                       ║");
        System.out.println("║  Sensors   : /api/v1/sensors                     ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println("  Press Ctrl+C to stop.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown signal — stopping server...");
            server.shutdownNow();
        }));

        Thread.currentThread().join();
    }
}
