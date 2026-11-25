package com.redteam.tool;

import com.redteam.tool.servlet.RedTeamingServlet;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * RED TEAMING TOOL — Standard Java HttpServer Application Container Entry Point.
 */
public final class RedTeamingToolApplication {

    private static final int DEFAULT_PORT = 3000;

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT)));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/", exchange -> {
            try {
                RedTeamingServlet.handle(exchange);
            } catch (Exception ex) {
                String err = "<h1>500 Internal Server Error</h1><pre>" + ex.getMessage() + "</pre>";
                byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
                if (!exchange.getResponseHeaders().containsKey("Content-Type")) {
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                }
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });

        System.out.printf("[RED TEAMING TOOL] Java HttpServer Container listening on http://0.0.0.0:%d%n", port);
        server.start();
    }
}
