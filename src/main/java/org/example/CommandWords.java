package org.example;

import java.util.HashMap;
import java.util.Map;

public class CommandWords {
    private Map<String, String> validCommands;

    public CommandWords() {
        validCommands = new HashMap<>();
        validCommands.put("go", "Move to another room");
        validCommands.put("help", "Show help");
        validCommands.put("look", "Look around");
        validCommands.put("eat", "Eat something");
        validCommands.put("open", "Open something");
        validCommands.put("take", "Take something");
        validCommands.put("interact", "Interact with something");
        validCommands.put("inv", "Check inventory");
        validCommands.put("restart", "Start a new run after an ending");
    }

    public boolean isCommand(String commandWord) {
        return validCommands.containsKey(commandWord);
    }

    public void showAll() {
        System.out.print("COMMANDS:\n");
        for (String command : validCommands.keySet()) {
            System.out.println("- " + command);
        }
        System.out.println();
    }
}
