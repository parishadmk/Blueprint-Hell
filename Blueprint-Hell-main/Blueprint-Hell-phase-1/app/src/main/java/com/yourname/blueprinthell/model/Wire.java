package com.yourname.blueprinthell.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Wire {
    private final List<Point> path;          // Points defining the wire (start, bends, end)
    private Packet occupyingPacket;
    private int bendsUsed;             // Track bend points (max 3 per Phase 2)
    private double length;             // Cached length for performance

    public Wire(Point start, Point end) {
        this.path = new ArrayList<>();
        this.path.add(start);
        this.path.add(end);
        this.occupyingPacket = null;
        this.bendsUsed = 0;
        this.length = start.distance(end); // Initial length (straight line)
    }

    /**
     * Adds a bend point to the wire (Phase 2 feature)
     * @param segmentIndex Index of the segment to bend (0 to n-1)
     * @param bendPoint The new bend point coordinates
     * @param coins Cost in coins to add this bend
     * @return true if bend was added successfully
     */
    public boolean addBend(int segmentIndex, Point bendPoint, int coins) {
        if (bendsUsed >= 3 || coins < 1 || segmentIndex < 0 || segmentIndex >= path.size() - 1) {
            return false; // Max 3 bends or invalid segment
        }
        path.add(segmentIndex + 1, bendPoint);
        bendsUsed++;
        updateLength();
        return true;
    }

    /**
     * Recalculates the total wire length
     */
    private void updateLength() {
        length = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            length += path.get(i).distance(path.get(i + 1));
        }
    }

    // --- Accessors ---
    public List<Point> getPath() { 
        return new ArrayList<>(path); // Return copy for safety
    }
    
    public boolean isOccupied() { 
        return occupyingPacket != null; 
    }
    
    public Packet getOccupyingPacket() { 
        return occupyingPacket; 
    }
    
    public void placePacket(Packet p) { 
        this.occupyingPacket = p; 
    }
    
    public void clearPacket() { 
        this.occupyingPacket = null; 
    }
    
    public double getLength() { 
        return length; 
    }
    
    public int getBendsUsed() {
        return bendsUsed;
    }
}