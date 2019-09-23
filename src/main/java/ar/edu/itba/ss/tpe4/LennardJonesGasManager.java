package ar.edu.itba.ss.tpe4;

import java.util.HashMap;
import java.util.List;

public class LennardJonesGasManager {
	
	private final Grid grid;
	private double balanceTime;
	private HashMap<Particle, Double> positionMap;

	public LennardJonesGasManager(Grid grid) {
		this.grid = grid;
		this.balanceTime = 0;
		this.positionMap = new HashMap<>();

		for (Particle particle: grid.getParticles()) {
			this.positionMap.put(particle, particle.getPosition().x);
		}
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

	public void execute() {
		double accumulatedTime = 0.0;

		if (Configuration.getTimeLimit() == -1)
		while(Double.compare(accumulatedTime, getTimeLimit()) <= 0) {
			Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());

			if (balanceTime == 0 && isBalanced()) {
				balanceTime = accumulatedTime;
			}

			for (Particle particle: grid.getParticles()) {
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

			accumulatedTime += Configuration.getTimeStep();
			// switch(Configuration.getIntegrator()) {
			// case VERLET:
			// 	grid.verletUpdate();
			// 	break;
			// case GEAR_PREDICTOR_CORRECTOR:
			// 	break;
			// case BEEMAN:
			// 	grid.beemanUpdate();
			// 	break;
			// }
		}
	}

}
