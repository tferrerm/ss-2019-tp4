package ar.edu.itba.ss.tpe4;

public class OscillatorManager {
	
	private final Particle particle;

	public OscillatorManager(Grid grid) {
		this.particle = grid.getParticles().get(0);
	}

}
