package org.example;

import java.util.ArrayList;
import java.util.List;

public class SaveData {
    private String playerName;
    private String roomKey;
    private List<String> inventoryNames;
    private List<String> visitedRooms;

    public SaveData() {}

    public SaveData(String inPlayerName, String inRoomKey, List<String> inInventoryNames, List<String> inVisitedRooms) {
        playerName = inPlayerName;
        roomKey = inRoomKey;
        inventoryNames = inInventoryNames == null ? new ArrayList<>() : new ArrayList<>(inInventoryNames);
        visitedRooms = inVisitedRooms == null ? new ArrayList<>() : new ArrayList<>(inVisitedRooms);
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    public List<String> getInventoryNames() {
        return inventoryNames;
    }

    public void setInventoryNames(List<String> inventoryNames) {
        this.inventoryNames = inventoryNames;
    }

    public List<String> getVisitedRooms() {
        return visitedRooms;
    }

    public void setVisitedRooms(List<String> visitedRooms) {
        this.visitedRooms = visitedRooms;
    }
}
