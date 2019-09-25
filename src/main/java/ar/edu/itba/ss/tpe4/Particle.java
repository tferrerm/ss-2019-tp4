package ar.edu.itba.ss.tpe4;

import java.awt.geom.Point2D;
import java.util.Objects;

public class Particle implements Cloneable {

    private static int count = 0;

    private int id;
    private final double radius;
    private double mass;
    private final Point2D.Double position;
    private Point2D.Double velocity;

    public Particle(final double radius, final Point2D.Double position) {
        this.radius = radius;
        this.position = position;
    }

    public Particle(final double radius, final double mass) {
        this.id = count++;
        this.radius = radius;
        this.mass = mass;
        this.position = new Point2D.Double();
        this.velocity = new Point2D.Double();
    }

    public Particle(final double radius, final double mass, final double x, final double y, final double vx, final double vy) {
        this.id = count++;
        this.radius = radius;
        this.mass = mass;
        this.position = new Point2D.Double(x, y);
        this.velocity = new Point2D.Double(vx, vy);
    }

    private Particle(final int id, final double radius, final double mass, final double x, final double y,
            final double vx, final double vy) {
        this.id = id;
        this.radius = radius;
        this.mass = mass;
        this.position = new Point2D.Double(x, y);
        this.velocity = new Point2D.Double(vx, vy);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o)
            return true;
        if(!(o instanceof Particle))
            return false;
        Particle other = (Particle) o;
        return this.id == other.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "id: " + id + "; radius: " + radius + " ; mass: " + mass + " ; x: " + position.x
                + " ; y: " + position.y + " ; vx: " + velocity.x + " ; vy: " + velocity.y;
    }

    @Override
    public Particle clone() {
        return new Particle(id, radius, mass, position.getX(), position.getY(), velocity.getX(), velocity.getY());
    }

    public int getId() {
        return id;
    }

    public double getRadius() {
        return radius;
    }

    public double getMass() {
        return mass;
    }

    public Point2D.Double getPosition() {
        return position;
    }

    public void setPosition(final double x, final double y) {
        position.x = x;
        position.y = y;
    }

    public Point2D.Double getVelocity() {
        return velocity;
    }

    public void setVelocity(final double vx, final double vy) {
        velocity.x = vx;
        velocity.y = vy;
    }

    public void setVelocity(final Point2D.Double v) {
        setVelocity(v.getX(), v.getY());
    }

    public double getVelocityAngle() {
        return Math.atan2(velocity.y, velocity.x);
    }
    
    public double getVelocityModule() {
        return Math.sqrt(Math.pow(velocity.getX(), 2) + Math.pow(velocity.getY(), 2));
    }

}
