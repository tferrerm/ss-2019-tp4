package ar.edu.itba.ss.tpe4;

import java.awt.geom.Point2D;

public final class ParticleCollision extends Collision {

    private final Particle otherParticle;

    public ParticleCollision(final Particle particle, final Particle otherParticle) {
        super(particle);
        this.otherParticle = otherParticle;
        updateTime();
    }

    public void updateTime() {

        double deltaVDeltaP = getDeltaVDeltaP();
        double deltaVDeltaV = getDeltaVDeltaV();
        double deltaPDeltaP = getDeltaPDeltaP();
        double sigma = getSigma();

        double d = (deltaVDeltaP * deltaVDeltaP) - deltaVDeltaV * (deltaPDeltaP - (sigma * sigma));

        if(Double.compare(deltaVDeltaP, 0.0) >= 0 || Double.compare(d, 0.0) < 0) {
            time = Double.POSITIVE_INFINITY;
        } else {
            time = - ((deltaVDeltaP + Math.sqrt(d)) / deltaVDeltaV);
        }
    }

    public double getDeltaX() {
        return particle.getPosition().getX() - otherParticle.getPosition().getX();
    }

    public double getDeltaY() {
        return particle.getPosition().getY() - otherParticle.getPosition().getY();
    }

    public Point2D.Double getDeltaVelocity() {
        return new Point2D.Double(
                particle.getVelocity().getX() - otherParticle.getVelocity().getX(),
                particle.getVelocity().getY() - otherParticle.getVelocity().getY());
    }

    public Point2D.Double getDeltaPosition() {
        return new Point2D.Double(getDeltaX(), getDeltaY());
    }

    public double getDeltaVDeltaP() {
        return (getDeltaVelocity().getX() * getDeltaPosition().getX())
                + (getDeltaVelocity().getY() * getDeltaPosition().getY());
    }

    public double getDeltaVDeltaV() {
        return (getDeltaVelocity().getX() * getDeltaVelocity().getX())
                + (getDeltaVelocity().getY() * getDeltaVelocity().getY());
    }

    public double getDeltaPDeltaP() {
        return (getDeltaPosition().getX() * getDeltaPosition().getX())
                + (getDeltaPosition().getY() * getDeltaPosition().getY());
    }

    public double getSigma() {
        return particle.getRadius() + otherParticle.getRadius();
    }

    public Particle getOtherParticle() {
        return otherParticle;
    }

}
