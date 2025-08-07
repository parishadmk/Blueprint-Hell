package com.yourname.blueprinthell.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.yourname.blueprinthell.model.GameState.GameStatus;

public class GameState {
    // --- NEW: Enum to track the game's current state ---
    public enum GameStatus { RUNNING, PAUSED, GAME_OVER }
    private GameStatus currentStatus = GameStatus.RUNNING;

    private List<Packet> packets = new ArrayList<>();
    private List<SystemNode> systems = new ArrayList<>();
    private List<Wire> wires = new ArrayList<>();
    private Map<Wire, SystemNode> wireDestinations = new HashMap<>();

    private int packetLoss = 0;
    private int totalPacketsSpawned = 0;
    private int coins = 0;
    private double remainingWireLength = 500.0;

    public GameState() { }

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
        totalPacketsSpawned++;
    }

    public void addSystem(SystemNode s) {
        systems.add(s);
    }

    // --- NEW: Getter for the game status ---
    public GameStatus getCurrentStatus() {
        return currentStatus;
    }

    public List<Packet> getPackets() { return packets; }
    public List<SystemNode> getSystems() { return systems; }
    public List<Wire> getWires() { return wires; }
    public int getPacketLoss() { return packetLoss; }
    public int getCoins() { return coins; }
    public double getRemainingWireLength() { return remainingWireLength; }

    // --- NEW: Method to check the game over condition ---
    private void checkGameOver() {
        if (totalPacketsSpawned > 0) {
            // Game over if loss is more than 50%
            if ((double) packetLoss / totalPacketsSpawned > 0.5) {
                currentStatus = GameStatus.GAME_OVER;
                System.out.println("GAME OVER: Packet loss exceeded 50%");
            }
        }
    }

    public void update() {
        // Don't update if the game is over
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

        // Packet movement and state changes
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
                if (p.getPosition().distance(w.getEnd()) < p.getSize()) {
                    SystemNode dest = wireDestinations.get(w);
                    if (dest != null && dest.canStorePacket()) {
                        dest.storePacket(p);
                        coins += 1;
                    } else {
                        packetLoss++;
                    }
                    it.remove();
                    break;
                }
            }
        }

        // --- NEW: Check for game over at the end of each update ---
        checkGameOver();
    }
    // Add these methods anywhere inside the GameState class

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
}