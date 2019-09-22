package ar.edu.itba.ss.tpe4;

import java.util.Collections;
import java.util.List;

public final class Grid {
	
	private List<Particle> particles;
    private final double areaBorderLength;

    public Grid(final List<Particle> particles) {
        this.areaBorderLength = Configuration.AREA_BORDER_LENGTH;
        this.particles = particles;
    }

<<<<<<< HEAD
	public List<Particle> getParticles() {
        return Collections.unmodifiableList(particles);
=======
    /*public void updateParticles(final double deltaTime) {
        for(Particle p : particles) {
            double newPositionX = p.getPosition().getX() + p.getVelocity().getX() * deltaTime;
            double newPositionY = p.getPosition().getY() + p.getVelocity().getY() * deltaTime;
            p.setPosition(newPositionX, newPositionY);
        }
    }*/
    
    // Euler
    private void initPrevParticles() {
		for(Particle p : currParticles) {
			Particle prevParticle = p.clone();
			double prevPosition = p.getPosition().getX() - Configuration.getTimeStep() * p.getVelocity().getX()
					+ Math.pow(Configuration.getTimeStep(), 2) * getParticleForce(p) / (2 * p.getMass()); // + error
			double prevVelocity = p.getVelocity().getX() - (timeStep / p.getMass()) * getParticleForce(p);// + error
			prevParticle.setPosition(prevPosition, 0);
			prevParticle.setVelocity(prevVelocity, 0);
			prevParticles.add(prevParticle);
		}
	}
    
//    private void initNextParticles() {
//    	for(Particle p : currParticles) {
//    		nextParticles.add(p.clone());
//    	}
//    }
    
    public void verletUpdate() {
		for(int i = 0; i < currParticles.size(); i++) {
			Particle currParticle = currParticles.get(i);
			Particle prevParticle = prevParticles.get(i);
			
			double newPositionX = 2 * currParticle.getPosition().getX() - prevParticle.getPosition().getX()
					+ Math.pow(Configuration.getTimeStep(), 2) * getParticleForce(currParticle) / currParticle.getMass(); //+error
			
			double newVelocityX = (newPositionX - prevParticle.getPosition().getX()) / (2 * Configuration.getTimeStep()); // + error
			
			prevParticle.setPosition(currParticle.getPosition().getX(), 0);
			prevParticle.setVelocity(currParticle.getVelocity().getX(), 0);
			currParticle.setPosition(newPositionX, 0);
			currParticle.setVelocity(newVelocityX, 0);
		}
	}
    
    public void beemanUpdate() {
    	for(int i = 0; i < currParticles.size(); i++) {
			Particle currParticle = currParticles.get(i);
			Particle prevParticle = prevParticles.get(i);
			
			double newPositionX = currParticle.getPosition().getX() + currParticle.getVelocity().getX() * timeStep
					+ (2 / (double) 3) * (getParticleForce(currParticle) / currParticle.getMass()) * Math.pow(timeStep, 2)
					- (1 / (double) 6) * (getParticleForce(prevParticle) / prevParticle.getMass()) * Math.pow(timeStep, 2); //+error?
			
			double predictedVelocityX = currParticle.getVelocity().getX() 
					+ (3 / (double) 2) * (getParticleForce(currParticle) / currParticle.getMass()) * timeStep
					- (1 / (double) 2) * (getParticleForce(prevParticle) / prevParticle.getMass()) * timeStep; // + error
			Particle predictedParticle = currParticle.clone();
			predictedParticle.setPosition(newPositionX, 0);
			predictedParticle.setVelocity(predictedVelocityX, 0);
			
			double newAccelerationX = getParticleForce(predictedParticle) / predictedParticle.getMass();
			double correctedVelocityX = currParticle.getVelocity().getX()
					+ (1 / (double) 3) * newAccelerationX * timeStep + (5 / (double) 6) * (getParticleForce(currParticle) / currParticle.getMass()) * timeStep
					- (1 / (double) 6) * (getParticleForce(prevParticle) / prevParticle.getMass()) * timeStep;
			
			prevParticle.setPosition(currParticle.getPosition().getX(), 0);
			prevParticle.setVelocity(currParticle.getVelocity().getX(), 0);
			currParticle.setPosition(newPositionX, 0);
			currParticle.setVelocity(correctedVelocityX, 0);
		}
	}
    
    private double getParticleForce(final Particle p) {
    	if(Configuration.isOscillatorMode()) {
				return - Configuration.OSCILLATOR_K * p.getPosition().getX() - Configuration.OSCILLATOR_GAMMA * p.getVelocity().getX();
			} else {
				Double R = 0.1;
				return (12 * Configuration.GAS_EPSILON / (double) Configuration.GAS_Rm) * (Math.pow((Configuration.GAS_Rm / R), Configuration.GAS_L + 1) - Math.pow((Configuration.GAS_Rm / R), Configuration.GAS_J + 1));
			}
>>>>>>> Add time breaks
    }
	
	public void setParticles(List<Particle> particles) {
		this.particles = particles;
	}

    public double getAreaBorderLength() {
        return areaBorderLength;
    }

}
