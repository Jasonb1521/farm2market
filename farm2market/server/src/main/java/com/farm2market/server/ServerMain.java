package com.farm2market.server;

import com.farm2market.server.db.DatabaseConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerMain {

    private final int port;
    private final int maxConnections;
    private final ExecutorService threadPool;
    private final AtomicBoolean running;
    private ServerSocket serverSocket;

    public ServerMain() {
        Properties props = loadProperties();
        this.port = Integer.parseInt(props.getProperty("server.port", "8888"));
        this.maxConnections = Integer.parseInt(props.getProperty("server.max.connections", "100"));
        int poolSize = Integer.parseInt(props.getProperty("server.thread.pool.size", "20"));
        this.threadPool = Executors.newFixedThreadPool(poolSize);
        this.running = new AtomicBoolean(false);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("server.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("[Server] Could not load server.properties: " + e.getMessage());
        }
        return props;
    }

    public void start() {
        start(false);
    }

    public void start(boolean loadSeed) {
        System.out.println("========================================");
        System.out.println("   Farm2Market Server Starting...       ");
        System.out.println("========================================");

        // Initialize database
        DatabaseConnection db = DatabaseConnection.getInstance();
        if (!db.testConnection()) {
            System.err.println("[Server] Failed to connect to database!");
            return;
        }
        db.initializeSchema();

        if (loadSeed) {
            System.out.println("[Server] Loading seed data...");
            db.loadSeedData();
        }

        // Start notification dispatcher
        NotificationDispatcher.getInstance().start();

        // Start server socket
        try {
            serverSocket = new ServerSocket(port);
            running.set(true);

            System.out.println("[Server] Listening on port " + port);
            System.out.println("[Server] Max connections: " + maxConnections);
            System.out.println("[Server] Ready to accept clients");
            System.out.println("========================================");

            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    System.out.println("[Server] New connection from: " + clientAddress);

                    ClientHandler handler = new ClientHandler(clientSocket);
                    threadPool.execute(handler);
                } catch (IOException e) {
                    if (running.get()) {
                        System.err.println("[Server] Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        System.out.println("[Server] Shutting down...");
        running.set(false);

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        threadPool.shutdown();
        NotificationDispatcher.getInstance().shutdown();
        DatabaseConnection.getInstance().close();

        System.out.println("[Server] Shutdown complete");
    }

    public boolean isRunning() {
        return running.get();
    }

    public static void main(String[] args) {
        ServerMain server = new ServerMain();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server.isRunning()) {
                server.stop();
            }
        }));

        // Check for seed data argument - this happens AFTER start initializes schema
        boolean loadSeed = false;
        for (String arg : args) {
            if ("--seed".equals(arg)) {
                loadSeed = true;
            }
        }

        server.start(loadSeed);
    }
}
