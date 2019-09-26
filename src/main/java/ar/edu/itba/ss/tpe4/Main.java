package ar.edu.itba.ss.tpe4;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Main {

    private static long startTime;

    private Main() {}

    public static void main(String[] args) {
        Mode request = Configuration.requestMode();
    	if(request == Mode.OSCILLATOR) {
    		executeOscillatorRun();
    	} else if (request == Mode.LENNARD_JONES_GAS) {
            executeGasRun();
    	}
        long endTime = System.nanoTime();
        System.out.println("Process done in " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms.");
    }

    private static void executeOscillatorRun() {
        startTime = System.nanoTime();
        List<Particle> particles = Configuration.generateRandomInputFilesAndParseConfiguration();
        Grid grid = new Grid(particles);
        OscillatorManager om = new OscillatorManager(grid);
        om.execute();
    }

    private static void executeGasRun() {
        startTime = System.nanoTime();
        List<Particle> particles = Configuration.generateRandomInputFilesAndParseConfiguration();
        Grid grid = new Grid(particles);
        LennardJonesGasManager gm = new LennardJonesGasManager(grid);
        gm.execute();
    }

}
