package ar.edu.itba.ss.tpe4;

public abstract class Collision {

    protected final Particle particle;
    protected Double time;

    public Collision(final Particle particle) {
        this.particle = particle;
    }

    public abstract void updateTime();

    public void updateTime(final double deltaTime) {
        time -= deltaTime;
    }

    public Particle getParticle() {
        return particle;
    }

    public Double getTime() {
        return time;
    }

}
