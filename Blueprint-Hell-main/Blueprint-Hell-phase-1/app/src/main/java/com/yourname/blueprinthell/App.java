package com.yourname.blueprinthell;

import com.yourname.blueprinthell.view.GamePanel;
import com.yourname.blueprinthell.view.MainMenuPanel;
import com.yourname.blueprinthell.view.ShopPanel; // --- NEW: Import ShopPanel ---
import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Blueprint Hell");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Changed for easier closing
            frame.setSize(1000, 700);
            frame.setResizable(false);

            CardLayout cardLayout = new CardLayout();
            JPanel mainPanel = new JPanel(cardLayout);

            // Create all game views
            GamePanel game = new GamePanel(cardLayout, mainPanel); // --- UPDATED: Pass layout to GamePanel ---
            MainMenuPanel menu = new MainMenuPanel(cardLayout, mainPanel, game);
            ShopPanel shop = new ShopPanel(cardLayout, mainPanel);   // --- NEW: Create ShopPanel ---

            // Add all panels to the layout
            mainPanel.add(menu, "menu");
            mainPanel.add(game, "game");
            mainPanel.add(shop, "shop"); // --- NEW: Add shop to layout ---

            frame.setContentPane(mainPanel);
            frame.setLocationRelativeTo(null); // Center the window
            frame.setVisible(true);
        });
    }
}