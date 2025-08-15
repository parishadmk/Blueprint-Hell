package com.yourname.blueprinthell.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SystemNode {
    private Point location;
    private Queue<Packet> buffer;
    private int bufferCapacity = 5;

    // --- NEW: Add lists for ports ---
    private List<Point> inputPorts;
    private List<Point> outputPorts;
    public static final int PORT_SIZE = 10; // Size of the port for drawing and clicking

    // --- NEW: type field to differentiate node categories ---
    private String type;

    public SystemNode(Point location) {
        this(location, "Standard");
    }

    public SystemNode(Point location, String type) {
        this.location = location;
        this.type = type;
        this.buffer = new LinkedList<>();

        // --- NEW: Initialize port lists ---
        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();

        // --- NEW: Add some default ports for testing ---
        // Add two input ports on the left side
        inputPorts.add(new Point(location.x - 20, location.y - 10));
        inputPorts.add(new Point(location.x - 20, location.y + 10));

        // Add two output ports on the right side
        outputPorts.add(new Point(location.x + 20, location.y - 10));
        outputPorts.add(new Point(location.x + 20, location.y + 10));
    }

    // --- NEW: Copy constructor ---
    public SystemNode(SystemNode other) {
        this(other.location == null ? null : new Point(other.location), other.type);
        this.bufferCapacity = other.bufferCapacity;

        // Copy buffer contents
        this.buffer = new LinkedList<>();
        for (Packet p : other.buffer) {
            Packet copyPacket = new Packet(p.getShape(), new Point(p.getPosition()), new Point(p.getVelocity()), p.getSize());
            copyPacket.addNoise(p.getNoise());
            this.buffer.add(copyPacket);
        }

        // Copy ports
        this.inputPorts = new ArrayList<>();
        for (Point p : other.inputPorts) {
            this.inputPorts.add(new Point(p));
        }
        this.outputPorts = new ArrayList<>();
        for (Point p : other.outputPorts) {
            this.outputPorts.add(new Point(p));
        }
    }

    // --- NEW: Getters for the port lists ---
    public List<Point> getInputPorts() {
        return inputPorts;
    }

    public List<Point> getOutputPorts() {
        return outputPorts;
    }

    public boolean canStorePacket() {
        return buffer.size() < bufferCapacity;
    }

    public void storePacket(Packet packet) {
        if (canStorePacket()) {
            buffer.add(packet);
        }
    }

    public Packet releasePacket() {
        return buffer.poll();
    }

    public Point getLocation() {
        return location;
    }

    public int getBufferSize() {
        return buffer.size();
    }

    // --- NEW: Getter for type ---
    public String getType() {
        return type;
    }
}