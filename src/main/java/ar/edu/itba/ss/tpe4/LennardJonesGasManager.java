package ar.edu.itba.ss.tpe4;

import java.util.List;

public class LennardJonesGasManager {
	
	private final Grid grid;
	private double balanceTime;

	public LennardJonesGasManager(Grid grid) {
		this.grid = grid;
		this.balanceTime = 0;
	}

	private Boolean isBalanced() {
		List<Particle> particles = grid.getCurrentParticles();
		Integer initialChamberAmount = 0;
		for (Particle particle: particles) {
			if (particle.getPosition().x < (Configuration.GAS_BOX_WIDTH - Configuration.GAS_BOX_SPLIT)) {
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
	
	public void execute() {
		double accumulatedTime = 0.0;

		if (Configuration.getTimeLimit() == -1)
		while(Double.compare(accumulatedTime, getTimeLimit()) <= 0) {
			Configuration.writeOvitoOutputFile(accumulatedTime, grid.getCurrentParticles());

			if (balanceTime == 0 && isBalanced()) {
				balanceTime = accumulatedTime;
			}
			
			accumulatedTime += Configuration.getTimeStep();
			switch(Configuration.getIntegrator()) {
			case VERLET:
				grid.verletUpdate();
				break;
			case GEAR_PREDICTOR_CORRECTOR:
				break;
			case BEEMAN:
				grid.beemanUpdate();
				break;
			}
		}
	}

}
