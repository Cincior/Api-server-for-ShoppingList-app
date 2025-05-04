package org.example;

public class Item {
    private int id;
    private String itemName;
    private String itemQuantity;

    public Item() {};

    public Item(int id, String itemName, String itemQuantity) {
        this.id = id;
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
}
