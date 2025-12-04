package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Room {
    private String description;
    private String firstTimeEntryText;
    private Map<String, Room> exits; // Map direction to neighboring Room
    private ArrayList<Item> roomItems;
    private boolean firstTimeEntry;

    public Room(String inDescription, String inFirstTimeEntryText) {
        description = inDescription;
        firstTimeEntryText = inFirstTimeEntryText;
        exits = new HashMap<>();
        roomItems = new ArrayList<>();
        firstTimeEntry = true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String inDescription) {
        description = inDescription;
    }

    public void setExit(String direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    public Room getExit(String direction) {
        return exits.get(direction);
    }

    public String getExitString() {
        StringBuilder sb = new StringBuilder();
        for (String direction : exits.keySet()) {
            sb.append(direction).append(" ");
        }
        return sb.toString().trim();
    }

    public Set<String> getExitNames() {
        return exits.keySet();
    }

    public void setFirstTimeEntryText(String inFirstTimeEntryText) {

        firstTimeEntryText = inFirstTimeEntryText;
    }

    public String getFirstTimeEntryText() {
        if (!firstTimeEntry) {
            return "";
        }

        firstTimeEntry = false;
        return firstTimeEntryText;
    }

    public boolean isFirstTimeEntry() {
        return firstTimeEntry;
    }

    public void setFirstTimeEntry(boolean firstTimeEntry) {
        this.firstTimeEntry = firstTimeEntry;
    }

    public ArrayList<Item> getRoomItems() {

        return roomItems;
    }

    public void addRoomItems(Item item) {
        roomItems.add(item);
    }

    public void removeRoomItem(Item item) {
        roomItems.remove(item);}
}