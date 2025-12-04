package org.example;

public class Item {
    private String description;
    private String name;
    private String location;
    private boolean isVisible;

    public Item(String name, String description) {
        this.name = name;
        this.isVisible = true;
    }

    public String getDescription() {
        return description;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}
