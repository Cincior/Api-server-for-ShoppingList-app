package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            System.out.println(method);

            switch (method) {
                case "GET":
                    handleGetRequest(exchange);
                    break;
                case "POST":
                    handlePostRequest(exchange);
                    break;
                case "PUT":
                    handlePutRequest(exchange);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange);
                    break;
                case "OPTIONS":
                    handleOptionsRequest(exchange);
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            exchange.sendResponseHeaders(500, -1);
        }

    }

    private void handlePutRequest(HttpExchange exchange) throws SQLException, IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody());
        BufferedReader reader = new BufferedReader(inputStreamReader);

        String requestBody = reader.lines().collect(Collectors.joining());
        Item item = gson.fromJson(requestBody, Item.class);

        DatabaseAccess db = new DatabaseAccess();
        Item updatedItem = db.updateItem(item);
        if(updatedItem != null) {
            sendResponse(exchange, 200, gson.toJson(updatedItem));
        } else {
            sendResponse(exchange, 500, gson.toJson("Update faled"));
        }
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> queryParams = getQueryParams(exchange.getRequestURI().getQuery());

        String itemId = queryParams.get("id");
        DatabaseAccess db = new DatabaseAccess();

        if(itemId == null) {
            if(db.deleteAllItems()) {
                sendResponse(exchange, 200, gson.toJson("all deleted successfully"));
            } else {
                sendResponse(exchange, 500, gson.toJson("Unable to delete!"));
            }
        } else {
            if(db.deleteItem(Integer.parseInt(itemId))) {
                sendResponse(exchange, 200, gson.toJson("deleted successfully"));
            } else {
                sendResponse(exchange, 500, gson.toJson("Unable to delete!"));
            }
        }


    }

    private Map<String, String> getQueryParams(String query) {
        if (query == null) {
            return Map.of();
        }

        return Stream.of(query.split("&"))
                .map(p -> p.split("="))
                .collect(Collectors.toMap(pp -> pp[0], pp -> pp.length > 1 ? pp[1] : ""));
    }

    private void handlePostRequest(HttpExchange exchange) throws SQLException, IOException {
        System.out.println("W poscie");
        InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody());
        BufferedReader reader = new BufferedReader(inputStreamReader);

        String requestBody = reader.lines().collect(Collectors.joining());

        Item newItem = gson.fromJson(requestBody, Item.class);
        DatabaseAccess db = new DatabaseAccess();

        int newItemId = db.addItem(newItem);
        JsonObject response = new JsonObject();
        response.addProperty("id", newItemId);

        if (newItemId != -1) {
            sendResponse(exchange, 201, gson.toJson(response));
        } else {
            sendResponse(exchange, 500, gson.toJson("Failed to add item"));
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws SQLException, IOException {
        DatabaseAccess db = new DatabaseAccess();
        JsonArray items = db.getItems();
        if (items == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        sendResponse(exchange, 200, gson.toJson(items));
    }


    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:4200");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1); // No content
    }

    private void sendResponse(HttpExchange exchange, int code, String responseContent) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:4200");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(code, responseContent.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseContent.getBytes(StandardCharsets.UTF_8));
        os.close();
    }


}
