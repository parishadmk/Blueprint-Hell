package com.yourname.blueprinthell.view;

import com.yourname.blueprinthell.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener {
    private GameState gameState;
    private Timer timer;

    private boolean isWiring = false;
    private Point wireStartPoint = null;
    private SystemNode wireStartNode = null;
    private Point currentMousePos = null;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // UI Buttons
    private JButton resumeButton;
    private JButton menuButton;
    private JButton pauseButton;
    // --- NEW: A permanent shop button ---
    private JButton shopButton;

    public GamePanel(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        
        setLayout(null); 
        setBackground(Color.BLACK);
        setFocusable(true);
        this.gameState = new GameState();

        // Add systems to the game
        SystemNode s1 = new SystemNode(new Point(100, 150));
        SystemNode s2 = new SystemNode(new Point(400, 150));
        SystemNode s3 = new SystemNode(new Point(100, 400));
        SystemNode s4 = new SystemNode(new Point(400, 400));
        gameState.addSystem(s1);
        gameState.addSystem(s2);
        gameState.addSystem(s3);
        gameState.addSystem(s4);

        setupUIButtons();

        // Mouse listeners for wiring
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
                    handleMousePress(e.getPoint());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
                    handleMouseRelease(e.getPoint());
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
                    handleMouseDrag(e.getPoint());
                }
            }
        });
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (gameState.getCurrentStatus() != GameState.GameStatus.GAME_OVER) {
                    timer.start();
                }
            }
        });

        this.timer = new Timer(1000 / 60, this);
        timer.start();
    }
    
    // --- UPDATED: Renamed to setupUIButtons ---
    private void setupUIButtons() {
        // On-screen pause button
        pauseButton = new JButton("Pause");
        pauseButton.setBounds(880, 20, 100, 30); 
        pauseButton.addActionListener(e -> togglePause());
        add(pauseButton);
        
        // --- NEW: On-screen shop button ---
        shopButton = new JButton("Shop");
        shopButton.setBounds(770, 20, 100, 30); // Positioned to the left of Pause
        shopButton.addActionListener(e -> pauseAndGoTo("shop"));
        add(shopButton);

        // Centered buttons for when the game is paused
        resumeButton = new JButton("Resume");
        menuButton = new JButton("Main Menu");
        resumeButton.setBounds(400, 300, 200, 50);
        menuButton.setBounds(400, 360, 200, 50);
        resumeButton.addActionListener(e -> togglePause());
        menuButton.addActionListener(e -> {
            this.gameState = new GameState(); // Reset game
            cardLayout.show(mainPanel, "menu");
        });
        add(resumeButton);
        add(menuButton);

        // Hide the centered buttons initially
        resumeButton.setVisible(false);
        menuButton.setVisible(false);
    }
    
    private void togglePause() {
        if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
            gameState.pauseGame();
            timer.stop();
            // Show the centered menu
            resumeButton.setVisible(true);
            menuButton.setVisible(true);
            // Hide the top-right buttons
            pauseButton.setVisible(false);
            shopButton.setVisible(false);
        } else if (gameState.getCurrentStatus() == GameState.GameStatus.PAUSED) {
            gameState.resumeGame();
            timer.start();
            // Hide the centered menu
            resumeButton.setVisible(false);
            menuButton.setVisible(false);
            // Show the top-right buttons again
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

    // ... (The rest of the file is exactly the same as before) ...
    private void handleMousePress(Point pressPoint) {
        for (SystemNode node : gameState.getSystems()) {
            for (Point portPos : node.getOutputPorts()) {
                if (pressPoint.distance(portPos) < SystemNode.PORT_SIZE) {
                    isWiring = true;
                    wireStartNode = node;
                    wireStartPoint = portPos;
                    currentMousePos = pressPoint;
                    return;
                }
            }
        }
    }

    private void handleMouseDrag(Point dragPoint) {
        if (isWiring) {
            currentMousePos = dragPoint;
            repaint();
        }
    }

    private void handleMouseRelease(Point releasePoint) {
        if (isWiring) {
            for (SystemNode destNode : gameState.getSystems()) {
                if (destNode == wireStartNode) continue;

                for (Point portPos : destNode.getInputPorts()) {
                    if (releasePoint.distance(portPos) < SystemNode.PORT_SIZE) {
                        Wire newWire = new Wire(wireStartPoint, portPos);
                        
                        if (gameState.addWire(newWire, destNode)) {
                            System.out.println("Wire created!");
                            double length = newWire.getLength();
                            int speed = 2;
                            Point velocity = new Point(
                                    (int) ((portPos.x - wireStartPoint.x) * speed / length),
                                    (int) ((portPos.y - wireStartPoint.y) * speed / length)
                            );
                            Packet newPacket = new Packet(Packet.Shape.SQUARE, new Point(wireStartPoint), velocity, 10);
                            gameState.addPacket(newPacket);
                        } else {
                            System.out.println("Not enough wire length!");
                        }
                        break;
                    }
                }
            }
            isWiring = false;
            wireStartNode = null;
            wireStartPoint = null;
            repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
            gameState.update();
        }
        if (gameState.getCurrentStatus() == GameState.GameStatus.GAME_OVER) {
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
        g2.drawString(wireInfo, 20, 30);
        g2.drawString(lossInfo, 20, 50);
        g2.drawString(coinsInfo, 20, 70);
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

        // Draw game elements
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.decode("#555577"));
        for (Wire w : gameState.getWires()) {
            g2.drawLine(w.getStart().x, w.getStart().y, w.getEnd().x, w.getEnd().y);
        }

        if (isWiring) {
            g2.setColor(Color.GREEN);
            g2.drawLine(wireStartPoint.x, wireStartPoint.y, currentMousePos.x, currentMousePos.y);
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
    }
}