package ar.edu.itba.ss.tpe4;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Main {

    private static long startTime;

    private Main() {

    }

    public static void main(String[] args) {
    	if(Configuration.requestMode() == Mode.OSCILLATOR) {
    		executeOscillatorRun();
    	} else {
    		//
    	}
        long endTime = System.nanoTime();
        System.out.println("Process done in " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms.");
    }

    private static void executeOscillatorRun() {
        Configuration.requestParameters();
        startTime = System.nanoTime();
        List<Particle> particles = Configuration.generateRandomInputFilesAndParseConfiguration();
        Grid grid = new Grid(particles);
        OscillatorManager om = new OscillatorManager(grid);
        om.execute();
    }

}
