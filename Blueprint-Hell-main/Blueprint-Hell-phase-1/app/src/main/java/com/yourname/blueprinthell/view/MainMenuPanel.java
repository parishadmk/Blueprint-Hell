package com.yourname.blueprinthell.view;

import com.yourname.blueprinthell.model.GameState; // Import GameState
import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    // --- UPDATED: The constructor now accepts the GamePanel ---
    public MainMenuPanel(CardLayout cardLayout, JPanel parentPanel, GamePanel gamePanel) {
        setLayout(new GridLayout(4, 1, 10, 10));
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JButton startButton = new JButton("Start Game");
        JButton levelsButton = new JButton("Levels");
        JButton settingsButton = new JButton("Settings");
        JButton exitButton = new JButton("Exit"); // --- NEW: Exit button ---

        // --- UPDATED: The start button now resets the game before showing it ---
        startButton.addActionListener(e -> {
            gamePanel.resetGame();
            cardLayout.show(parentPanel, "game");
        });

        // --- NEW: The exit button closes the application ---
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton);
        add(levelsButton);
        add(settingsButton);
        add(exitButton);
    }
}