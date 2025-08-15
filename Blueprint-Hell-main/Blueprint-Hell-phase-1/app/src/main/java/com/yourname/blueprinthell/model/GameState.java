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

    public GameState() { }

    // --- NEW: Copy constructor and copy method for quick save/load ---
    public GameState(GameState other) {
        this.currentStatus = other.currentStatus;
        this.packetLoss = other.packetLoss;
        this.packetsDelivered = other.packetsDelivered;
        this.totalPacketsSpawned = other.totalPacketsSpawned;
        this.coins = other.coins;
        this.remainingWireLength = other.remainingWireLength;

        // Deep copy packets
        this.packets = new ArrayList<>();
        for (Packet p : other.packets) {
            Packet np = new Packet(p.getShape(), new Point(p.getPosition()), new Point(p.getVelocity()), p.getSize());
            np.addNoise(p.getNoise());
            this.packets.add(np);
        }

        // Deep copy systems
        this.systems = new ArrayList<>();
        for (SystemNode s : other.systems) {
            this.systems.add(new SystemNode(s));
        }

        // Deep copy wires and destinations
        this.wires = new ArrayList<>();
        this.wireDestinations = new HashMap<>();
        for (int i = 0; i < other.wires.size(); i++) {
            Wire ow = other.wires.get(i);
            Wire nw = new Wire(new Point(ow.getStart()), new Point(ow.getEnd()));
            this.wires.add(nw);
        }
        for (int i = 0; i < other.wires.size(); i++) {
            Wire ow = other.wires.get(i);
            SystemNode odest = other.wireDestinations.get(ow);
            if (odest != null) {
                int index = other.systems.indexOf(odest);
                if (index >= 0 && index < this.systems.size()) {
                    Wire nw = this.wires.get(i);
                    SystemNode ndest = this.systems.get(index);
                    this.wireDestinations.put(nw, ndest);
                }
            }
        }
    }

    public GameState copy() {
        return new GameState(this);
    }
    
    // --- The missing pause/resume methods ---
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
        packets.add(p);
        // Only count packets spawned by the player, not re-routed ones
        if (p.getVelocity().distance(0, 0) != 0) { // A rough check
             totalPacketsSpawned++;
        }
    }

    public void addSystem(SystemNode s) {
        systems.add(s);
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

        // Collision detection (no changes)
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

        // Packet movement and arrival
        Iterator<Packet> it = packets.iterator();
        while (it.hasNext()) {
            Packet p = it.next();
            p.move();

            if (p.isLost()) {
                packetLoss++;
                it.remove();
                continue;
            }

            for (Wire w : wires) {
                // Check if the packet is on this wire and has arrived
                if (w.getOccupyingPacket() == p && p.getPosition().distance(w.getEnd()) < p.getSize()) {
                    SystemNode dest = wireDestinations.get(w);
                    if (dest != null && dest.canStorePacket()) {
                        dest.storePacket(p);
                        coins += 1;
                        packetsDelivered++;
                    } else {
                        packetLoss++;
                    }
                    w.clearPacket(); // Free up the wire
                    it.remove();
                    break; 
                }
            }
        }
        
        // --- NEW: Re-routing logic ---
        // Check each system to see if it can release a packet
        for (SystemNode node : systems) {
            if (node.getBufferSize() > 0) {
                // Find an empty output wire connected to this system
                for (Wire wire : wires) {
                    if (!wire.isOccupied()) {
                        boolean wireBelongsToNode = false;
                        for (Point portPos : node.getOutputPorts()) {
                            if (wire.getStart().equals(portPos)) {
                                wireBelongsToNode = true;
                                break;
                            }
                        }

                        if (wireBelongsToNode) {
                            // Found an empty output wire. Release a packet.
                            Packet packetToRelease = node.releasePacket();
                            if (packetToRelease != null) {
                                // Reset its position to the start of the new wire
                                packetToRelease.setPosition(new Point(wire.getStart()));

                                // Calculate new velocity along the wire
                                double length = wire.getLength();
                                int speed = 2;
                                Point velocity = new Point(
                                        (int) ((wire.getEnd().x - wire.getStart().x) * speed / length),
                                        (int) ((wire.getEnd().y - wire.getStart().y) * speed / length)
                                );
                                packetToRelease.setVelocity(velocity);

                                // Add it back to the list of moving packets
                                packets.add(packetToRelease);
                                // Mark the wire as occupied by this packet
                                wire.placePacket(packetToRelease);
                                
                                // Packet has been re-routed, move to the next system
                                break; 
                            }
                        }
                    }
                }
            }
        }
        
        checkEndConditions();
    }
}