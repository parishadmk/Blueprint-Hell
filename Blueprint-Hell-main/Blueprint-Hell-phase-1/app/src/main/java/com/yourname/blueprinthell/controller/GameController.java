package com.yourname.blueprinthell.controller;

import com.yourname.blueprinthell.model.GameState;
import com.yourname.blueprinthell.model.Packet;
import com.yourname.blueprinthell.model.SystemNode;
import com.yourname.blueprinthell.model.Wire;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GameController {

    private GameState gameState;
    private JPanel gamePanel; // The view
    private Timer gameTimer;

    // Wiring state
    private boolean isWiring = false;
    private Point wireStartPoint = null;
    private SystemNode wireStartNode = null;
    private Point currentMousePos = null;

    public GameController(GameState gameState, JPanel gamePanel, Timer timer) {
        this.gameState = gameState;
        this.gamePanel = gamePanel;
        this.gameTimer = timer;
        addListeners();
    }

    private void addListeners() {
        gamePanel.addMouseListener(new MouseAdapter() {
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

        gamePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (gameState.getCurrentStatus() == GameState.GameStatus.RUNNING) {
                    handleMouseDrag(e.getPoint());
                }
            }
        });
    }

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
            gamePanel.repaint();
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
                            double length = newWire.getLength();
                            int speed = 2;
                            Point velocity = new Point(
                                    (int) ((portPos.x - wireStartPoint.x) * speed / length),
                                    (int) ((portPos.y - wireStartPoint.y) * speed / length)
                            );
                            Packet newPacket = new Packet(Packet.Shape.SQUARE, new Point(wireStartPoint), velocity, 10);
                            newWire.placePacket(newPacket); // Associate packet with wire
                            gameState.addPacket(newPacket);
                        }
                        break;
                    }
                }
            }
            isWiring = false;
            wireStartNode = null;
            wireStartPoint = null;
            gamePanel.repaint();
        }
    }

    // Getters for the view to use
    public boolean isWiring() { return isWiring; }
    public Point getWireStartPoint() { return wireStartPoint; }
    public Point getCurrentMousePos() { return currentMousePos; }
}