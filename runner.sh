#!/bin/bash
verlet="verlet"
beeman="beeman"
gear_predictor_corrector="gear_predictor_corrector"
integrators=(verlet beeman gear_predictor_corrector)
mkdir results_bash
for method in {0..2}
do
  for dt in {1..3}
  do
    echo 0 > params.txt
    echo $method >> params.txt
    cat dt_${dt}.txt >> params.txt
    echo >> params.txt # empty line because cat does not insert LF
    echo 5 >> params.txt
    java -jar target/tpes-1.0-SNAPSHOT.jar < params.txt
    name=$(ls ovito_output*)
    mv ${name} results_bash/${dt}_${name}
  done
done
for result in $(ls results_bash)
do
  tail -n 1 results_bash/$result > aux.tmp
  mv aux.tmp results_bash/$result
done
