package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TitleScreen {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TitleScreen::createAndShowGUI);
    }

    private static void createAndShowGUI() {

        // Main Title Screen Frame
        JFrame frame = new JFrame("Monday");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 450);
        frame.setLocationRelativeTo(null); // Center window to screen
        frame.setLayout(new BorderLayout());

        // Title Label
        JLabel titleLabel = new JLabel("MONDAY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(16, 10, 12, 10));
        frame.add(titleLabel, BorderLayout.NORTH);

        // Endings Panel
        JPanel endingsPanel = new JPanel();
        endingsPanel.setLayout(new BoxLayout(endingsPanel, BoxLayout.Y_AXIS));
        endingsPanel.setBorder(BorderFactory.createTitledBorder("Endings"));

        // Load Achieved Endings
        ProgressHandler progressHandler = new ProgressHandler();
        List<String> achieved = progressHandler.getEndings();
        Set<String> achievedSet = new HashSet<>(achieved == null ? new ArrayList<>() : achieved);

        // Show entries for any achieved endings on the title screen
        boolean anyShown = false;

        // Ending Text Logic

        boolean hasBed = achievedSet.contains("Give Up Ending");
        boolean hasWoods = achievedSet.contains("Free Will Ending");
        boolean hasOffice = achievedSet.contains("Killer Ending");
        boolean hasCubicle = achievedSet.contains("Sisyphus Ending");
        boolean hasRoulette = achievedSet.contains("Addiction Ending");

        if (hasBed && hasWoods && hasOffice && hasCubicle && hasRoulette) {
            JLabel allLabel = new JLabel("All Endings Achieved!");
            allLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            allLabel.setForeground(new Color(0, 128, 0)); // dark green
            allLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            endingsPanel.add(allLabel);
            anyShown = true;
        }
        if (achievedSet.contains("Give Up Ending")) {
            JLabel bedLabel = new JLabel("Give Up Ending");
            bedLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            bedLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            endingsPanel.add(bedLabel);
            anyShown = true;
        }
        if (achievedSet.contains("Free Will Ending")) {
            JLabel woodsLabel = new JLabel("Free Will Ending");
            woodsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            woodsLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            endingsPanel.add(woodsLabel);
            anyShown = true;}

        if (achievedSet.contains("Killer Ending")) {
            JLabel officeLabel = new JLabel("Killer Ending");
            officeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            officeLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            endingsPanel.add(officeLabel);
            anyShown = true;
        }
        if (achievedSet.contains("Sisyphus Ending")) {
            JLabel cubicleLabel = new JLabel("Sisyphus Ending");
            cubicleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            cubicleLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            endingsPanel.add(cubicleLabel);
            anyShown = true;
        }
        if (achievedSet.contains("Addiction Ending")) {
            JLabel rouletteLabel = new JLabel("Addiction Ending");
            rouletteLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            rouletteLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            endingsPanel.add(rouletteLabel);
            anyShown = true;
        }

        if (!anyShown) {
            // keep some vertical space so the area is visible even when empty
            endingsPanel.add(Box.createVerticalStrut(16));
        }

        frame.add(endingsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        // Start Button
        JButton playButton = new JButton("Start");
        playButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        playButton.setPreferredSize(new Dimension(150, 50));

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                createGameWindow();
            }
        });

        buttonPanel.add(playButton);

        // Clear Endings Button

        JButton clearEndingsButton = new JButton("Clear Endings");
        clearEndingsButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        clearEndingsButton.setPreferredSize(new Dimension(150, 50));
        clearEndingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int res = JOptionPane.showConfirmDialog(
                        frame,
                        "Clear all achieved endings? This cannot be undone.",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION
                );
                if (res == JOptionPane.YES_OPTION) {
                    // Reset progress.json by saving an empty progress json
                    ProgressHandler ph = new ProgressHandler();
                    ph.save(new ProgressData());

                    // Refresh the endings panel
                    endingsPanel.removeAll();
                    endingsPanel.add(Box.createVerticalStrut(16));
                    endingsPanel.revalidate();
                    endingsPanel.repaint();

                    JOptionPane.showMessageDialog(frame, "Endings cleared.");
                }
            }
        });
        buttonPanel.add(clearEndingsButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static void createGameWindow() {
        JFrame gameFrame = new JFrame("Monday Zork - Gameplay");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(600, 500);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setLayout(new BorderLayout());

        // Output Area
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        gameFrame.add(scrollPane, BorderLayout.CENTER);

        // Right Side Panel for Item Buttons
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        sidePanel.setPreferredSize(new Dimension(150, 0));
        gameFrame.add(sidePanel, BorderLayout.EAST);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton sendButton = new JButton("Send");
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        gameFrame.add(inputPanel, BorderLayout.SOUTH);

        // Prepare callback to return to title screen after a run ends
        Runnable backToTitle = new Runnable() {
            @Override
            public void run() {
                // Ensure UI changes happen on EDT
                SwingUtilities.invokeLater(() -> {
                    gameFrame.dispose();
                    createAndShowGUI();
                });
            }
        };

        // Show Game GUI
        GUIGame game = new GUIGame(outputArea, sidePanel, backToTitle);
        gameFrame.setVisible(true); // Show frame before game starts

        // Start game logic
        SwingUtilities.invokeLater(() -> game.play());

        ActionListener inputAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String commandText = inputField.getText();
                if (!commandText.trim().isEmpty()) {
                    outputArea.setText("");
                    game.processCommand(commandText);
                    inputField.setText("");
                }
                inputField.requestFocusInWindow();
            }
        };

        inputField.addActionListener(inputAction);
        sendButton.addActionListener(inputAction);
        inputField.requestFocusInWindow();
    }
}