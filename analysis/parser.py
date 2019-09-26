from models import Particle, Step, Simulation
import glob
import sys
import os

X_FILE_COORD = 3

def parseDirectoryFromArgs():
  return parseDirectory(sys.argv[1])

def parseModeFromArgs():
  return int(sys.argv[2])

def parseDirectory(directory):
  return [parseFile(f) for f in glob.glob(directory + '/*')]

def parseFile(filename):
  lines = [line.rstrip('\n') for line in open(filename)]
  steps = []
  while len(lines) > 0:
    steps.append(parseStep(lines))
  return Simulation(steps, os.path.basename(filename))

def parseStep(lines):
  nextLines = int(lines.pop(0))
  time = float(lines.pop(0).split("Time=").pop())
  particles = [ parseParticle(lines.pop(0)) for _ in range(nextLines)]
  return Step(time, particles)

def parseParticle(line):
  properties = line.split(" ")
  particle = Particle(*properties)
  return particle

def parseOscillatorDirectory(file_prefix = 'ovito_output'):
  directory = sys.argv[1]
  simulations = {
                  'verlet': None,
                  'beeman': None,
                  'gear_predictor_corrector': None
                }

  for simtype in simulations.keys():
    filename = '{}_{}.xyz'.format(file_prefix, simtype)
    path = os.path.abspath(os.path.join(directory, filename))
    simulations[simtype] = parseFile(path)

  return simulations

def parseOneLineResultFile(filename):
    file = open("../results_bash/{}".format(filename))
    split_line = file.readline().split(" ")
    return float(split_line[X_FILE_COORD])

def parseDtFromFile(file_index):
    file = open("../dt_{}.txt".format(file_index))
    return float(file.readline())
