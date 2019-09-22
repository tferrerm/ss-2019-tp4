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

	public List<Particle> getParticles() {
        return Collections.unmodifiableList(particles);
    }
	
	public void setParticles(List<Particle> particles) {
		this.particles = particles;
	}

    public double getAreaBorderLength() {
        return areaBorderLength;
    }

}
