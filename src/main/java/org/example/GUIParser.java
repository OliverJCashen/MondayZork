package org.example;

import java.util.Scanner;

public class GUIParser {
    private CommandWords commands;  // holds all valid command words

    public GUIParser() {
        commands = new CommandWords();
    }

    public Command getCommand(String inputLine) {
        String word1 = null;
        String word2 = null;

        Scanner tokenizer = new Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next();      // get first word
            if(tokenizer.hasNext()) {
                word2 = tokenizer.next();      // get second word
            }
        }

        tokenizer.close();

        // Ensure we are passing the valid command word object if it exists, or null
        if(commands.isCommand(word1)) {
            return new Command(word1, word2);
        } else {
            return new Command(null, word2); 
        }
    }

    public String showCommands() {
        return "go, help, look, take, inv, sleep, restart";
    }
}
