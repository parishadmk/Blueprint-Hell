package com.yourname.blueprinthell.controller;

import com.yourname.blueprinthell.model.GameState;
import com.yourname.blueprinthell.model.Packet;
import com.yourname.blueprinthell.model.Packet.Shape;
import com.yourname.blueprinthell.model.SystemNode;
import com.yourname.blueprinthell.model.Wire;
import java.awt.Point;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



public class GameController {

    private GameState gameState;
    private JPanel gamePanel;
    // private Timer gameTimer; // Removed unused field

    private boolean isWiring = false;
    private Point wireStartPoint = null;
    private SystemNode wireStartNode = null;
    private Point currentMousePos = null;

    public GameController(GameState gameState, JPanel gamePanel) {
        this.gameState = gameState;
        this.gamePanel = gamePanel;
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
                            // --- UPDATED: Logic is the same, but now it uses the path ---
                            Point start = newWire.getPath().get(0);
                            Point end = newWire.getPath().get(1);
                            double length = start.distance(end);
                            int speed = 2;
                            Point velocity = new Point(
                                    (int) ((end.x - start.x) * speed / length),
                                    (int) ((end.y - start.y) * speed / length)
                            );
                            Packet newPacket = new Packet(Packet.Shape.SQUARE, new Point(start), velocity, 10, Packet.Type.MESSENGER);
                            // In GameController or where packets are created:
                            new Packet(Shape.SQUARE, position, velocity, 10, Packet.Type.BULKY);        
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

    public boolean isWiring() { return isWiring; }
    public Point getWireStartPoint() { return wireStartPoint; }
    public Point getCurrentMousePos() { return currentMousePos; }
}