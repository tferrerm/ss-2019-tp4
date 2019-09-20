package ar.edu.itba.ss.tpe4;

import java.util.Arrays;
import java.util.Optional;

public enum Integrator {
	
	VERLET (0),
	GEAR_PREDICTOR_CORRECTOR (1),
	BEEMAN (2);
	
	private int integrator;
	
	Integrator(final int integrator) {
		this.integrator = integrator;
	}
	
	public int getIntegrator() {
		return integrator;
	}
	
	public static Optional<Integrator> valueOf(final int value) {
		return Arrays.stream(values()).filter(m -> m.getIntegrator() == value).findFirst();
	}

}
