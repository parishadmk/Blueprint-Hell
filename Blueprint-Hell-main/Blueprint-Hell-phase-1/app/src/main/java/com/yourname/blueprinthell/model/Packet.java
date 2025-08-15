package com.yourname.blueprinthell.model;

import java.awt.*;

public class Packet {
    public enum Shape { SQUARE, TRIANGLE }
    public enum Type { 
        MESSENGER,  // Normal packets
        SECRET,     // Slower, higher reward
        BULKY       // Heavy, breaks wires
    }

    // Physics constants
    private static final double BULKY_MAX_SPEED = 3.0;
    private static final double DEFAULT_MAX_SPEED = 10.0;
    private static final double SECRET_SPEED_FACTOR = 0.7;

    private final Shape shape;
    private final Type type;
    private final int size;
    private Point position;
    private Point velocity;
    private double acceleration;
    private double noise;
    private int currentWireSegment;

    public Packet(Shape shape, Point position, Point velocity, int size, Type type) {
        this.shape = shape;
        this.position = position;
        this.velocity = velocity;
        this.size = size;
        this.type = type;
        this.acceleration = 0;
        this.noise = 0;
        this.currentWireSegment = 0;
        
        // Secret packets start slower
        if (type == Type.SECRET) {
            velocity.x = (int)((velocity.x / speed) * maxSpeed);
            velocity.y = (int)((velocity.y / speed) * maxSpeed);
        }
    }

    public void move(Wire wire) {
        if (wire == null) {
            // Fallback to linear movement
            position.translate(velocity.x, velocity.y);
            return;
        }

        // Apply acceleration
        velocity.x += acceleration * Math.signum(velocity.x);
        velocity.y += acceleration * Math.signum(velocity.y);

        // Enforce speed limits
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y);
        double maxSpeed = (type == Type.BULKY) ? BULKY_MAX_SPEED : DEFAULT_MAX_SPEED;
        if (speed > maxSpeed) {
            velocity.x = (velocity.x / speed) * maxSpeed;
            velocity.y = (velocity.y / speed) * maxSpeed;
        }

        // Move along wire segments
        List<Point> path = wire.getPath();
        if (currentWireSegment >= path.size() - 1) return;

        Point start = path.get(currentWireSegment);
        Point end = path.get(currentWireSegment + 1);
        
        // Calculate movement along segment
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double segmentLength = Math.sqrt(dx * dx + dy * dy);
        
        double moveX = velocity.x;
        double moveY = velocity.y;
        
        // Normalize direction if needed
        if (segmentLength > 0) {
            double directionX = dx / segmentLength;
            double directionY = dy / segmentLength;
            moveX = directionX * speed;
            moveY = directionY * speed;
        }

        position.setLocation(
            position.x + moveX,
            position.y + moveY
        );

        // Advance to next segment if reached end
        if (position.distance(end) < Math.abs(speed)) {
            position.setLocation(end);
            currentWireSegment++;
        }
    }

    // --- Getters/Setters ---
    public Shape getShape() { return shape; }
    public Point getPosition() { return new Point(position); }
    public Point getVelocity() { return new Point(velocity); }
    public double getNoise() { return noise; }
    public int getSize() { return size; }
    public Type getType() { return type; }
    public double getAcceleration() { return acceleration; }

    public void setPosition(Point position) { 
        this.position = new Point(position); 
    }
    
    public void setVelocity(Point velocity) { 
        this.velocity = new Point(velocity); 
        
        // Secret packets maintain reduced speed
        if (type == Type.SECRET) {
            this.velocity.x *= SECRET_SPEED_FACTOR;
            this.velocity.y *= SECRET_SPEED_FACTOR;
        }
    }
    
    public void setAcceleration(double acceleration) { 
        this.acceleration = acceleration; 
    }

    // --- Noise System ---
    public void addNoise(double amount) { 
        noise += amount; 
    }
    
    public boolean isLost() { 
        return noise > size; 
    }
    
    // --- Helper Methods ---
    public boolean isBulky() {
        return type == Type.BULKY;
    }
    
    public boolean isSecret() {
        return type == Type.SECRET;
    }
}
