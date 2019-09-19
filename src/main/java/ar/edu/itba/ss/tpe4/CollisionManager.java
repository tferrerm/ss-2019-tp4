package ar.edu.itba.ss.tpe4;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class CollisionManager {

    private final Grid grid;
    private final List<ParticleCollision> particleCollisions;
    private final List<BorderCollision> borderCollisions;

    public CollisionManager(final Grid grid) {
        this.grid = grid;
        this.particleCollisions = new ArrayList<>();
        this.borderCollisions = new ArrayList<>();
    }

    public void executeAlgorithm() {
        initializeCollisions();
        double accumulatedTime = 0.0;
        double nextFrameTime = 0;
        while(Double.compare(nextFrameTime, Configuration.getTimeLimit()) <= 0) {
            Collision firstCollision = getFirstCollision();

            double collisionTime = firstCollision.getTime();
            double lastAccumulatedTime = accumulatedTime;
            accumulatedTime += collisionTime;

//          WRITE EVERY EVENT
//            if(Configuration.isSingleRunMode()) {
//                Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());
//            }

            if(Double.compare(accumulatedTime, nextFrameTime) >= 0) {
                nextFrameTime = Configuration.writeOvitoOutputFile(
                        accumulatedTime,
                        nextFrameTime - lastAccumulatedTime,
                        nextFrameTime,
                        grid.getParticles()
                );
            }

            if(isBigParticleBorderCollision(firstCollision)) {
                System.out.println("Big Particle collided with border. Finishing...");
                return;
            }
            
            grid.updateParticles(collisionTime);
            updateCollisionVelocities(firstCollision);
            updateCollisionTimes(firstCollision);
        }
    }

    private boolean isBigParticleBorderCollision(Collision collision) {
        boolean isBorderCollision = collision instanceof BorderCollision;
        if(isBorderCollision) {
            return collision.getParticle().getId() == 0;
        }
        return false;
    }

    private void initializeCollisions() {
        for(int i = 0; i < grid.getParticles().size(); i++) {
            Particle p1 = grid.getParticles().get(i);
            for(int j = i + 1; j < grid.getParticles().size(); j++) {
                Particle p2 = grid.getParticles().get(j);
                initializeParticleCollision(p1, p2);
            }
            initializeBorderCollisions(p1);
        }
    }

    private void initializeParticleCollision(final Particle particle, final Particle otherParticle) {
        particleCollisions.add(new ParticleCollision(particle, otherParticle));
    }

    private void initializeBorderCollisions(final Particle particle) {
        borderCollisions.addAll(Arrays.asList(
                new BorderCollision(particle, Border.HORIZONTAL, grid.getAreaBorderLength()),
                new BorderCollision(particle, Border.VERTICAL, grid.getAreaBorderLength())
        ));
    }

    private Collision getFirstCollision() {
        Collision collision = Stream.of(particleCollisions, borderCollisions).flatMap(c -> c.stream())
                .min(Comparator.comparing(Collision::getTime)).get();
        if(Double.isNaN(collision.getTime()))
        	throw new IllegalStateException("Time for first collision is NaN.");
        if(Double.isInfinite(collision.getTime()))
        	throw new IllegalStateException("No collisions will occur.");

        return collision;
    }

    private void updateCollisionVelocities(final Collision collision) {
        Particle collisionParticle = collision.getParticle();
        if(collision instanceof ParticleCollision) {
            ParticleCollision particleCollision = (ParticleCollision) collision;
            Particle otherCollisionParticle = particleCollision.getOtherParticle();
            double impulse = calculateImpulse(particleCollision);
            final Point2D.Double impulseCoordinates = calculateImpulseCoordinates(particleCollision, impulse);

            Point2D.Double newVelocities = calculateNewParticleCollisionVelocities(collisionParticle, impulseCoordinates, -1);
            collisionParticle.setVelocity(newVelocities);

            newVelocities = calculateNewParticleCollisionVelocities(otherCollisionParticle, impulseCoordinates, 1);
            otherCollisionParticle.setVelocity(newVelocities);
        } else if(collision instanceof BorderCollision) {
            BorderCollision borderCollision = (BorderCollision) collision;
            if(borderCollision.getBorder().equals(Border.VERTICAL)) {
                collisionParticle.setVelocity(- collisionParticle.getVelocity().getX(), collisionParticle.getVelocity().getY());
            } else {
                collisionParticle.setVelocity(collisionParticle.getVelocity().getX(), - collisionParticle.getVelocity().getY());
            }
        }
    }

    private double calculateImpulse(final ParticleCollision collision) {
        final Particle p1 = collision.getParticle();
        final Particle p2 = collision.getOtherParticle();
        final double delta = collision.getDeltaVDeltaP();
        return (2 * p1.getMass() * p2.getMass()) * delta
                / (collision.getSigma() * (p1.getMass() + p2.getMass()));
    }

    private Point2D.Double calculateImpulseCoordinates(final ParticleCollision collision, final double impulse) {

        Point2D.Double pd = new Point2D.Double(
                (impulse * collision.getDeltaX()) / collision.getSigma(),
                (impulse * collision.getDeltaY()) / collision.getSigma()
        );
        return pd;
    }

    private Point2D.Double calculateNewParticleCollisionVelocities(
            final Particle p, final Point2D.Double impulseCoordinates, final int sign) {
        return new Point2D.Double(
                p.getVelocity().getX() + sign * impulseCoordinates.getX() / p.getMass(),
                p.getVelocity().getY() + sign * impulseCoordinates.getY() / p.getMass()
        );
    }

    private void updateCollisionTimes(final Collision firstCollision) {
    	double firstCollisionTime = firstCollision.getTime();
        if(firstCollision instanceof ParticleCollision) {
            updateCollisions(firstCollision.getParticle(), ((ParticleCollision) firstCollision).getOtherParticle(), firstCollisionTime);
        } else if(firstCollision instanceof BorderCollision) {
        	updateCollisions(firstCollision.getParticle(), firstCollisionTime);
        }
    }

    private void updateCollisions(final Particle particle, final double firstCollisionTime) {
    	updateCollisions(particle, null, firstCollisionTime);
    }

    private void updateCollisions(final Particle particle, final Particle otherParticle, final double firstCollisionTime) {
	
    	for(ParticleCollision collision : particleCollisions) {
        	if(particlesInvolvedInCollision(collision, particle, otherParticle)) {
            	collision.updateTime();
            } else {
                collision.updateTime(firstCollisionTime);
            }
        }

        for(BorderCollision collision : borderCollisions) {
        	if(collision.getParticle().equals(particle) || collision.getParticle().equals(otherParticle)) {
                collision.updateTime();
            } else {
                collision.updateTime(firstCollisionTime);
            }
        }
    }

	private boolean particlesInvolvedInCollision(final ParticleCollision collision, final Particle particle, final Particle otherParticle) {
		return collision.getParticle().equals(particle) || collision.getOtherParticle().equals(particle)
				|| collision.getParticle().equals(otherParticle) || collision.getOtherParticle().equals(otherParticle);
	}

}
