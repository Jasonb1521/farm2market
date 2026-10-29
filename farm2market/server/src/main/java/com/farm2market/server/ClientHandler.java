package com.farm2market.server;

import com.farm2market.core.model.User;
import com.farm2market.core.protocol.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestRouter router;
    private BufferedReader reader;
    private PrintWriter writer;
    private User currentUser;
    private String sessionToken;
    private volatile boolean connected;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.router = new RequestRouter(this);
        this.connected = true;
    }

    @Override
    public void run() {
        String clientAddress = socket.getInetAddress().getHostAddress();

        try {
            socket.setSoTimeout(60000); // 60 second read timeout
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("[Client:" + clientAddress + "] Handler started");

            String line;
            while (connected && (line = reader.readLine()) != null) {
                try {
                    Request request = ProtocolSerializer.deserializeRequest(line);
                    Response response = router.route(request);

                    String responseJson = ProtocolSerializer.serialize(response);
                    writer.print(responseJson);  // serialize() already adds newline
                    writer.flush();

                } catch (Exception e) {
                    System.err.println("[Client:" + clientAddress + "] Error processing request: " + e.getMessage());
                    Response errorResponse = Response.error(null, "PARSE_ERROR", "Invalid request format");
                    writer.print(ProtocolSerializer.serialize(errorResponse));
                    writer.flush();
                }
            }

        } catch (SocketException e) {
            if (connected) {
                System.out.println("[Client:" + clientAddress + "] Connection reset");
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("[Client:" + clientAddress + "] I/O error: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        if (!connected) return;

        connected = false;
        String clientAddress = socket.getInetAddress().getHostAddress();

        // Unregister from notification dispatcher
        if (currentUser != null) {
            NotificationDispatcher.getInstance().unregisterClient(currentUser.getUserId());
        }

        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Ignore
        }

        System.out.println("[Client:" + clientAddress + "] Disconnected");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && writer != null) {
            NotificationDispatcher.getInstance().registerClient(user.getUserId(), writer);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }

    public String getClientAddress() {
        return socket.getInetAddress().getHostAddress();
    }
}
