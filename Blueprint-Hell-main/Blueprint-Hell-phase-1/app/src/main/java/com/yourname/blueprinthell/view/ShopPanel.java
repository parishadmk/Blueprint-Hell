package com.yourname.blueprinthell.view;

import javax.swing.*;
import java.awt.*;

public class ShopPanel extends JPanel {
    public ShopPanel(CardLayout cardLayout, JPanel parentPanel, GamePanel gamePanel) {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // Title
        JLabel title = new JLabel("Shop", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // Items Panel
        JPanel itemsPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        itemsPanel.setOpaque(false); // Make it transparent
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create buttons for each item
        JButton atarButton = new JButton("Atar (3 Coins): Disable Impact Waves for 10s");
        JButton aryamanButton = new JButton("Aryaman (4 Coins): Disable Collisions for 5s");
        JButton anahitaButton = new JButton("Anahita (5 Coins): Reset all packet noise");
        JButton wireButton = new JButton("Extra Wire (2 Coins): +50 wire length");

        itemsPanel.add(atarButton);
        itemsPanel.add(aryamanButton);
        itemsPanel.add(anahitaButton);
        itemsPanel.add(wireButton);
        add(itemsPanel, BorderLayout.CENTER);

        // Bottom panel with coins and navigation
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        JLabel coinsLabel = new JLabel("Coins: " + gamePanel.getGameState().getCoins(), SwingConstants.CENTER);
        coinsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backButton = new JButton("Back to Game");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> cardLayout.show(parentPanel, "game"));

        JButton menuButton = new JButton("Main Menu");
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.addActionListener(e -> cardLayout.show(parentPanel, "menu"));

        bottomPanel.add(coinsLabel);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(backButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(menuButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}