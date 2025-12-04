package org.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

public class GUINarrationHandler { // AI GENERATED CLASS

    private static final int DELAY_MS = 10; // Speed of typing
    private static final Queue<String> messageQueue = new LinkedList<>();
    private static boolean isPrinting = false;

    public static void printByCharacter(JTextArea outputArea, String message) {
        messageQueue.add(message);
        processQueue(outputArea);
    }

    private static void processQueue(JTextArea outputArea) {
        if (isPrinting || messageQueue.isEmpty()) {
            return;
        }

        isPrinting = true;
        String message = messageQueue.poll();

        if (outputArea.getText().length() > 0) {
            outputArea.append("\n");
        }

        Timer timer = new Timer(DELAY_MS, null);
        timer.addActionListener(new ActionListener() {
            private int index = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index < message.length()) {
                    outputArea.append(String.valueOf(message.charAt(index)));
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                    index++;
                } else {
                    timer.stop();
                    outputArea.append("\n"); // Add final newline
                    isPrinting = false;
                    
                    // Trigger the next message in the queue
                    processQueue(outputArea); 
                }
            }
        });
        timer.start();
    }
}
