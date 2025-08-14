package com.yourname.blueprinthell.view;

import com.yourname.blueprinthell.controller.GameController; // NEW Import
import com.yourname.blueprinthell.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener {
    private GameState gameState;
    private Timer timer;
    private GameController gameController; // NEW reference to the controller

    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // UI Buttons
    private JButton resumeButton;
    private JButton menuButton;
    private JButton pauseButton;
    private JButton shopButton;

    public GamePanel(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        
        setLayout(null); 
        setBackground(Color.BLACK);
        setFocusable(true);
        
        this.timer = new Timer(1000 / 60, this);
        resetGame(); // Initialize game state and controller

        setupUIButtons();
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (gameState.getCurrentStatus() != GameState.GameStatus.GAME_OVER && gameState.getCurrentStatus() != GameState.GameStatus.WIN) {
                    timer.start();
                }
            }
        });
    }
    
    public final void resetGame() {
        this.gameState = new GameState();
        // --- NEW: Add systems for a fresh game ---
        gameState.addSystem(new SystemNode(new Point(100, 150)));
        gameState.addSystem(new SystemNode(new Point(400, 150)));
        gameState.addSystem(new SystemNode(new Point(100, 400)));
        gameState.addSystem(new SystemNode(new Point(400, 400)));
        
        // --- NEW: Create the controller ---
        this.gameController = new GameController(this.gameState, this, this.timer);
        
        if (!timer.isRunning()) {
            timer.start();
        }
        
        if(shopButton != null) { // Null check for first-time setup
            shopButton.setVisible(true);
            pauseButton.setVisible(true);
            resumeButton.setVisible(false);
            menuButton.setVisible(false);
        }
        
        requestFocusInWindow();
    }

    private void setupUIButtons() {
        pauseButton = new JButton("Pause");
        pauseButton.setBounds(880, 20, 100, 30); 
        pauseButton.addActionListener(e -> togglePause());
        add(pauseButton);
        
        shopButton = new JButton("Shop");
        shopButton.setBounds(770, 20, 100, 30);
        shopButton.addActionListener(e -> pauseAndGoTo("shop"));
        add(shopButton);

        resumeButton = new JButton("Resume");
        menuButton = new JButton("Main Menu");
        resumeButton.setBounds(400, 300, 200, 50);
        menuButton.setBounds(400, 360, 200, 50);
        resumeButton.addActionListener(e -> togglePause());
        menuButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        add(resumeButton);
        add(menuButton);

        resumeButton.setVisible(false);
        menuButton.setVisible(false);
    }
    
    private void togglePause() {
        if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
            gameState.pauseGame();
            timer.stop();
            resumeButton.setVisible(true);
            menuButton.setVisible(true);
            pauseButton.setVisible(false);
            shopButton.setVisible(false);
        } else if (gameState.getCurrentStatus() == GameState.GameStatus.PAUSED) {
            gameState.resumeGame();
            timer.start();
            resumeButton.setVisible(false);
            menuButton.setVisible(false);
            pauseButton.setVisible(true);
            shopButton.setVisible(true);
        }
        repaint();
    }

    private void pauseAndGoTo(String panelName) {
        if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
            timer.stop();
            cardLayout.show(mainPanel, panelName);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
            gameState.update();
        }
        if (gameState.getCurrentStatus() == GameState.GameStatus.GAME_OVER || gameState.getCurrentStatus() == GameState.GameStatus.WIN) {
            timer.stop();
        }
        repaint();
    }
    
    private void drawHUD(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(Color.WHITE);
        String wireInfo = String.format("Wire Length: %.0f", gameState.getRemainingWireLength());
        String lossInfo = "Packet Loss: " + gameState.getPacketLoss();
        String coinsInfo = "Coins: " + gameState.getCoins();
        String deliveredInfo = "Delivered: " + gameState.getPacketsDelivered() + "/10";
        g2.drawString(wireInfo, 20, 30);
        g2.drawString(lossInfo, 20, 50);
        g2.drawString(coinsInfo, 20, 70);
        g2.drawString(deliveredInfo, 20, 90);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(new Font("Monospaced", Font.BOLD, 50));
        g2.setColor(Color.RED);
        String msg = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
    
    private void drawWinScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(new Font("Monospaced", Font.BOLD, 50));
        g2.setColor(Color.GREEN);
        String msg = "YOU WIN!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(new Font("Monospaced", Font.BOLD, 50));
        g2.setColor(Color.WHITE);
        String msg = "PAUSED";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2 - 50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.decode("#555577"));
        for (Wire w : gameState.getWires()) {
            g2.drawLine(w.getStart().x, w.getStart().y, w.getEnd().x, w.getEnd().y);
        }
        
        // --- UPDATED: Drawing logic now asks the controller for wiring state ---
        if (gameController.isWiring()) {
            g2.setColor(Color.GREEN);
            Point start = gameController.getWireStartPoint();
            Point mouse = gameController.getCurrentMousePos();
            if (start != null && mouse != null) {
                g2.drawLine(start.x, start.y, mouse.x, mouse.y);
            }
        }
        
        for (SystemNode s : gameState.getSystems()) {
            Point loc = s.getLocation();
            int w = 40, h = 40;
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(loc.x - w / 2, loc.y - h / 2, w, h);
            g2.setColor(Color.BLUE);
            for (Point p : s.getInputPorts()) {
                g2.fillRect(p.x - SystemNode.PORT_SIZE / 2, p.y - SystemNode.PORT_SIZE / 2, SystemNode.PORT_SIZE, SystemNode.PORT_SIZE);
            }
            g2.setColor(Color.ORANGE);
            for (Point p : s.getOutputPorts()) {
                g2.fillRect(p.x - SystemNode.PORT_SIZE / 2, p.y - SystemNode.PORT_SIZE / 2, SystemNode.PORT_SIZE, SystemNode.PORT_SIZE);
            }
        }
        for (Packet p : gameState.getPackets()) {
            Point pos = p.getPosition();
            int sz = p.getSize();
            if (p.getShape() == Packet.Shape.SQUARE) {
                g2.setColor(Color.YELLOW);
                g2.fillRect(pos.x - sz / 2, pos.y - sz / 2, sz, sz);
            } else {
                g2.setColor(Color.CYAN);
                g2.fill(new Polygon(new int[]{pos.x, pos.x - sz / 2, pos.x + sz / 2}, new int[]{pos.y - sz / 2, pos.y + sz / 2, pos.y + sz / 2}, 3));
            }
        }
        
        drawHUD(g2);

        if (gameState.getCurrentStatus() == GameState.GameStatus.PAUSED) {
            drawPauseScreen(g2);
        }
        if (gameState.getCurrentStatus() == GameState.GameStatus.GAME_OVER) {
            drawGameOver(g2);
        }
        if (gameState.getCurrentStatus() == GameState.GameStatus.WIN) {
            drawWinScreen(g2);
        }
    }
}