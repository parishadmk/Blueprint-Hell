package com.yourname.blueprinthell;

// Logging Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Swing & AWT Imports
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Project-specific Imports
import com.yourname.blueprinthell.controller.GameController;
import com.yourname.blueprinthell.model.GameState;
import com.yourname.blueprinthell.view.GamePanel;

public class App {

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().startGame());
    }

    private void startGame() {
        // --- UI SETUP ---
        JFrame frame = new JFrame("Blueprint Hell");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);
        GamePanel gamePanel = new GamePanel(cardLayout, mainPanel); // Create the panel
        mainPanel.add(gamePanel, "game");

        frame.add(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        // Add the listener to log when the game window is closed
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.info("Game window closed. Shutting down.");
                super.windowClosing(e);
            }
        });

        // --- GAME LOGIC SETUP ---
        GameState gameState = new GameState(); // Create the game state
        gamePanel.setGameState(gameState); // IMPORTANT: Link the state to the panel

        Timer gameTimer = new Timer(16, e -> { // Approx. 60 FPS
            gameState.update();
            gamePanel.repaint();
        });

        // The controller needs all three components
        new GameController(gameState, gamePanel, gameTimer);
        
        gameTimer.start();
        frame.setVisible(true); // Show the frame AFTER everything is set up
        logger.info("Game initialized and started successfully.");
    }
}