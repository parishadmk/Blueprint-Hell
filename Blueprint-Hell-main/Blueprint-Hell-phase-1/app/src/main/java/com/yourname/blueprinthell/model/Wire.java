package com.yourname.blueprinthell.model;

import java.awt.*;

public class Wire {
    private Point start;
    private Point end;
    private Packet occupyingPacket; // null if wire is free

    public Wire(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.occupyingPacket = null;
    }

    public Point getStart() { return start; }
    public Point getEnd() { return end; }

    public boolean isOccupied() {
        return occupyingPacket != null;
    }

    public void placePacket(Packet p) {
        this.occupyingPacket = p;
    }

    public Packet getOccupyingPacket() {
        return occupyingPacket;
    }

    public void clearPacket() {
        this.occupyingPacket = null;
    }

    public double getLength() {
        return start.distance(end);
    }
}