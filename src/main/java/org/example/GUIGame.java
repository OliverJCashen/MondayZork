package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GUIGame {
    private GUIParser parser;
    private GameCharacter player;
    private JTextArea gameOutput;
    private JPanel actionPanel;
    private JLabel locationLabel;
    private enum PromptState { NONE, NAME, SAFE_PIN, PEDESTAL }
    private PromptState promptState = PromptState.NONE;
    private boolean gameEnded = false;
    private String lastEndingName = null;
    private final SaveHandler saveHandler = new SaveHandler();
    private final ProgressHandler progressHandler = new ProgressHandler();
    private final Runnable onRestartToTitle;
    private boolean bossTalkedTo = false;

    Room kitchen, bedroom, hallway, garden, road, office, casino, woods;

    public GUIGame(JTextArea inGameOutput, JPanel inActionPanel, Runnable inOnRestartToTitle) {
        gameOutput = inGameOutput;
        actionPanel = inActionPanel;
        onRestartToTitle = inOnRestartToTitle;

        parser = new GUIParser();
        player = new GameCharacter("player");

        locationLabel = new JLabel("Location: -");
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    }

    // Prevents writing out long method name
    public void print(String message) {
        GUINarrationHandler.printByCharacter(gameOutput, message);
    }

    // Helper Functions
    public void updateRoomButtons() { updateRoomButtonsInternal(); }
    public GameCharacter getPlayer() { return player; }
    public void addItemToCurrentRoom(Item item) { player.getCurrentRoom().addRoomItems(item); }
    public void removeItemFromCurrentRoomByName(String name) {
        if (name == null) return;
        ArrayList<Item> items = player.getCurrentRoom().getRoomItems();
        if (items == null) return;
        Item toRemove = null;
        for (Item i : items) {
            if (i.getName().equalsIgnoreCase(name)) { toRemove = i; break; }
        }
        if (toRemove != null) {
            player.getCurrentRoom().removeRoomItem(toRemove);
        }
    }

    private void onNameChosen(String playerName) {
        player.setName(playerName);
        print("Well... Of course you are!\nWho else would you be?\nI, after all, being the omniscience narrator that I am already knew that!");
        gameOutput.setText("");
        createRooms();
        print(player.getCurrentRoom().getFirstTimeEntryText());
        updateRoomButtonsInternal();
    }

    private void endGameFromPrompt(String inEndingName, String inEndingText) {
        endGame(inEndingName, inEndingText); }

    private void endGame(String endingName, String endingText) {
        gameEnded = true;
        bossTalkedTo = false;
        lastEndingName = endingName;

        progressHandler.addEndingIfNew(endingName);

        saveHandler.clear();
        gameOutput.setText("");
        print(endingText);

        print(String.format("\nEnding recorded: %s", endingName));
        print("\nRun has ended. Type 'restart' or press the Restart button to return to the title screen.");

        // Replace action panel with only a Restart button
        updateRoomButtons();
    }

    private void restartGame() {
        gameEnded = false;
        lastEndingName = null;
        bossTalkedTo = false;
        // Reset player and parser; ask for name again. Rooms will be created after we get the name.
        parser = new GUIParser();
        player = new GameCharacter("player");
        gameOutput.setText("");
        startNamePrompt();
        // Clear action buttons until rooms are created
        actionPanel.removeAll();
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private void startNamePrompt() {
        promptState = PromptState.NAME;
        print("Uhm, who are you again?\n(Enter name below)");
    }

    private void startSafePinPrompt() {
        promptState = PromptState.SAFE_PIN;
        String safeText = String.format("""
                %s chooses to keep their keys in a safe, for some reason.
                %s was told to do this long ago by a man claiming to be a wizard.
                Personally, I would never trust a wizard, but %s did. However,
                %s did also forget the pin to unlock this safe.
                Perhaps they left themselves a reminder somewhere...?
                Enter the PIN below:""",
                player.getName(), player.getName(), player.getName(), player.getName());

        print(safeText);
    }

    private void startPedestalPrompt() {
        promptState = PromptState.PEDESTAL;
        String pedestalText = ("""
                Only true contrations will know the answer to this riddle:
                What is the sum of, for n from one to seven, of the integral from zero to one of six times n times x to the (n minus one) dx?
                And remember, only stupid, annoying, defiant people will ever know the answer.
                (Hint: The universe holds the answer.)""");

        print(pedestalText);
    }

    private boolean handleActivePromptInput(String input) {
        String text = input == null ? "" : input.trim();
        switch (promptState) {
            case NAME: {
                if (text.isEmpty()) {
                    print("I'm quite sure that you do actually have a name.\n(Enter name below)");
                    return false;
                }
                String capitalisedName = text.substring(0, 1).toUpperCase() + (text.length() > 1 ? text.substring(1) : "");
                onNameChosen(capitalisedName);
                return true;
            }
            case SAFE_PIN: {
                if ("4829".equals(text)) {

                    String rightPinText = String.format("""
                            %s entered the PIN correctly, to find their keys inside.
                            %s will never ever reconsider the concept of keeping their keys in a safe.""",
                            player.getName(), player.getName());

                    print(rightPinText);

                    removeItemFromCurrentRoomByName("safe");
                    addItemToCurrentRoom(new TakeableItem("Keys", "Player's house keys."));
                    updateRoomButtons();
                } else {
                    String wrongPinText = String.format("""
                            %s gave it the old college try and guess the PIN.
                            This was obviously wrong, and %s should feel stupid for trying.""",
                            player.getName(), player.getName());
                    print(wrongPinText);
                }
                return true;
            }
            case PEDESTAL: {
                if ("42".equals(text)) {
                    String endingText = String.format("""
                        In pure defiance of everything I stand for, %s says the right answer.
                        Somehow, you have defied both me, and your destiny.
                        You should be at work, %s. But instead you're here. In the woods.
                        How will you even get home?
                        Was it really worth it, %s?""",
                            player.getName(), player.getName(), player.getName());

                    endGameFromPrompt("Free Will Ending", endingText);
                    return true;
                } else {
                    print("The pedestal remains silent. That doesn't seem to be it.\nAfter all, you probably aren't smart enough for the answer");
                    return true;
                }
            }
            case NONE:
            default:
                return true;
        }
    }

    private void createRooms() {

        String bedroomDescription, hallDescription, kitchenDescription, roadDescription, officeDescription, casinoDescription, woodsDescription, gardenDescription;
        String bedroomFirstTimeText, hallFirstTimeText, kitchenFirstTimeText, roadFirstTimeText, officeFirstTimeText, casinoFirstTimeText, woodsFirstTimeText, gardenFirstTimeText;

        // DESCRIPTIONS

        // Bedroom => Hall, Kitchen

        bedroomFirstTimeText = (String.format("""
                *BEEP* *BEEP*
                %s was awoken by the sound of their least favourite thing on the planet.
                Rising out of bed like a poor man's Frankenstein, %s stood up, turned off their alarm clock, and began to get ready for work.
                %s decided to go the kitchen to get their keys.""",
                player.getName(), player.getName(), player.getName()));

        bedroomDescription = (String.format("""
                %s looked around their favourite bedroom.
                It was their favourite bedroom on the basis that it was their only bedroom, but that didn't make them like it any less.
                With it's gorgeous beige walls, art-deco white-washed door and breathtakingly dull view out of the window,
                %s never felt more at home than in this room.
                As a matter of fact, %s often felt an urge to never leave their picturesque twin-sized beed.
                However, as is their duty, %s decided to go into the hall to go to work.""",
                player.getName(), player.getName(), player.getName(), player.getName()));

        // Hall => Kitchen, Garden

        hallFirstTimeText = (String.format("""
                %s found themselves in what is potentially the worst room in the house, the hall.
                Serving the sole purpose of connecting the ever-slumberous bedroon and oh-so-delicious kitchen,
                this hall had absolutely nothing to give %s.""",
                player.getName(), player.getName()));

        hallDescription = (String.format("""
                Despite the horribleness of this hall, it did have one redeeming quality.
                It was the home of Lamp, a house-warming gift given to %s by their boss.
                %s hated their boss, but they did love Lamp.
                Sometimes when %s looked at Lamp, they could see their boss in the bulb,
                frustrating %s dearly.""",
                player.getName(), player.getName(), player.getName(), player.getName()));

        // Kitchen => Hall, Road

        kitchenFirstTimeText = (String.format("""
                %s entered the kitchen.
                What an incredible room, %s thought. Unlike the incredibly useless hall,
                the kitchen provided sustenance and comfort in times of need. Although the true possibilities
                of this room are endless, all that %s really needed right now is to get their keys to leave the house.""",
                player.getName(), player.getName(), player.getName()));

        kitchenDescription = (String.format("""
                %s admired this oh-so most beautiful room.
                Standing back, they admired the wood veneer counter top, the frivolous use of a chair,
                and the incredibly vintage television set. %s sometimes imagined themselves spending most of their
                time in a different kitchen, potentially even one with the capacity to prepare food.
                This sickened %s greatly.
                """,
                player.getName(), player.getName(), player.getName()));

        // Road => Casino, Office

        roadFirstTimeText = String.format("""
                %s left the house, and found themselves on the open road.
                Everyday on the way to work, %s passed by the local casino.
                Obviously, wherever they lived seemed to be the Las Vegas of Ireland.
                They never had any intention of going. Casinos are for losers.
                Real hard workers put all their money into buying stock in the company they work at,
                and then work so hard they raise the stock price single-handedly.
                """,
                player.getName(), player.getName());

        roadDescription = String.format("""
                There are quite literally only two buildings on this road, the Casino,
                and the office that %s worked at. They call it Casino n' Office Rd,
                named after Mr. Casino N. Office, who funded the road.
                %s has never gone to Casino, despite it's ever so enticing addiction hotline number everywhere.
                That didn't change today, where %s still just wants to go to work.""",
                player.getName(), player.getName(), player.getName());

        // Office

        officeFirstTimeText = String.format("""
                %s found themselves in the office.
                They hated it here.
                Work Inc. was potentially the most evil company in the world,
                but it's the only job %s could get with their qualifications.
                %s saw the boss they hated walking about, singing their soul with hatred.
                Regardless, %s just had to make it to their cubicle to start the work for the week.""",
                player.getName(), player.getName(), player.getName(), player.getName());

        officeDescription = String.format("""
                Work Inc. made %s miserable.
                They are known to cut down trees in the Amazon, record it, and
                send the footage to billionaires for them to laugh at.
                %s felt that their boss was a huge part of this problem.
                But Monday is never the day for moral deliberation, and all %s wanted
                to do was do their work and go home.""",
                player.getName(), player.getName(), player.getName());

        // Casino

        casinoFirstTimeText = String.format("""
                %s pivoted from their obvious goal with the sole purpose of defying me, The Narrator.
                I personally find this quiet rude.
                Despite this, %s walked into the casino, laying their eyes upon it for the first time.
                It was a nearly empty concrete room, with a crowd of people huddled around a single roulette table.""",
                player.getName(), player.getName());

        casinoDescription = String.format("""
                %s stared at the crowd of people huddled around the roulette table.
                Feeling somewhat intimidated, %s turned around to leave.
                However, the door is locked.
                It's almost as if someone locked it to prove a point about being a contrarian.
                Who could have done that?""",
                player.getName(), player.getName());

        // Garden => Hall, Woods

        gardenFirstTimeText = (String.format("""
                Despite the door being a perfectly good exit to the house, %s decided to jump out of the window instead.
                The seductive smell of sweet green grass must have enticed them greatly.
                However, the dastardly grass made %s leave the house without their keys.
                Luckily, the door the hall was right there for %s to open.""",
                player.getName(),player.getName(),player.getName()));

        gardenDescription = (String.format("""
                %s got this house for cheap due to the fact that the front door faced directly into the woods.
                %s often heard terrifying sounds of freedom coming from the woods, and never had an urge to explore.
                That lack of urge persisted even to today, where %s still does not want to go into the woods, and would rather get their keys and go to work.""",
                player.getName(), player.getName(), player.getName()));

        // Woods

        woodsFirstTimeText = String.format("""
                %s, why did you go into the woods?
                I specifically said you didn't want to go into the woods.
                I'm not trying to impose on your life but I really think you should have gone to work.
                Are you just defying me for the sake of it? Is that what this is?
                Because I don't deserve it %s, not one bit.""",
                player.getName(), player.getName());

        woodsDescription = String.format("""
                I don't really know what you're looking for %s, first you deny me and then
                you ask me to describe the woods? You're being very fickle. It's frustrating.
                Ugh, whatever. If you want 'adventure' then I'll give you adventure.
                Ahem. Alright.
                In these dark and mysterious woods, %s can make out a stone pedestal, 
                with what seems to be some foul riddle upon it...""",
                player.getName(), player.getName());

        // INITIALISING
        this.bedroom = new Room(bedroomDescription, bedroomFirstTimeText);
        this.hallway = new Room(hallDescription, hallFirstTimeText);
        this.kitchen = new Room(kitchenDescription, kitchenFirstTimeText);
        this.road = new Room(roadDescription, roadFirstTimeText);
        this.office = new Room(officeDescription, officeFirstTimeText);
        this.casino = new Room(casinoDescription, casinoFirstTimeText);
        this.garden = new Room(gardenDescription, gardenFirstTimeText);
        this.woods = new Room(woodsDescription, woodsFirstTimeText);

        // ITEMS
        this.hallway.addRoomItems(new TakeableItem("Lamp", "A house-warming gift from the boss."));
        this.bedroom.addRoomItems(new InteractableItem("Nightstand", "A wooden nightstand with a safe."));
        this.bedroom.addRoomItems(new InteractableItem("Bed", "Your picturesque twin-sized bed."));
        this.kitchen.addRoomItems(new InteractableItem("Safe", "A locked safe."));
        this.office.addRoomItems(new InteractableItem("Boss", "Your overbearing boss, glaring over the floor."));
        this.office.addRoomItems(new InteractableItem("Cubicle", "Your drab gray cubicle. The embodiment of Monday."));
        this.woods.addRoomItems(new InteractableItem("Pedestal", "A stone pedestal etched with a riddle."));
        this.casino.addRoomItems(new InteractableItem("Roulette", "A gleaming roulette table, numbers winking in red and black."));

        // EXITS
        this.bedroom.setExit("Door", this.hallway);
        this.bedroom.setExit("Window", this.garden);

        this.hallway.setExit("Bedroom", this.bedroom);
        this.hallway.setExit("Kitchen", this.kitchen);

        this.kitchen.setExit("Hall", this.hallway);
        this.kitchen.setExit("Door", this.road);

        this.road.setExit("Casino", this.casino);
        this.road.setExit("Office", this.office);

        this.garden.setExit("Door", this.hallway);
        this.garden.setExit("Woods", this.woods);

        player.setCurrentRoom(this.bedroom);
    }

    public void play() {
        // If a save exists and is not blank, attempt to load

        Optional<SaveData> maybeSave = saveHandler.load();
        if (maybeSave.isPresent()) {
            SaveData data = maybeSave.get();

            // restore player name first

            if (data.getPlayerName() != null && !data.getPlayerName().isEmpty()) {
                player.setName(data.getPlayerName());
            }

            createRooms();

            // Prevents first-time text after reload
            List<String> visited = data.getVisitedRooms();
            if (visited != null) {
                for (String key : visited) {
                    Room r = roomFromKey(key);
                    if (r != null) {
                        r.setFirstTimeEntry(false);
                    }
                }
            }


            Room loadedRoom = roomFromKey(data.getRoomKey());
            if (loadedRoom == null) {
                loadedRoom = bedroom; // fallback
            }
            player.setCurrentRoom(loadedRoom);
            restoreInventory(data.getInventoryNames());
            gameOutput.setText("");
            print(player.getCurrentRoom().getDescription());
            updateRoomButtons();
            return;
        }

        startNamePrompt();

    }

    public void processCommand(String commandText) {
        if (gameEnded) {
            String trimmed = commandText == null ? "" : commandText.trim().toLowerCase();
            if ("restart".equals(trimmed)) {
                if (onRestartToTitle != null) {
                    onRestartToTitle.run();
                }
            } else {
                print("Run Complete! Type 'restart' to play again!");
            }
            return;
        }

        if (promptState != PromptState.NONE) {
            boolean finished = handleActivePromptInput(commandText);
            if (finished) {
                promptState = PromptState.NONE;
            }
            return;
        }

        gameOutput.setText("");

        Command command = parser.getCommand(commandText);
        
        String commandWord = command.getCommandWord();

        if (commandWord == null) {
            print(String.format("%s tried to do something that is completely impossible.", player.getName()));
            return;
        }

        switch (commandWord) {
            case "go":
                goRoom(command);
                break;
            case "look":
                lookRoom(command);
                break;
            case "take":
                takeItem(command);
                break;
            case "interact":
                interactItem(command);
                break;
            case "inv":
                viewInventory(command);
                break;
            case "restart":
                restartGame();
                break;
            case "help":
                print("Your command words are: " + parser.showCommands());
                break;
            default:
                print("I don't know what you mean...");
                break;
        }

        gameOutput.setText(""); // Clear the main output area
    }

    private void goRoom(Command command) {
        if (!command.hasSecondWord()) {
            print(String.format("%s had an indescribable urge to go somewhere, but couldn't parse where.\nTruly, a human experience.", player.getName()));
            return;
        }

        String direction = command.getSecondWord();
        Room nextRoom = player.getCurrentRoom().getExit(direction);

        if (nextRoom == null) {
            print(String.format("%s had an indescribable urge to go somewhere that did not exist.\nSuch is the life of a fiction writer.", player.getName()));
        } else {
            if (player.getCurrentRoom() == kitchen && nextRoom == road) {
                boolean hasKeys = false;
                for (Item item : player.getInventory()) {
                    if (item.getName().equalsIgnoreCase("Keys")) {
                        hasKeys = true;
                        break;
                    }
                }

                if (!hasKeys) {
                    print(String.format("%s has somehow already forgotten that they need their keys to open the door.", player.getName()));
                    return;
                }
            }

            player.setCurrentRoom(nextRoom);


            print(player.getCurrentRoom().getFirstTimeEntryText());
            
            // Update buttons whenever we move to a new room
            updateRoomButtons();
        }
    }

    private void updateRoomButtonsInternal() {
        
        actionPanel.removeAll();

        // If the game ended, show only Restart
        if (gameEnded) {
            JButton restartBtn = new JButton("Restart");
            restartBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            restartBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (onRestartToTitle != null) {
                        onRestartToTitle.run();
                    }
                }
            });
            actionPanel.add(restartBtn);
            actionPanel.revalidate();
            actionPanel.repaint();
            return;
        }

        updateLocationLabel();
        actionPanel.add(locationLabel);
        actionPanel.add(Box.createVerticalStrut(8));

        // LOOK AROUND BUTTON

        JButton lookButton = new JButton("Look around");
        lookButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        lookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processCommand("look");
                actionPanel.revalidate();
                actionPanel.repaint();
            }
        });

        actionPanel.add(lookButton);
        actionPanel.add(Box.createVerticalStrut(8));

        // INVENTORY BUTTON

        JButton inventoryButton = new JButton("Inventory");
        inventoryButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        inventoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processCommand("inv");
                actionPanel.revalidate();
                actionPanel.repaint();
            }
        });

        actionPanel.add(inventoryButton);
        actionPanel.add(Box.createVerticalStrut(8));

        // EXIT BUTTONS

        for (String exitName : player.getCurrentRoom().getExitNames()) {
            JButton exitButton = new JButton(exitName);
            exitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    processCommand("go " + exitName);
                    updateRoomButtons();
                    actionPanel.revalidate();
                    actionPanel.repaint();
                }
            });
            actionPanel.add(exitButton);
        }

        // ITEMS BUTTONS

        ArrayList<Item> items = player.getCurrentRoom().getRoomItems();
        if (items != null && !items.isEmpty()) {
            for (Item item : items) {
                if (item.isVisible() && item instanceof Interactable && !(item instanceof Takeable)) {

                    // Boss Handling, Prevents Boss Showing Again After First Interaction

                    if (player.getCurrentRoom() == office && bossTalkedTo &&
                            item.getName() != null && item.getName().equalsIgnoreCase("Boss")) {
                        continue;
                    }

                    JButton interactButton = new JButton(item.getName());
                    interactButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                    interactButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            processCommand("interact " + item.getName());
                            updateRoomButtons();
                            actionPanel.revalidate();
                            actionPanel.repaint();
                        }
                    });
                    actionPanel.add(interactButton);
                }

                if (item.isVisible() && item instanceof Takeable) {
                    JButton takeButton = new JButton(item.getName());
                    takeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                    takeButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            processCommand("take " + item.getName());
                            updateRoomButtons();
                            actionPanel.revalidate();
                            actionPanel.repaint();
                        }
                    });
                    actionPanel.add(takeButton);
                }
            }
        }

        // KILL YOUR BOSS WITH A LAMP YEAHHHH

        if (player.getCurrentRoom() == office && bossTalkedTo) {
            boolean hasLamp = false;
            for (Item invItem : player.getInventory()) {
                if (invItem.getName() != null && invItem.getName().equalsIgnoreCase("Lamp")) {
                    hasLamp = true;
                    break;
                }
            }
            if (hasLamp) {
                JButton useLampButton = new JButton("Use Lamp");
                useLampButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                useLampButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String endingText = String.format("""
                            %s raised Lamp above their head, before smashing it down onto their bosses head.
                            He dies.
                            There's not a single gasp from a single other employee.
                            That feels quite hollow, doesn't it %s?""",
                        player.getName(), player.getName());
                        endGame("Killer Ending", endingText);
                    }
                });
                actionPanel.add(useLampButton);
            }
        }

        // Keeps Save Button at Bottom of Action Panel
        actionPanel.add(Box.createVerticalGlue());

        // SAVE BUTTON

        JButton saveButton = new JButton("Save Game");
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SaveData data = buildSaveData();
                saveHandler.save(data);
                print("Game saved.");
            }
        });
        actionPanel.add(saveButton);

        // REFRESH PANEL

        actionPanel.revalidate();
        actionPanel.repaint();
    }

    // LOCATION LABEL

    private void updateLocationLabel() {
        String name = displayNameFromRoom(player.getCurrentRoom());
        locationLabel.setText("Location: " + name);
    }
    

    private SaveData buildSaveData() {
        String roomKey = keyFromRoom(player.getCurrentRoom());
        ArrayList<String> inv = new ArrayList<>();
        for (Item i : player.getInventory()) {
            inv.add(i.getName());
        }

        // Saves which rooms have been visited for first-time text reasons

        ArrayList<String> visited = new ArrayList<>();
        for (Room r : allRooms()) {
            if (r != null && !r.isFirstTimeEntry()) {
                visited.add(keyFromRoom(r));
            }
        }
        return new SaveData(player.getName(), roomKey, inv, visited);
    }

    // Makes above method less cluttered

    private Room[] allRooms() {
        return new Room[]{bedroom, hallway, kitchen, road, office, casino, woods, garden};
    }


    private void restoreInventory(List<String> names) {
        if (names == null || names.isEmpty()) return;
        for (String n : names) {
            Item found = findAndRemoveItemFromRooms(n);
            if (found == null) {
                // default to takeable placeholder since only takeables can be in inventory
                found = new TakeableItem(n, "");
            }
            player.addInventory(found);
        }
    }

    private Item findAndRemoveItemFromRooms(String name) {
        Room[] rooms = allRooms();
        for (Room r : rooms) {
            if (r == null) continue;
            ArrayList<Item> items = r.getRoomItems();
            if (items == null) continue;
            for (Item it : new ArrayList<>(items)) {
                if (it.getName().equalsIgnoreCase(name)) {
                    r.removeRoomItem(it);
                    return it;
                }
            }
        }
        return null;
    }



    // Map a room to a stable key for saving
    private String keyFromRoom(Room room) {
        if (room == null) return "";
        if (room == bedroom) return "bedroom";
        if (room == hallway) return "hallway";
        if (room == kitchen) return "kitchen";
        if (room == road) return "road";
        if (room == office) return "office";
        if (room == casino) return "casino";
        if (room == woods) return "woods";
        if (room == garden) return "garden";
        return "bedroom";
    }

    // Nicer Display Names

    private String displayNameFromRoom(Room room) {
        String key = keyFromRoom(room);
        switch (key) {
            case "bedroom": return "Bedroom";
            case "hallway": return "Hallway";
            case "kitchen": return "Kitchen";
            case "road": return "Road";
            case "office": return "Office";
            case "casino": return "Casino";
            case "woods": return "Woods";
            case "garden": return "Garden";
            default: return "-";
        }
    }

    // Map key back to room after rooms are created

    private Room roomFromKey(String key) {
        if (key == null) return null;
        switch (key) {
            case "bedroom": return bedroom;
            case "hallway": return hallway;
            case "kitchen": return kitchen;
            case "road": return road;
            case "office": return office;
            case "casino": return casino;
            case "woods": return woods;
            case "garden": return garden;
            default: return null;
        }
    }
    

    private void lookRoom(Command command) {
        print(player.getCurrentRoom().getDescription());
        if(!player.getCurrentRoom().getRoomItems().isEmpty()){
            print("Items:");
            for (Item iItem : player.getCurrentRoom().getRoomItems()) {
                print(String.format("- %s", iItem.getName()));
            }
        }
    }

    private void takeItem(Command command) {
        if (!command.hasSecondWord()) {
            print("Take what?");
            return;
        }

        String itemName = command.getSecondWord();
        Item found = null;
        for (Item i : player.getCurrentRoom().getRoomItems()) {
            if(i.getName().equalsIgnoreCase(itemName)) {
                    found = i;
                    break;
                }
            }
        
            if(found != null) {
                if (!(found instanceof Takeable)) {
                    print("You can't take that. Try 'interact " + found.getName().toLowerCase() + "'.");
                    return;
                }
                player.addInventory(found);
                player.getCurrentRoom().removeRoomItem(found);
                print(found.getName() + " taken!");
                updateRoomButtons(); // Update UI
            } else {
                print("No such item!");
            }
        }

        private void interactItem(Command command) {
            if (!command.hasSecondWord()) {
                print("Interact with what?");
                return;
            }
            String itemName = command.getSecondWord();

            // Nightstand Logic

            if (itemName.equalsIgnoreCase("nightstand") && player.getCurrentRoom() == bedroom) {
                Item nightstand = null;
                for (Item i : player.getCurrentRoom().getRoomItems()) {
                    if (i.getName().equalsIgnoreCase("nightstand")) {
                        nightstand = i;
                        break;
                    }
                }
                if (nightstand != null) {
                    // Check if player already has note
                    boolean hasNoteAlready = false;
                    for (Item invItem : player.getInventory()) {
                        if (invItem.getName().equalsIgnoreCase("note")) {
                            hasNoteAlready = true;
                            break;
                        }
                    }

                    if (hasNoteAlready) {
                        print(String.format("%s checks the nightstand again, only to find absolutely nothing.\nNext time for sure though.", player.getName()));
                    } else {
                        print(String.format("%s checks the nightstand again, finding a small note.\nIt reads: 'PIN: 4829'\nConvenient.", player.getName()));
                        print("It reads: 'PIN: 4829'");
                        player.addInventory(new TakeableItem("note", "A note with the code 4829."));
                    }
                    // DON'T remove nightstand in the bedroom
                    updateRoomButtons();
                    return;
                }
            }

            // Bed Logic for Bed Ending

            if (itemName.equalsIgnoreCase("bed") && player.getCurrentRoom() == bedroom) {
                String bedEndingText = String.format("""
                        %s decided that work wasn't worth it, and will never be worth it again.
                        After all, should all of us not just do what comes to mind first?
                        We are animals after all.
                        %s has simply given up.
                        %s drifts to sleep, slightly more nihilistic than when they woke up.""",
                        player.getName(), player.getName(), player.getName()
                );
                endGame("Give Up Ending", bedEndingText);
                return;
            }

            // Safe Logic

            if (itemName.equalsIgnoreCase("safe") && player.getCurrentRoom() == kitchen) {
                startSafePinPrompt();
                return;
            }

            // Pedestal Logic

            if (itemName.equalsIgnoreCase("pedestal") && player.getCurrentRoom() == woods) {
                startPedestalPrompt();
                return;
            }

            // Boss Logic

            if (itemName.equalsIgnoreCase("boss") && player.getCurrentRoom() == office) {
                if (!bossTalkedTo) {
                    boolean hasLamp = false;
                    for (Item invItem : player.getInventory()) {
                        if (invItem.getName() != null && invItem.getName().equalsIgnoreCase("Lamp")) {
                            hasLamp = true;
                            break;
                        }
                    }
                    print("You approach your boss.\n\"Back to work,\" he mutters without looking up.");
                    if (hasLamp) {
                        String weightLine = String.format("%s felt anger writhing in their arms, the feeling of the Lamp weighing heavy.\nPerhaps now is the time to use it.", player.getName());
                        print(weightLine);
                    }
                    bossTalkedTo = true; // enable follow-up action
                } else {
                    print("You try to get his attention again. He just gestures you back to your desk.");
                }
                return;
            }

            // Cubicle Logic

            if (itemName.equalsIgnoreCase("cubicle") && player.getCurrentRoom() == office) {
                String endingText = String.format("""
                        %s grumbiliy plopped themselves down at their cubicle, dissatisfied.
                        They began to contemplate, rethink, refactor.
                        Their work was so dull they had a lot of time for thinking, after all.
                        Maybe this isn't so bad, %s thought.
                        A philosopher once proposed that meaningless tasks can be given meaning by those who accomplish them.
                        We, as humans, have the choice to decided what has meaning to us.
                        %s looks at their computer screen and continues to type.
                        "Could be worse. I could be a gambler."
                        """,
                        player.getName(), player.getName(), player.getName());

                endGame("Sisyphus Ending", endingText);
                return;
            }

            // Casino Logic

            if (itemName.equalsIgnoreCase("roulette") && player.getCurrentRoom() == casino) {
                String endingText = String.format("""
                        %s sat down at the roulette table, hoping to waste some of their money.
                        The dealer see them, immediately brandishing his brass knuckles,
                        punching %s in the head. Killing them instantly.
                        Hey! This would make a great anti-addiction ad!
                        """,
                        player.getName(), player.getName());

                endGame("Addiction Ending", endingText);
                return;
            }

            print("Nothing interesting happens. How dissapointing.");
        }

 /*       private void placeItem(Command command) {
            if (!command.hasSecondWord()) {
                print("Place what?");
                return;
            }
            String itemName = command.getSecondWord();
            Item found = null;
            for (Item i : player.getInventory()) {
                if(i.getName().equalsIgnoreCase(itemName)) {
                    found = i;
                    break;
                }
            }

            if(found != null) {
                player.getCurrentRoom().addRoomItems(found);
                player.removeInventory(found);
                print(found.getName() + " put!");
                updateRoomButtons(); // Update UI
            } else {
                print("You don't have that item!");
            }
        } */ // Leftover from Week 1 Nominated Exercise

        private void viewInventory(Command command) {
            ArrayList<Item> inv = player.getInventory();
            if (inv.isEmpty()) {
                print(String.format("%s feels the immense pleasure of completely empty pockets", player.getName()));
            } else {
                print("Inventory:");
                for (Item i : inv) {
                    print("- " + i.getName());
                }
            }
        }
    }
