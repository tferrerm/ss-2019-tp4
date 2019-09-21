package ar.edu.itba.ss.tpe4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Grid {
	
	private List<Particle> prevParticles = new ArrayList<>();
	private List<Particle> currParticles;
    private List<Particle> nextParticles = new ArrayList<>();
    private final double areaBorderLength;

    public Grid(final List<Particle> currParticles) {
        this.areaBorderLength = Configuration.AREA_BORDER_LENGTH;
        this.currParticles = currParticles;
        initPrevParticles();
        //initNextParticles();
    }

    /*public void updateParticles(final double deltaTime) {
        for(Particle p : particles) {
            double newPositionX = p.getPosition().getX() + p.getVelocity().getX() * deltaTime;
            double newPositionY = p.getPosition().getY() + p.getVelocity().getY() * deltaTime;
            p.setPosition(newPositionX, newPositionY);
        }
    }*/

//    public double getDensity() {
//        return particles.size() / Math.pow(areaBorderLength, 2);
//    }
//    
//    public double getTemperature() {
//    	return 1 / (double) particles.size() * particles.stream().mapToDouble(p -> p.getMass() * Math.pow(p.getVelocityModule(), 2)).sum()
//    			/ (2 * BOLTZMANN_CONSTANT);
//    }
    
    // Euler
    private void initPrevParticles() {
		for(Particle p : currParticles) {
			Particle prevParticle = p.clone();
			double prevPosition = p.getPosition().getX() - Configuration.getTimeStep() * p.getVelocity().getX()
					+ Math.pow(Configuration.getTimeStep(), 2) * getCurrentParticleForce(p) / (2 * p.getMass()); // + error
			prevParticle.setPosition(prevPosition, 0);
			prevParticles.add(prevParticle);
			System.out.println(Configuration.getTimeStep() * p.getVelocity().getX() + " " + 
					Math.pow(Configuration.getTimeStep(), 2) * getCurrentParticleForce(p) / (2 * p.getMass()));
		}
	}
    
    private void initNextParticles() {
    	for(Particle p : currParticles) {
    		nextParticles.add(p.clone());
    	}
    }
    
    public void verletUpdate() {
		for(int i = 0; i < currParticles.size(); i++) {
			Particle currParticle = currParticles.get(i);
			Particle prevParticle = prevParticles.get(i);
			
			double newPositionX = 2 * currParticle.getPosition().getX() - prevParticle.getPosition().getX()
					+ Math.pow(Configuration.getTimeStep(), 2) * getCurrentParticleForce(currParticle) / currParticle.getMass(); //+error
			
			double newVelocityX = (newPositionX - prevParticle.getPosition().getX()) / (2 * Configuration.getTimeStep()); // + error
			
			prevParticle.setPosition(currParticle.getPosition().getX(), 0);
			currParticle.setPosition(newPositionX, 0);
			currParticle.setVelocity(newVelocityX, 0);
			System.out.println("PREV: " + currParticle.getPosition().getX() + "; NEW: " + newPositionX);
			
			//nextParticles.get(i).setPosition(newPositionX, 0);
			// SACAR NEXT PARTICLES?
			
		}
	}
    
    private double getCurrentParticleForce(final Particle p) {
    	if(Configuration.isOscillatorMode()) {
			return - Configuration.OSCILLATOR_K * p.getPosition().getX() - Configuration.OSCILLATOR_GAMMA * p.getVelocity().getX();
		} else {
			//
			return 0;
		}
    }

	public List<Particle> getCurrentParticles() {
        return Collections.unmodifiableList(currParticles);
    }
    
    public List<Particle> getPreviousParticles() {
        return Collections.unmodifiableList(prevParticles);
    }
    
    public List<Particle> getNextParticles() {
        return Collections.unmodifiableList(nextParticles);
    }

    public double getAreaBorderLength() {
        return areaBorderLength;
    }

}
