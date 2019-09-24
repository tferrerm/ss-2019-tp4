package ar.edu.itba.ss.tpe4;

import java.util.ArrayList;
import java.util.List;

public class OscillatorManager {
	
	private final Grid grid;
	private final double timeStep;

	public OscillatorManager(Grid grid) {
		this.grid = grid;
		this.timeStep = Configuration.getTimeStep();
	}
	
	public void execute() {
		List<Particle> previousParticles = initPreviousParticles(grid.getParticles());
		double accumulatedTime = 0.0;
		while(Double.compare(accumulatedTime, Configuration.getTimeLimit()) <= 0) {
			Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());
			
			accumulatedTime += Configuration.getTimeStep();
			switch(Configuration.getIntegrator()) {
			case VERLET:
				verletUpdate(previousParticles);
				break;
			case GEAR_PREDICTOR_CORRECTOR:
				gearUpdate();
				break;
			case BEEMAN:
				beemanUpdate(previousParticles);
				break;
			}
		}
	}
	
		public void verletUpdate(List<Particle> previousParticles) {
    	List<Particle> currentParticles = grid.getParticles();
    	
			for(int i = 0; i < currentParticles.size(); i++) {
				Particle currParticle = currentParticles.get(i);
				Particle prevParticle = previousParticles.get(i);
				
				double newPositionX = 2 * currParticle.getPosition().getX() - prevParticle.getPosition().getX()
						+ Math.pow(Configuration.getTimeStep(), 2) * getAcceleration(currParticle);
				double newVelocityX = (newPositionX - prevParticle.getPosition().getX()) / (2 * Configuration.getTimeStep());
				
				prevParticle.setPosition(currParticle.getPosition().getX(), 0);
				prevParticle.setVelocity(currParticle.getVelocity().getX(), 0);
				currParticle.setPosition(newPositionX, 0);
				currParticle.setVelocity(newVelocityX, 0);
			}
		}
    
    public void beemanUpdate(List<Particle> previousParticles) {
    	List<Particle> currentParticles = grid.getParticles();
    	
    	for(int i = 0; i < currentParticles.size(); i++) {
			Particle currParticle = currentParticles.get(i);
			Particle prevParticle = previousParticles.get(i);
			
			double newPositionX = currParticle.getPosition().getX() + currParticle.getVelocity().getX() * timeStep
					+ (2 / (double) 3) * getAcceleration(currParticle) * Math.pow(timeStep, 2)
					- (1 / (double) 6) * getAcceleration(prevParticle) * Math.pow(timeStep, 2);
			
			double predictedVelocityX = currParticle.getVelocity().getX() 
					+ (3 / (double) 2) * getAcceleration(currParticle) * timeStep
					- (1 / (double) 2) * getAcceleration(prevParticle) * timeStep;
			Particle predictedParticle = currParticle.clone();
			predictedParticle.setPosition(newPositionX, 0);
			predictedParticle.setVelocity(predictedVelocityX, 0);
			
			double newAccelerationX = getAcceleration(newPositionX, predictedVelocityX, currParticle.getMass());
			double correctedVelocityX = currParticle.getVelocity().getX()
					+ (1 / (double) 3) * newAccelerationX * timeStep + (5 / (double) 6) * getAcceleration(currParticle) * timeStep
					- (1 / (double) 6) * getAcceleration(prevParticle) * timeStep;
			
			prevParticle.setPosition(currParticle.getPosition().getX(), 0);
			prevParticle.setVelocity(currParticle.getVelocity().getX(), 0);
			currParticle.setPosition(newPositionX, 0);
			currParticle.setVelocity(correctedVelocityX, 0);
		}
	}

	public void gearUpdate() {
    	List<Particle> currentParticles = grid.getParticles();
    	
    	for(int i = 0; i < currentParticles.size(); i++) {
			GearParticle currParticle = (GearParticle) currentParticles.get(i);
			//Particle prevParticle = prevParticles.get(i);
			
			//List<Double> currParticleR = initGearRList(currParticle);
			double predictedR = currParticle.getPosition().getX() + currParticle.getVelocity().getX() * timeStep
					+ currParticle.getR2() * Math.pow(timeStep, 2) / 2
					+ currParticle.getR3() * Math.pow(timeStep, 3) / 6
					+ currParticle.getR4() * Math.pow(timeStep, 4) / 24
					+ currParticle.getR5() * Math.pow(timeStep, 5) / 120;
			double predictedR1 = currParticle.getVelocity().getX() + currParticle.getR2() * timeStep
					+ currParticle.getR3() * Math.pow(timeStep, 2) / 2
					+ currParticle.getR4() * Math.pow(timeStep, 3) / 6
					+ currParticle.getR5() * Math.pow(timeStep, 4) / 24;
			double predictedR2 = currParticle.getR2() + currParticle.getR3() * timeStep
					+ currParticle.getR4() * Math.pow(timeStep, 2) / 2
					+ currParticle.getR5() * Math.pow(timeStep, 3) / 6;
			double predictedR3 = currParticle.getR3() + currParticle.getR4() * timeStep + currParticle.getR5() * Math.pow(timeStep, 2) / 2;
			double predictedR4 = currParticle.getR4() + currParticle.getR5() * timeStep;
			double predictedR5 = currParticle.getR5();
			
			double newAccelerationX = getAcceleration(predictedR, predictedR1, currParticle.getMass());
			double deltaAcceleration = newAccelerationX - predictedR2;
			double deltaR2 = deltaAcceleration * Math.pow(timeStep, 2) / 2;
		
			double correctedPositionX = predictedR + (3 / (double) 16) * deltaR2;
			double correctedVelocityX = predictedR1 + (251 / (double) 360) * deltaR2 / timeStep;
			double correctedR2 = predictedR2 + deltaR2 * 2 / Math.pow(timeStep, 2);
			double correctedR3 = predictedR3 + (11 / (double) 18) * deltaR2 * 6 / Math.pow(timeStep, 3);
			double correctedR4 = predictedR4 + (1 / (double) 6) * deltaR2 * 24 / Math.pow(timeStep, 4);
			double correctedR5 = predictedR5 + (1 / (double) 60) * deltaR2 * 120 / Math.pow(timeStep, 5);
			
			currParticle.setPosition(correctedPositionX, 0);
			currParticle.setVelocity(correctedVelocityX, 0);
			currParticle.setR2(correctedR2);
			currParticle.setR3(correctedR3);
			currParticle.setR4(correctedR4);
			currParticle.setR5(correctedR5);
		}
    }

//    private List<Double> initGearRList(Particle p) {
//    	List<Double> res = new ArrayList<>();
//    	res.add(p.getPosition().getX());
//    	res.add(p.getVelocity().getX());
//    	res.add(getAcceleration(p));
//		res.add(getR3(p));
//		res.add(getR4(p));
//		res.add(getR5(p));
//		return res;
//    }
    
    private double getAcceleration(final Particle p) {
    	return getParticleForce(p) / p.getMass();
    }
    
    private double getAcceleration(final double position, final double velocity, final double mass) {
    	return getParticleForce(position, velocity) / mass;
	}

//	private double getR3(Particle p) {
//    	return - (Configuration.OSCILLATOR_K / p.getMass()) * p.getVelocity().getX()
//    			- (Configuration.OSCILLATOR_GAMMA / p.getMass()) * getAcceleration(p);
//    }
//    
//    private double getR4(Particle p) {
//    	return - (Configuration.OSCILLATOR_K / p.getMass()) * getAcceleration(p)
//    			- (Configuration.OSCILLATOR_GAMMA / p.getMass()) * getR3(p);
//    }
//    
//    private double getR5(Particle p) {
//    	return - (Configuration.OSCILLATOR_K / p.getMass()) * getR3(p)
//    			- (Configuration.OSCILLATOR_GAMMA / p.getMass()) * getR4(p);
//    }
    
    private double getParticleForce(final Particle p) {
    	return - Configuration.OSCILLATOR_K * p.getPosition().getX() - Configuration.OSCILLATOR_GAMMA * p.getVelocity().getX();
    }
    
    private double getParticleForce(final double position, final double velocity) {
    	return - Configuration.OSCILLATOR_K * position - Configuration.OSCILLATOR_GAMMA * velocity;
	}
    
    // Euler Algorithm evaluated in (- timeStep)
    private List<Particle> initPreviousParticles(List<Particle> currentParticles) {
    	List<Particle> previousParticles = new ArrayList<>();
		for(Particle p : currentParticles) {
			Particle prevParticle = p.clone();
			double prevPosition = p.getPosition().getX() - Configuration.getTimeStep() * p.getVelocity().getX()
					+ Math.pow(Configuration.getTimeStep(), 2) * getParticleForce(p) / (2 * p.getMass());
			double prevVelocity = p.getVelocity().getX() - (timeStep / p.getMass()) * getParticleForce(p);
			prevParticle.setPosition(prevPosition, 0);
			prevParticle.setVelocity(prevVelocity, 0);
			previousParticles.add(prevParticle);
		}
		
		return previousParticles;
	}

}
