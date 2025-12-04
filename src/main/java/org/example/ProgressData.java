package org.example;

import java.util.ArrayList;
import java.util.List;

public class ProgressData {
    private List<String> endingsAchieved;

    public ProgressData() {
        this.endingsAchieved = new ArrayList<>();
    }

    public List<String> getEndingsAchieved() {
        return endingsAchieved;
    }

    public void setEndingsAchieved(List<String> endingsAchieved) {
        this.endingsAchieved = endingsAchieved == null ? new ArrayList<>() : new ArrayList<>(endingsAchieved);
    }
}
