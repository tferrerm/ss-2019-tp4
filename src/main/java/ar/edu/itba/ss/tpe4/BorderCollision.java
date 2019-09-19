package ar.edu.itba.ss.tpe4;

public final class BorderCollision extends Collision {

    private final Border border;
    private final double gridDimension;

    public BorderCollision(final Particle particle, final Border border, final double gridDimension) {
        super(particle);
        this.border = border;
        this.gridDimension = gridDimension;
        updateTime();
    }

    public void updateTime() {
        double positionComponent = border.equals(Border.VERTICAL) ? particle.getPosition().getX() : particle.getPosition().getY();
        double velocityComponent = border.equals(Border.VERTICAL) ? particle.getVelocity().getX() : particle.getVelocity().getY();
        int compare = border.equals(Border.VERTICAL) ? Double.compare(particle.getVelocity().getX(), 0.0)
                : Double.compare(particle.getVelocity().getY(), 0.0);
        time = ((compare == 0)?  Double.POSITIVE_INFINITY
        		: (compare > 0)? (gridDimension - particle.getRadius() - positionComponent) / velocityComponent
                : (particle.getRadius() - positionComponent) / velocityComponent
        );
    }

    public Border getBorder() {
        return border;
    }

}
