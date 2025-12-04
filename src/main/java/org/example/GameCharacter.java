package org.example;

import java.io.Serializable;
import java.util.ArrayList;

public class GameCharacter implements Serializable {
    private String name;
    private Room currentRoom;
    private ArrayList<Item> inventory;

    public GameCharacter(String name) {
        this.name = name;
        inventory = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String inName) {
        name = inName;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public ArrayList<Item> getInventory() {
        return inventory;
    }

    public void addInventory(Item item) {
        inventory.add(item);
    }

    public void removeInventory(Item item) {
        inventory.remove(item);
    }
}
