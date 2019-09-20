package ar.edu.itba.ss.tpe4;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class Configuration {
    private static String inputFileName = "config.txt";
    private static Mode mode;
    private static Integrator integrator;
    private static int particleCount;
    private static double timeStep; 
    private static int timeLimit;
    
    private static final int OSCILLATOR_PARTICLE_COUNT = 1;
    private static final int GAS_PARTICLE_COUNT = 300;

    private Configuration() {

    }
    
    public static Mode requestMode() {
    	@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
    	
    	System.out.println("Enter Mode [0 -> Oscillator; 1 -> Lennard-Jones Gas]: ");
        Integer selectedMode = null;
        while (selectedMode == null || selectedMode < 0 || selectedMode > 1) {
        	selectedMode = stringToInt(scanner.nextLine());
        }
        mode = Mode.valueOf(selectedMode).get();
        
        if(mode == Mode.OSCILLATOR)
        	particleCount = OSCILLATOR_PARTICLE_COUNT;
        else
        	particleCount = GAS_PARTICLE_COUNT;
        return mode;
    }

    public static void requestParameters() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Enter Integrator [0 -> Verlet; 1 -> Gear Predictor-Corrector; 2 -> Beeman]: ");
        Integer selectedIntegrator = null;
        while (selectedIntegrator == null || selectedIntegrator < 0 || selectedIntegrator > 2) {
        	selectedIntegrator = stringToInt(scanner.nextLine());
        }
        integrator = Integrator.valueOf(selectedIntegrator).get();
        
        System.out.println("Enter Time Step:");
        Double selectedTimeStep = null;
        while(selectedTimeStep == null || selectedTimeStep <= 0) {
        	selectedTimeStep = stringToDouble(scanner.nextLine());
        }
        timeStep = selectedTimeStep;

        System.out.println("Enter Time Limit:");
        Integer selectedTimeLimit = null;
        while(selectedTimeLimit == null || selectedTimeLimit <= 0) {
            selectedTimeLimit = stringToInt(scanner.nextLine());
        }
        timeLimit = selectedTimeLimit;

        scanner.close();
    }

    /* Parameters must have already been requested */
    public static List<Particle> generateRandomInputFilesAndParseConfiguration() {
        generateRandomInputFile();
        List<Particle> particles = parseConfiguration();
        generateOvitoOutputFile();
        return particles;
    }

    private static List<Particle> parseConfiguration() {
        List<Particle> particles = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            /* Time (0) */
            br.readLine();

            /* Big Particle Properties */
            String line = br.readLine();
            if(line == null)
                failWithMessage("Particles do not match particle count.");
            String[] attributes = line.split(" ");
            attributes = removeSpaces(attributes);
            setParticleProperties(particles, attributes, true);

            /* Small Particle Properties */
            for(int i = 0; i < smallParticleCount; i++) {
                line = br.readLine();
                if(line == null)
                    failWithMessage("Particles do not match particle count.");
                attributes = line.split(" ");
                attributes = removeSpaces(attributes);
                setParticleProperties(particles, attributes, false);
                // ADD PARTICLE
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return particles;
    }

    private static String[] removeSpaces(final String[] array) {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        List<String> filteredList = list.stream().filter(s -> !s.equals("") && !s.equals(" ")).collect(Collectors.toList());
        String[] newArray = new String[filteredList.size()];
        return filteredList.toArray(newArray);
    }

    private static void setParticleProperties(final List<Particle> particles, final String[] attributes, final boolean isBigParticle) {
        final int propertyCount = 4;
        Double x = null;
        Double y = null;
        Double vx = null;
        Double vy = null;
        if(attributes.length != propertyCount || (x = stringToDouble(attributes[0])) == null || (y = stringToDouble(attributes[1])) == null
                || (vx = stringToDouble(attributes[2])) == null || (vy = stringToDouble(attributes[3])) == null) {
            failWithMessage(attributes[0] + ", " + attributes[1] + ", " + attributes[2] + ", " + attributes[3] + " are invalid attributes.");
        }

        if(isBigParticle)
            particles.add(new Particle(BIG_PARTICLE_RADIUS, BIG_PARTICLE_MASS, x, y, vx, vy));
        else
            particles.add(new Particle(SMALL_PARTICLE_RADIUS, SMALL_PARTICLE_MASS, x, y, vx, vy));
    }

    private static Integer stringToInt(final String s) {
        Integer i = null;
        try {
            i = Integer.valueOf(s);
        } catch(NumberFormatException e) {
            return null;
        }
        return i;
    }

    private static Double stringToDouble(final String s) {
        Double d = null;
        try {
            d = Double.valueOf(s);
        } catch(NumberFormatException e) {
            return null;
        }
        return d;
    }

    private static void failWithMessage(final String message) {
        System.err.println(message);
        System.exit(1);
    }

    /* Time (0) - Big Particle Properties - Small Particles Properties */
    private static List<Particle> generateRandomInputFile() {
        List<Particle> particles = new ArrayList<>();
        File inputFile = new File(inputFileName);
        inputFile.delete();
        try(FileWriter fw = new FileWriter(inputFile)) {
            inputFile.createNewFile();
            fw.write("0\n");

            particles.add(new Particle(BIG_PARTICLE_RADIUS, new Point2D.Double(BIG_PARTICLE_INIT_POSITION.getX(), BIG_PARTICLE_INIT_POSITION.getY())));
            fw.write(BIG_PARTICLE_INIT_POSITION.getX() + " " + BIG_PARTICLE_INIT_POSITION.getY()
                    + " " + BIG_PARTICLE_INIT_VELOCITY + " " + BIG_PARTICLE_INIT_VELOCITY + "\n");

            Random r = new Random();
            for(int i = 0; i < smallParticleCount; i++) {
                double randomPositionX = 0;
                double randomPositionY = 0;
                boolean isValidPosition = false;

                while(!isValidPosition) {
                    randomPositionX = (AREA_BORDER_LENGTH - 2 * SMALL_PARTICLE_RADIUS) * r.nextDouble() + SMALL_PARTICLE_RADIUS;
                    randomPositionY = (AREA_BORDER_LENGTH - 2 * SMALL_PARTICLE_RADIUS) * r.nextDouble() + SMALL_PARTICLE_RADIUS;
                    isValidPosition = validateParticlePosition(particles, randomPositionX, randomPositionY, SMALL_PARTICLE_RADIUS);
                }

                double randomVelocity = smallParticleMaxVelocity * r.nextDouble();
                double angle = 2 * Math.PI * r.nextDouble();
                double randomVelocityX = Math.cos(angle) * randomVelocity;
                double randomVelocityY = Math.sin(angle) * randomVelocity;

                particles.add(new Particle(SMALL_PARTICLE_RADIUS, new Point2D.Double(randomPositionX, randomPositionY)));
                fw.write(randomPositionX + " " + randomPositionY + " " + randomVelocityX + " " + randomVelocityY + "\n");
            }
        } catch (IOException e) {
            System.err.println("Failed to create dynamic input file.");
            e.printStackTrace();
        }

        return particles;
    }

    private static boolean validateParticlePosition(final List<Particle> particles, final double randomPositionX, final double randomPositionY, final double radius) {
        if(particles.isEmpty())
            return true;
        for(Particle p : particles) {
            if(Math.sqrt(Math.pow(p.getPosition().getX() - randomPositionX, 2) + Math.pow(p.getPosition().getY() - randomPositionY, 2))
                    < (p.getRadius() + radius))
                return false;
        }
        return true;
    }

    private static void generateOvitoOutputFile() {
        File outputFile = new File("./ovito_output.xyz");
        outputFile.delete();
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create Ovito output file.");
            e.printStackTrace();
        }
    }

    public static double writeOvitoOutputFile(final double accumulatedTime, double deltaTime, double nextFrameTime, final List<Particle> particles) {
        File outputFile = new File("ovito_output.xyz");

        while(Double.compare(nextFrameTime, accumulatedTime) <= 0) {
            try(FileWriter fw = new FileWriter(outputFile, true)) {
                fw.write((smallParticleCount + 1) + "\n");
                fw.write("Lattice=\"" + AREA_BORDER_LENGTH * 2 + " 0.0 0.0 0.0 " + AREA_BORDER_LENGTH * 2 + " 0.0 0.0 0.0 "
                        + AREA_BORDER_LENGTH * 2 + "\" Properties=id:I:1:radius:R:1:mass:R:1:pos:R:2:velo:R:2 Time=" + nextFrameTime + "\n");
                for(Particle p : particles) {
                    writeOvitoParticle(fw, p, deltaTime);
                }
            } catch (IOException e) {
                System.err.println("Failed to write Ovito output file.");
                e.printStackTrace();
            }
            nextFrameTime += FIXED_INTERVAL;
            deltaTime += FIXED_INTERVAL;
        }
        return nextFrameTime;
    }

    private static void writeOvitoParticle(final FileWriter fw, final Particle particle, final double deltaTime)
            throws IOException {
        Point2D.Double newPosition = new Point2D.Double(
                particle.getPosition().getX() + particle.getVelocity().getX() * deltaTime,
                particle.getPosition().getY() + particle.getVelocity().getY() * deltaTime);

        fw.write(particle.getId() + " " + particle.getRadius() + " " + particle.getMass() + " " + newPosition.getX() + " "
                + newPosition.getY() + " " + particle.getVelocity().getX() + " " + particle.getVelocity().getY());
        fw.write('\n');
    }

    public static void writeOvitoOutputFile(final Double time, final List<Particle> particles) {
        File outputFile = new File("ovito_output.xyz");
        try(FileWriter fw = new FileWriter(outputFile, true)) {
            fw.write((smallParticleCount + 1) + "\n");
            fw.write("Lattice=\"" + AREA_BORDER_LENGTH * 2 + " 0.0 0.0 0.0 " + AREA_BORDER_LENGTH * 2 + " 0.0 0.0 0.0 "
                    + AREA_BORDER_LENGTH * 2 + "\" Properties=id:I:1:radius:R:1:mass:R:1:pos:R:2:velo:R:2 Time=" + time + "\n");
            for(Particle p : particles) {
                writeOvitoParticle(fw, p);
            }
        } catch (IOException e) {
            System.err.println("Failed to write Ovito output file.");
            e.printStackTrace();
        }
    }

    private static void writeOvitoParticle(final FileWriter fw, final Particle particle) throws IOException {
        fw.write(particle.getId() + " " + particle.getRadius() + " " + particle.getMass() + " " + particle.getPosition().getX() + " "
                + particle.getPosition().getY() + " " + particle.getVelocity().getX() + " " + particle.getVelocity().getY());
        fw.write('\n');
    }

    public static int getSmallParticleCount() {
        return smallParticleCount;
    }

    public static int getTimeLimit() {
        return timeLimit;
    }
    
    public static Mode getMode() {
    	return mode;
    }

}
