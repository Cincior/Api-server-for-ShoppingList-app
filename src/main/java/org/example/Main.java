package org.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final String ITEM_PATH = "/api/items";
    private static final int PORT = 777;
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext(ITEM_PATH, new ItemHandler());
            //server.createContext(authenticationPath, new AuthorizationHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}