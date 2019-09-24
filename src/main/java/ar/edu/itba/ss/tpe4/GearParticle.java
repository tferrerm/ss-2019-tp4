package ar.edu.itba.ss.tpe4;

import java.awt.geom.Point2D.Double;

public final class GearParticle extends Particle {

	private double r2;
	private double r3;
	private double r4;
	private double r5;
	
	public GearParticle(double radius, double mass, double x, double y,
			double vx, double vy) {
		super(radius, mass, x, y, vx, vy);
		this.r2 = - (Configuration.OSCILLATOR_K / mass) * x;
		this.r3 = - (Configuration.OSCILLATOR_K / mass) * vx;
		this.r4 = Math.pow(Configuration.OSCILLATOR_K / mass, 2) * x;
		this.r5 = Math.pow(Configuration.OSCILLATOR_K / mass, 2) * vx;
	}
	public double getR2() {
		return r2;
	}
	
	public void setR2(double r2) {
		this.r2 = r2;
	}
	
	public double getR3() {
		return r3;
	}
	
	public void setR3(double r3) {
		this.r3 = r3;
	}
	
	public double getR4() {
		return r4;
	}
	
	public void setR4(double r4) {
		this.r4 = r4;
	}
	
	public double getR5() {
		return r5;
	}
	
	public void setR5(double r5) {
		this.r5 = r5;
	}
	
	

}
