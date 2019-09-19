package ar.edu.itba.ss.tpe4;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Main {

    private static long startTime;

    private Main() {

    }

    public static void main(String[] args) {
        executeSingleRun();
        long endTime = System.nanoTime();
        System.out.println("Process done in " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms.");
    }

    private static void executeSingleRun() {
        Configuration.requestParameters();
        startTime = System.nanoTime();
        List<Particle> particles = Configuration.generateRandomInputFilesAndParseConfiguration();
        Grid grid = new Grid(particles);
        CollisionManager cm = new CollisionManager(grid);
        cm.executeAlgorithm();
        System.out.println("Temperature: " + grid.getTemperature());
    }

}
