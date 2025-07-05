package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;

public class DatabaseAccess {
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private final String DBNAME = "shopping";
    private Connection connection;

    public JsonArray getItems() throws SQLException {
        String query = "SELECT * FROM items";
        if (!connect()) {
            return null;
        }
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        JsonArray resultJson = convertToJson(resultSet);
        connection.close();
        return resultJson;
    }

    public int addItem(Item item) throws SQLException {
        String query = "INSERT INTO items (itemName, itemQuantity) VALUES (?, ?)";
        if (!connect()) {
            return -1;
        }

        PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, item.getItemName());
        statement.setString(2, item.getItemQuantity());

        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            return -1;
        }

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        } else {
            return -1;
        }
    }

    public boolean deleteItem(int itemId) throws SQLException {
        String query = "DELETE FROM items WHERE id = ?";

        if(!connect()) {
            return false;
        }

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, itemId);
        return statement.executeUpdate() > 0;
    }

    public boolean deleteAllItems() throws SQLException {
        String query = "DELETE FROM items";

        if(!connect()) {
            return false;
        }

        Statement statement = connection.createStatement();
        return statement.executeUpdate(query) > 0;
    }

    public Item updateItem(Item item) throws SQLException {
        String queryUpdate = "UPDATE items SET itemName = ?, itemQuantity = ? WHERE id = ?";

        if(!connect()) {
            return null;
        }

        PreparedStatement statement = connection.prepareStatement(queryUpdate);
        statement.setString(1, item.getItemName());
        statement.setString(2, item.getItemQuantity());
        statement.setInt(3, item.getId());

        int rowsUpdated = statement.executeUpdate();

        if (rowsUpdated > 0) {
            String querySelect = "SELECT id, itemName, itemQuantity FROM items WHERE id = ?";
            PreparedStatement selectStatement = connection.prepareStatement(querySelect);
            selectStatement.setInt(1, item.getId());

            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                Item updatedItem = new Item();
                updatedItem.setId(resultSet.getInt("id"));
                updatedItem.setItemName(resultSet.getString("itemName"));
                updatedItem.setItemQuantity(resultSet.getString("itemQuantity"));

                return updatedItem;
            }
        }

        return null;
    }

    private boolean connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DBNAME, USERNAME, PASSWORD);
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private JsonArray convertToJson(ResultSet resultSet) throws SQLException {
        JsonArray jsonArray = new JsonArray();
        ResultSetMetaData metaData = resultSet.getMetaData();

        while (resultSet.next()) {
            JsonObject item = new JsonObject();

            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                item.addProperty(columnName, columnValue.toString());
            }

            jsonArray.add(item);
        }

        return jsonArray;
    }
}
