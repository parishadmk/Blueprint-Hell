package com.yourname.blueprinthell.model;

import java.awt.*;
import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Shape { SQUARE, TRIANGLE }

    private Shape shape;
    private Point position;
    private Point velocity;
    private double noise;
    private int size;

    public Packet(Shape shape, Point position, Point velocity, int size) {
        this.shape = shape;
        this.position = position;
        this.velocity = velocity;
        this.size = size;
        this.noise = 0.0;
    }

    // Getters/setters
    public Shape getShape() { return shape; }
    public Point getPosition() { return position; }
    public Point getVelocity() { return velocity; }
    public double getNoise() { return noise; }
    public int getSize() { return size; }

    public void move() {
        position.translate(velocity.x, velocity.y);
    }

    public void addNoise(double amount) {
        noise += amount;
    }

    public boolean isLost() {
        return noise > size;
    }

        // Add these methods to Packet.java

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setVelocity(Point velocity) {
        this.velocity = velocity;
    }
}

