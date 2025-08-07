package com.yourname.blueprinthell.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel(CardLayout cardLayout, JPanel parentPanel) {
        setLayout(new GridLayout(4, 1, 10, 10));
        setBackground(Color.DARK_GRAY);

        JButton startButton = new JButton("Start Game");
        JButton levelsButton = new JButton("Levels");
        JButton settingsButton = new JButton("Settings");
        JButton exitButton = new JButton("Exit");

        startButton.addActionListener(e -> cardLayout.show(parentPanel, "game"));
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton);
        add(levelsButton);
        add(settingsButton);
        add(exitButton);
    }
}
