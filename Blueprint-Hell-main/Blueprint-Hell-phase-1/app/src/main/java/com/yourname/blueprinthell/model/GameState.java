package com.yourname.blueprinthell.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameState {
    public enum GameStatus { RUNNING, PAUSED, GAME_OVER, WIN }
    private GameStatus currentStatus = GameStatus.RUNNING;

    private List<Packet> packets = new ArrayList<>();
    private List<SystemNode> systems = new ArrayList<>();
    private List<Wire> wires = new ArrayList<>();
    private Map<Wire, SystemNode> wireDestinations = new HashMap<>();

    private int packetLoss = 0;
    private int packetsDelivered = 0;
    private int totalPacketsSpawned = 0;
    private int coins = 0;
    private double remainingWireLength = 500.0;

    // Phase 2 physics constants
    private static final double BULKY_DECELERATION = -0.1;
    private static final double LONG_WIRE_ACCELERATION = 0.05;
    private static final double SECRET_PACKET_SPEED_FACTOR = 0.8;
    private static final int LONG_WIRE_THRESHOLD = 300;

    public GameState() { }

    public void pauseGame() {
        if (currentStatus == GameStatus.RUNNING) {
            currentStatus = GameStatus.PAUSED;
        }
    }

    public void resumeGame() {
        if (currentStatus == GameStatus.PAUSED) {
            currentStatus = GameStatus.RUNNING;
        }
    }

    public boolean addWire(Wire w, SystemNode dest) {
        if (remainingWireLength >= w.getLength()) {
            wires.add(w);
            wireDestinations.put(w, dest);
            remainingWireLength -= w.getLength();
            return true;
        }
        return false;
    }

    public void addPacket(Packet p) {
        initializePacketPhysics(p);
        packets.add(p);
        if (p.getVelocity().distance(0, 0) != 0) {
            totalPacketsSpawned++;
        }
    }

    public void addSystem(SystemNode s) {
        systems.add(s);
    }

    private void initializePacketPhysics(Packet p) {
        if (p.getType() == Packet.Type.BULKY) {
            p.setAcceleration(BULKY_DECELERATION);
        } else if (p.getType() == Packet.Type.SECRET) {
            Point vel = p.getVelocity();
            p.setVelocity(new Point(
                (int)(vel.x * SECRET_PACKET_SPEED_FACTOR),
                (int)(vel.y * SECRET_PACKET_SPEED_FACTOR)
            ));
        }
    }

    public GameStatus getCurrentStatus() { return currentStatus; }
    public List<Packet> getPackets() { return packets; }
    public List<SystemNode> getSystems() { return systems; }
    public List<Wire> getWires() { return wires; }
    public int getPacketLoss() { return packetLoss; }
    public int getCoins() { return coins; }
    public double getRemainingWireLength() { return remainingWireLength; }
    public int getPacketsDelivered() { return packetsDelivered; }

    private void checkEndConditions() {
        if (packetsDelivered >= 10) {
            currentStatus = GameStatus.WIN;
            return;
        }
        if (totalPacketsSpawned > 0 && (double) packetLoss / totalPacketsSpawned > 0.5) {
            currentStatus = GameStatus.GAME_OVER;
            return;
        }
        if (packets.isEmpty() && remainingWireLength < 1) {
            boolean allBuffersEmpty = true;
            for(SystemNode node : systems) {
                if(node.getBufferSize() > 0) {
                    allBuffersEmpty = false;
                    break;
                }
            }
            if(allBuffersEmpty) {
                currentStatus = GameStatus.GAME_OVER;
            }
        }
    }

    public void update() {
        if (currentStatus != GameStatus.RUNNING) {
            return;
        }

        // Collision detection
        List<Packet> currentPackets = new ArrayList<>(packets);
        for (int i = 0; i < currentPackets.size(); i++) {
            for (int j = i + 1; j < currentPackets.size(); j++) {
                Packet p1 = currentPackets.get(i);
                Packet p2 = currentPackets.get(j);
                if (p1.getPosition().distance(p2.getPosition()) < (p1.getSize() / 2.0 + p2.getSize() / 2.0)) {
                    p1.addNoise(1);
                    p2.addNoise(1);
                }
            }
        }

        // Packet movement and physics
        Iterator<Packet> it = packets.iterator();
        while (it.hasNext()) {
            Packet p = it.next();
            Wire currentWire = getWireForPacket(p);
            
            // Apply acceleration rules
            if (currentWire != null) {
                if (p.getType() != Packet.Type.BULKY && currentWire.getLength() > LONG_WIRE_THRESHOLD) {
                    p.setAcceleration(LONG_WIRE_ACCELERATION);
                }
            }
            
            // Physics update
            p.move(currentWire);
            
            // Handle lost packets
            if (p.isLost()) {
                packetLoss++;
                it.remove();
                continue;
            }


            // Destination handling
            if (currentWire != null) {
                handlePacketDestination(p, currentWire, it);
            }
        }
        
        // System processing
        processSystems();
        
        checkEndConditions();
    }

    private Wire getWireForPacket(Packet p) {
        for (Wire w : wires) {
            if (w.getOccupyingPacket() == p) {
                return w;
            }
        }
        return null;
    }

    private void handlePacketDestination(Packet p, Wire wire, Iterator<Packet> it) {
        Point wireEnd = wire.getPath().get(wire.getPath().size() - 1);
        if (p.getPosition().distance(wireEnd) < p.getSize()) {
            SystemNode dest = wireDestinations.get(wire);
            if (dest != null && dest.canStorePacket(p)) {
                dest.storePacket(p);
                coins += getDeliveryReward(p);
                packetsDelivered++;
            } else {
                packetLoss++;
            }
            wire.clearPacket();
            it.remove();
        }
    }

    private int getDeliveryReward(Packet p) {
        return switch (p.getType()) {
            case SECRET -> 3;
            case BULKY -> 2;
            default -> 1;
        };
    }

    private void processSystems() {
        for (SystemNode node : systems) {
            if (node.getBufferSize() > 0) {
                for (Wire wire : wires) {
                    if (!wire.isOccupied() && isWireConnectedToNode(wire, node)) {
                        releasePacketFromSystem(node, wire);
                        break;
                    }
                }
            }
        }
    }

    private boolean isWireConnectedToNode(Wire wire, SystemNode node) {
        Point wireStart = wire.getPath().get(0);
        return node.getOutputPorts().stream()
                .anyMatch(portPos -> portPos.equals(wireStart));
    }

    private void releasePacketFromSystem(SystemNode node, Wire wire) {
        Packet packetToRelease = node.releasePacket();
        if (packetToRelease != null) {
            Point wireStart = wire.getPath().get(0);
            packetToRelease.setPosition(new Point(wireStart));
            Point start = wire.getPath().get(0);
            Point end = wire.getPath().get(1);
            
            packetToRelease.setPosition(new Point(start));
            initializePacketVelocity(packetToRelease, start, end);
            initializePacketPhysics(packetToRelease);
            
            if (wire.getLength() > LONG_WIRE_THRESHOLD) {
                packetToRelease.setAcceleration(LONG_WIRE_ACCELERATION);
            }
            
            packets.add(packetToRelease);
            wire.placePacket(packetToRelease);
        }
    }

    private void initializePacketVelocity(Packet p, Point start, Point end) {
        double length = start.distance(end);
        int baseSpeed = (p.getType() == Packet.Type.BULKY) ? 1 : 2;
        p.setVelocity(new Point(
            (int)((end.x - start.x) * baseSpeed / length),
            (int)((end.y - start.y) * baseSpeed / length)
        ));
    }


    private Wire findWireForPacket(Packet p) {
    for (Wire w : wires) {
        if (w.getOccupyingPacket() == p) {
            return w;
        }
    }
    return null;
}

    public void applyAergiaEffect() {
    if (coins >= 10) {
        coins -= 10;
        for (Packet p : packets) {
            p.setAcceleration(0);
            p.setVelocity(new Point(0,0));
        }
    }
}

    public void applyLongWireEffect() {
        if (coins >= 5) {
            coins -= 5;
            remainingWireLength += 100; // Increase wire length by 100
        }
    }

    public void applySecretPacketEffect() {
        if (coins >= 15) {
            coins -= 15;
            for (Packet p : packets) {
                if (p.getType() == Packet.Type.SECRET) {
                    p.setVelocity(new Point(
                        (int)(p.getVelocity().x * SECRET_PACKET_SPEED_FACTOR),
                        (int)(p.getVelocity().y * SECRET_PACKET_SPEED_FACTOR)
                    ));
                }
            }
        }
    }
}