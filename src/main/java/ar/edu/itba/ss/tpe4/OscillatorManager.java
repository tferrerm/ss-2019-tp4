package ar.edu.itba.ss.tpe4;

public class OscillatorManager {
	
	private final Grid grid;

	public OscillatorManager(Grid grid) {
		this.grid = grid;
	}
	
	public void execute() {
		double accumulatedTime = 0.0;
		while(Double.compare(accumulatedTime, Configuration.getTimeLimit()) <= 0) {
			Configuration.writeOvitoOutputFile(accumulatedTime, grid.getCurrentParticles());
			
			accumulatedTime += Configuration.getTimeStep();
			switch(Configuration.getIntegrator()) {
			case VERLET:
				grid.verletUpdate();
				break;
			case GEAR_PREDICTOR_CORRECTOR:
				break;
			case BEEMAN:
				break;
			}
		}
	}

}
