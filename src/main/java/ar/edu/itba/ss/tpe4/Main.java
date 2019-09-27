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
    	} else {
    		executeGasTestRuns();
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
    
    private static void executeGasTestRuns() {
        startTime = System.nanoTime();
        double holeSize = Configuration.GAS_TEST_INIT_HOLE_SIZE;
        for(int i = 0; i < 7; i++) {
        	double average = 0;
        	Configuration.GAS_BOX_HOLE_SIZE = holeSize;
        	Configuration.GAS_BOX_HOLE_POSITION = 100 - holeSize / 2;
        	for(int j = 0; j < Configuration.GAS_TEST_CYCLES; j++) {
        		List<Particle> particles = Configuration.generateRandomInputFilesAndParseConfiguration();
                Grid grid = new Grid(particles);
                LennardJonesGasManager gm = new LennardJonesGasManager(grid);
                average += gm.execute();
        	}
        	System.out.println("Hole Size: " + Configuration.GAS_BOX_HOLE_SIZE + "; Average Balance Time: " 
        	+ (average / Configuration.GAS_TEST_CYCLES));
        	holeSize += 10;
        }
    }

}
