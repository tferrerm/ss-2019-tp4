package ar.edu.itba.ss.tpe4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.geom.Point2D;

public class LennardJonesGasManager {
	
	private final Grid grid;
	private double balanceTime;
	private HashMap<Particle, Double> positionMap;

	public LennardJonesGasManager(Grid grid) {
		this.grid = grid;
		this.balanceTime = 0.0;
		this.positionMap = new HashMap<>();

		for (Particle particle: grid.getParticles()) {
			this.positionMap.put(particle, particle.getPosition().x);
		}
	}

	private double getParticleForce(double distance) {
		double coefficient = Configuration.GAS_L * Configuration.GAS_EPSILON / Configuration.GAS_Rm;
		double repulsion = Math.pow((Configuration.GAS_Rm / distance), Configuration.GAS_L + 1);
		double attraction =Math.pow((Configuration.GAS_Rm / distance), Configuration.GAS_J + 1);
		return - coefficient * (repulsion - attraction);
	}

	private Boolean isBalanced() {
		List<Particle> particles = grid.getParticles();
		Integer initialChamberAmount = 0;
		for (Particle particle: particles) {
			if (isInFirstChamber(particle)) {
				initialChamberAmount += 1;
			}
		}
		return initialChamberAmount == particles.size() / 2;
	}

	private double getTimeLimit() {
		double timeLimit = Integer.MAX_VALUE;
		
		switch(Configuration.getTimeLimit()) {
			case -1:
				timeLimit = this.balanceTime > 0 ? this.balanceTime : Integer.MAX_VALUE;
				break;
			case -2:
				timeLimit = this.balanceTime > 0 ? this.balanceTime * 2 : Integer.MAX_VALUE;
			default:
				timeLimit = Configuration.getTimeLimit();
		}

		return timeLimit;
	}

	public Boolean isInFirstChamber(Particle particle) {
		return particle.getPosition().x < Configuration.GAS_BOX_SPLIT;
	}

	public void updatePositionByBouncing(List<Particle> particles) {
		for (Particle particle: particles) {
			Boolean isOutsideTopBound = particle.getPosition().y >= Configuration.GAS_BOX_HEIGHT;
			Boolean isOutsideBottomBound = particle.getPosition().y <= 0;
			Boolean isOutsideRightBound = particle.getPosition().x >= Configuration.GAS_BOX_WIDTH;
			Boolean isOutsideLeftBound = particle.getPosition().x <= 0;
			Boolean isWithinHole = particle.getPosition().y >= Configuration.GAS_BOX_HOLE_POSITION && particle.getPosition().y <= Configuration.GAS_BOX_HOLE_POSITION + Configuration.GAS_BOX_HOLE_SIZE;

			if (isOutsideTopBound || isOutsideBottomBound) {
				particle.setVelocity(particle.getVelocity().x, -particle.getVelocity().y);
			}

			if (isOutsideLeftBound || isOutsideRightBound) {
				particle.setVelocity(-particle.getVelocity().x, particle.getVelocity().y);
			}

			if (!isWithinHole) {
				double lastX = this.positionMap.get(particle);
				Boolean changedChamber = !isInFirstChamber(particle) && lastX < Configuration.GAS_BOX_SPLIT || isInFirstChamber(particle) && lastX > Configuration.GAS_BOX_SPLIT;
				if (changedChamber) {
					particle.setVelocity(-particle.getVelocity().x, particle.getVelocity().y);
					continue;
				} 
			}

			this.positionMap.put(particle, particle.getPosition().x);
		}
	}

	public double distance(Particle a, Particle b) {
		return Math.sqrt(Math.pow(b.getPosition().x - a.getPosition().x, 2) + Math.pow(b.getPosition().y - a.getPosition().y, 2));
	}

	public List<Particle> getClosestParticles(Particle particle) {
		List<Particle> particles = grid.getParticles();
		// TODO: use cellindex method
		return particles.stream().filter(p -> distance(particle, p) > 0 && distance(particle, p) <= Configuration.GAS_RANGE && isInFirstChamber(particle) == isInFirstChamber(p)).collect(Collectors.toList());
	}

	private Point2D.Double getAppliedForce (Particle particle) {
		List<Particle> closeParticles = getClosestParticles(particle);
		double totalForceX = 0.0;
		double totalForceY = 0.0;

		for (Particle p: closeParticles) {
			double forceModulus = getParticleForce(distance(p, particle));
			double forceAngle = Math.atan2(p.getPosition().y - particle.getPosition().y, p.getPosition().x - particle.getPosition().x);
			totalForceX += Math.cos(forceAngle) * forceModulus;
			totalForceY += Math.sin(forceAngle) * forceModulus;
		}

		return new Point2D.Double(totalForceX, totalForceY);
	}

	private Point2D.Double getAppliedAcceleration (Particle particle) {
		Point2D.Double force = getAppliedForce(particle);
		return new Point2D.Double(force.x / particle.getMass(), force.y / particle.getMass());
	}

	public void verletUpdate(List<Particle> previousParticles) {
		List<Particle> currentParticles = grid.getParticles(); 
		
		for(int i = 0; i < currentParticles.size(); i++) {
			Particle currParticle = currentParticles.get(i);
			Particle prevParticle = previousParticles.get(i);
			Point2D.Double acceleration  = getAppliedAcceleration(currParticle);
			double newPositionX = 2 * currParticle.getPosition().getX() - prevParticle.getPosition().getX()
					+ Math.pow(Configuration.getTimeStep(), 2) * acceleration.x; //+error
			double newPositionY = 2 * currParticle.getPosition().getY() - prevParticle.getPosition().getY()
					+ Math.pow(Configuration.getTimeStep(), 2) * acceleration.y; //+error
			double newVelocityX = (newPositionX - prevParticle.getPosition().getX()) / (2 * Configuration.getTimeStep()); // + error
			double newVelocityY = (newPositionY - prevParticle.getPosition().getY()) / (2 * Configuration.getTimeStep()); // + error
			
			prevParticle.setPosition(currParticle.getPosition().getX(), currParticle.getPosition().getY());
			prevParticle.setVelocity(currParticle.getVelocity().getX(), currParticle.getPosition().getY());
			currParticle.setPosition(newPositionX, newPositionY);
			currParticle.setVelocity(newVelocityX, newVelocityY);
		}
	}

	    // Euler Algorithm evaluated in (- timeStep)
		private List<Particle> initPreviousParticles(List<Particle> currentParticles) {
			List<Particle> previousParticles = new ArrayList<>();
			for(Particle p : currentParticles) {
				Particle prevParticle = p.clone();
				Point2D.Double force = getAppliedForce(p);
				double prevPositionX = p.getPosition().getX() - Configuration.getTimeStep() * p.getVelocity().getX()
						+ Math.pow(Configuration.getTimeStep(), 2) * force.x / (2 * p.getMass()); // + error
				double prevPositionY = p.getPosition().getY() - Configuration.getTimeStep() * p.getVelocity().getY()
						+ Math.pow(Configuration.getTimeStep(), 2) * force.y / (2 * p.getMass()); // + error
				double prevVelocityX = p.getVelocity().getX() - (Configuration.getTimeStep() / p.getMass()) * force.x;// + error
				double prevVelocityY = p.getVelocity().getX() - (Configuration.getTimeStep() / p.getMass()) * force.y;// + error
				prevParticle.setPosition(prevPositionX, prevPositionY);
				prevParticle.setVelocity(prevVelocityX, prevVelocityY);
				previousParticles.add(prevParticle);
			}
			
			return previousParticles;
		}

	public void execute() {
		double accumulatedTime = 0.0;
		List<Particle> previousParticles = initPreviousParticles(grid.getParticles());

		if (Configuration.getTimeLimit() == -1)
		while(Double.compare(accumulatedTime, getTimeLimit()) <= 0) {
			Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());

			// get balance time
			if (balanceTime == 0 && isBalanced()) {
				balanceTime = accumulatedTime;
			}

			// update position if the particles bounce
			updatePositionByBouncing(grid.getParticles());

			// increase time by dt
			accumulatedTime += Configuration.getTimeStep();

			// update position and velocity
			verletUpdate(previousParticles);
		}
	}

}
