package com.yourname.blueprinthell.view;

import javax.swing.*;
import java.awt.*;

public class ShopPanel extends JPanel {
    public ShopPanel(CardLayout cardLayout, JPanel parentPanel) {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // Title
        JLabel title = new JLabel("Shop", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // Items Panel
        JPanel itemsPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        itemsPanel.setOpaque(false); // Make it transparent
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create buttons for each item
        JButton atarButton = new JButton("Atar (3 Coins): Disable Impact Waves for 10s");
        JButton aryamanButton = new JButton("Aryaman (4 Coins): Disable Collisions for 5s");
        JButton anahitaButton = new JButton("Anahita (5 Coins): Reset all packet noise");

        itemsPanel.add(atarButton);
        itemsPanel.add(aryamanButton);
        itemsPanel.add(anahitaButton);
        add(itemsPanel, BorderLayout.CENTER);

        // Back to Game Button
        JButton backButton = new JButton("Back to Game");
        backButton.addActionListener(e -> cardLayout.show(parentPanel, "game"));
        add(backButton, BorderLayout.SOUTH);
    }
}