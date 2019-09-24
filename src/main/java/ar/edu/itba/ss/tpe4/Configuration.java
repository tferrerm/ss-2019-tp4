package ar.edu.itba.ss.tpe4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    
    public static String filename;
    
    public static final int AREA_BORDER_LENGTH = 1;
    private static final int OSCILLATOR_PARTICLE_COUNT = 1;
    private static final double OSCILLATOR_RADIUS = 0;
    private static final double OSCILLATOR_MASS = 70; // kg
    public static final double OSCILLATOR_K = 10e4; // N/m
    public static final double OSCILLATOR_GAMMA = 100; // kg/s
    private static final double OSCILLATOR_A = 0.1; // HACER
    private static final double OSCILLATOR_INIT_POS = 1; // m
    private static final double OSCILLATOR_INIT_VEL = - OSCILLATOR_A * OSCILLATOR_GAMMA / (2 * OSCILLATOR_MASS); // m/s
    
    private static final int GAS_PARTICLE_COUNT = 300;
    private static final double GAS_PARTICLE_RADIUS = 0.005;
    public static final double GAS_EPSILON = 2; // adimensional
    public static final double GAS_Rm = 1; // adimensional
    public static final double GAS_L = 12; // adimensional
    public static final double GAS_J = 6; // adimensional
    private static final double GAS_PARTICLE_MASS = 0.1; // adimensional
    private static final double GAS_INITIAL_VELOCITY = 10; // m/s
    public static final double GAS_RANGE = 5; // m
    public static final double GAS_BOX_HEIGHT = 200; // m
    public static final double GAS_BOX_WIDTH = 400; // m
    public static final double GAS_BOX_HOLE_SIZE = 50; // m
    public static final double GAS_BOX_HOLE_POSITION = 75; // m
    public static final double GAS_BOX_SPLIT = 200; // m

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
        filename = "./ovito_output_" + integrator.toString().toLowerCase() + ".xyz";
        
        System.out.println("Enter Time Step:");
        Double selectedTimeStep = null;
        while(selectedTimeStep == null || selectedTimeStep <= 0) {
        	selectedTimeStep = stringToDouble(scanner.nextLine());
        }
        timeStep = selectedTimeStep;

        System.out.println("Enter Time Limit [-1 -> Balance time; -2 -> 2 * Balance Time]:");
        Integer selectedTimeLimit = null;
        while(selectedTimeLimit == null || selectedTimeLimit <= 0) {
            selectedTimeLimit = stringToInt(scanner.nextLine());
        }
        timeLimit = selectedTimeLimit;

        scanner.close();
    }

    /* Parameters must have already been requested */
    public static List<Particle> generateRandomInputFilesAndParseConfiguration() {
        generateInputFile();
        List<Particle> particles = parseConfiguration();
        generateOvitoOutputFile();
        return particles;
    }

    private static List<Particle> parseConfiguration() {
        try(BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            /* Time (0) */
            br.readLine();
            
            switch(mode) {
            case OSCILLATOR:
            	return parseOscillatorConfig(br);
			case LENNARD_JONES_GAS:
            	return parseGasConfig(br);
            default:
            	throw new IllegalStateException();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return Collections.emptyList();
    }

	private static List<Particle> parseOscillatorConfig(final BufferedReader br) throws IOException {
		List<Particle> particles = new ArrayList<>();
    	String line = br.readLine();
        if(line == null)
            failWithMessage("Particles do not match particle count.");
        String[] attributes = line.split(" ");
        attributes = removeSpaces(attributes);
        setParticleProperties(particles, attributes);
        return particles;
	}
	
    private static List<Particle> parseGasConfig(final BufferedReader br) throws IOException {
    	List<Particle> particles = new ArrayList<>();
        for(int i = 0; i < GAS_PARTICLE_COUNT; i++) {
            String line = br.readLine();
            if(line == null)
                failWithMessage("Particles do not match particle count.");
            String[] attributes = line.split(" ");
            attributes = removeSpaces(attributes);
            setParticleProperties(particles, attributes);
        }
    	return particles;
	}

	private static String[] removeSpaces(final String[] array) {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        List<String> filteredList = list.stream().filter(s -> !s.equals("") && !s.equals(" ")).collect(Collectors.toList());
        String[] newArray = new String[filteredList.size()];
        return filteredList.toArray(newArray);
    }

    private static void setParticleProperties(final List<Particle> particles, final String[] attributes) {
        final int propertyCount = 4;
        Double x = null;
        Double y = null;
        Double vx = null;
        Double vy = null;
        if(attributes.length != propertyCount || (x = stringToDouble(attributes[0])) == null || (y = stringToDouble(attributes[1])) == null
                || (vx = stringToDouble(attributes[2])) == null || (vy = stringToDouble(attributes[3])) == null) {
            failWithMessage(attributes[0] + ", " + attributes[1] + ", " + attributes[2] + ", " + attributes[3] + " are invalid attributes.");
        }
        
        switch(mode) {
        case OSCILLATOR:
        	switch(integrator) {
        	case GEAR_PREDICTOR_CORRECTOR:
        		particles.add(new GearParticle(OSCILLATOR_RADIUS, OSCILLATOR_MASS, x, y, vx, vy));
            	break;
            default:
            	particles.add(new Particle(OSCILLATOR_RADIUS, OSCILLATOR_MASS, x, y, vx, vy));
            	break;
        	}
        	break;
        case LENNARD_JONES_GAS:
        	particles.add(new Particle(GAS_PARTICLE_RADIUS, GAS_PARTICLE_MASS, x, y, vx, vy));
        	break;
        }
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

    /* Time (0) */
    private static void generateInputFile() {
        List<Particle> particles = new ArrayList<>();
        File inputFile = new File(inputFileName);
        inputFile.delete();
        try(FileWriter fw = new FileWriter(inputFile)) {
            inputFile.createNewFile();
            fw.write("0\n");
            
            switch(mode) {
            case OSCILLATOR:
            	particles.add(new Particle(OSCILLATOR_RADIUS, OSCILLATOR_MASS, OSCILLATOR_INIT_POS, 0, OSCILLATOR_INIT_VEL, 0));
                fw.write(OSCILLATOR_INIT_POS + " 0.0 " + OSCILLATOR_INIT_VEL + " 0.0\n");
                break;
            case LENNARD_JONES_GAS:
                Random r = new Random();
                for(int i = 0; i < GAS_PARTICLE_COUNT; i++) {
                    double randomPositionX = 0;
                    double randomPositionY = 0;
                    boolean isValidPosition = false;

                    while(!isValidPosition) {
                        randomPositionX = (GAS_BOX_WIDTH - GAS_BOX_SPLIT - 2 * GAS_PARTICLE_RADIUS) * r.nextDouble() + GAS_PARTICLE_RADIUS;
                        randomPositionY = (GAS_BOX_HEIGHT - 2 * GAS_PARTICLE_RADIUS) * r.nextDouble() + GAS_PARTICLE_RADIUS;
                        isValidPosition = validateParticlePosition(particles, randomPositionX, randomPositionY, GAS_PARTICLE_RADIUS);
                    }

                    double randomVelocity = GAS_INITIAL_VELOCITY;
                    double angle = 2 * Math.PI * r.nextDouble();
                    double randomVelocityX = Math.cos(angle) * randomVelocity;
                    double randomVelocityY = Math.sin(angle) * randomVelocity;

                    particles.add(new Particle(GAS_PARTICLE_RADIUS, GAS_PARTICLE_MASS, randomPositionX, randomPositionY, randomVelocityX, randomVelocityY));
                    fw.write(randomPositionX + " " + randomPositionY + " " + randomVelocityX + " " + randomVelocityY + "\n");
                }
                break;
            }
        } catch (IOException e) {
            System.err.println("Failed to create dynamic input file.");
            e.printStackTrace();
        }
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
        File outputFile = new File(filename);
        outputFile.delete();
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create Ovito output file.");
            e.printStackTrace();
        }
    }

    public static void writeOvitoOutputFile(final Double time, final List<Particle> particles) {
        File outputFile = new File(filename);
        try(FileWriter fw = new FileWriter(outputFile, true)) {
            fw.write(particleCount + "\n");
            fw.write("Lattice=\"" + AREA_BORDER_LENGTH + " 0.0 0.0 0.0 " + AREA_BORDER_LENGTH + " 0.0 0.0 0.0 "
                    + AREA_BORDER_LENGTH + "\" Properties=id:I:1:mass:R:1:pos:R:2:velo:R:2 Time=" + time + "\n");
            for(Particle p : particles) {
                writeOvitoParticle(fw, p);
            }
        } catch (IOException e) {
            System.err.println("Failed to write Ovito output file.");
            e.printStackTrace();
        }
    }

    private static void writeOvitoParticle(final FileWriter fw, final Particle particle) throws IOException {
        fw.write(particle.getId() + " " + particle.getRadius() + " " + particle.getMass() + " " + particle.getPosition().getX() 
        		+ " " + particle.getPosition().getY() + " " + particle.getVelocity().getX() + " " 
        		+ particle.getVelocity().getY());
        fw.write('\n');
    }

    private static void writeGasOvitoParticle(final FileWriter fw, final Particle particle) throws IOException {
        fw.write(particle.getId() + " " + particle.getRadius() + " " + particle.getMass() + " " + particle.getPosition().getX() / 400.0 + " " + particle.getPosition().getY() / 200.0 + " " + particle.getVelocity().getX() + " " 
        		+ particle.getVelocity().getY());
        fw.write('\n');
    }

    public static void writeGasOvitoOutputFile(final Double time, final List<Particle> particles) {
        File outputFile = new File(filename);
        try(FileWriter fw = new FileWriter(outputFile, true)) {
            fw.write(particleCount + "\n");
            fw.write("Lattice=\"" + AREA_BORDER_LENGTH + " 0.0 0.0 0.0 " + AREA_BORDER_LENGTH + " 0.0 0.0 0.0 "
                    + AREA_BORDER_LENGTH + "\" Properties=id:I:1:radius:R:1:mass:R:1:pos:R:2:velo:R:2 Time=" + time + "\n");
            for(Particle p : particles) {
                writeGasOvitoParticle(fw, p);
            }
        } catch (IOException e) {
            System.err.println("Failed to write Ovito output file.");
            e.printStackTrace();
        }
    }

    public static int getParticleCount() {
        return particleCount;
    }
    
    public static double getTimeStep() {
        return timeStep;
    }

    public static int getTimeLimit() {
        return timeLimit;
    }
    
    public static boolean isOscillatorMode() {
    	return mode == Mode.OSCILLATOR;
    }

    public static boolean isGasMode() {
    	return mode == Mode.LENNARD_JONES_GAS;
    }
    
    public static Integrator getIntegrator() {
    	return integrator;
    }

}
