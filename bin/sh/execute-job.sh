#!/bin/bash
#
# Copyright 2015 Delft University of Technology
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

# rootdir=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/../..

# Parse commandline instructions (provided by Graphalytics).
while [[ $# -gt 1 ]] # Parse two arguments: [--key value] or [-k value]
  do
  key="$1"
  value="$2"

  case $key in

    --job-id)
      JOB_ID="$value"
      shift;;

    --log-path)
      LOG_PATH="$value"
      shift;;

    --algorithm)
      ALGORITHM="$value"
      shift;;

    --source-vertex)
      SOURCE_VERTEX="$value"
      shift;;

    --max-iteration)
      MAX_ITERATION="$value"
      shift;;

    --damping-factor)
      DAMPING_FACTOR="$value"
      shift;;

    --input-path)
      INPUT_PATH="$value"
      shift;;

    --output-path)
      OUTPUT_PATH="$value"
      shift;;

    --num-machines)
      NUM_MACHINES="$value"
      shift;;

    --num-threads)
      NUM_THREADS="$value"
      shift;;

    --directed)
      DIRECTED="$value"
      shift;;

    --num-vertices)
      NUM_VERTICES="$value"
      shift;;

    --home-dir)
      HOME_DIR="$value"
      shift;;

    *)
      echo "Error: invalid option: " "$key"
      exit 1
      ;;
  esac
  shift
done

# TODO Add Gunrock bin folder path instead of softlinking manually
# MAking arguments understandable by Gunrock
if $DIRECTED;then
	UNDIRECTED=false
else
	UNDIRECTED=true
fi

case $ALGORITHM in

    bfs)
      COMMAND="$HOME_DIR/build/bin/$ALGORITHM \
        market ${INPUT_PATH} \
        --src $SOURCE_VERTEX"
      ;;
    sssp)
      COMMAND="$HOME_DIR/build/bin/$ALGORITHM \
        market ${INPUT_PATH} \
        --src $SOURCE_VERTEX"
      ;;
    pr)
      COMMAND="$HOME_DIR/build/bin/$ALGORITHM \
        market ${INPUT_PATH} "
    ;;
    lcc)
      COMMAND="$HOME_DIR/build/bin/louvain \
        market ${INPUT_PATH} "
      ;;

     wcc)
       COMMAND="$HOME_DIR/build/bin/$ALGORITHM \
         --jobid $JOB_ID \
         --dataset $INPUT_PATH \
         --output $OUTPUT_PATH \
         --directed $DIRECTED \
         --log-path $LOG_PATH \
         --threadnum $NUM_THREADS"
       ;;


     cdlp)
       COMMAND="$HOME_DIR/build/bin/lp \
         market ${INPUT_PATH} "
       ;;

    #  lcc)
    #    COMMAND="$rootdir/bin/exe/$ALGORITHM \
    #      --jobid $JOB_ID \
    #      --dataset $INPUT_PATH \
    #      --output $OUTPUT_PATH \
    #      --directed $DIRECTED \
    #      --log-path $LOG_PATH \
    #      --threadnum $NUM_THREADS"
    #    ;;

    #  sssp)
    #    COMMAND="$rootdir/bin/exe/$ALGORITHM
    #      --jobid $JOB_ID \
    #      --dataset $INPUT_PATH \
    #      --output $OUTPUT_PATH \
    #      --directed $DIRECTED
    #      --source-vertex $SOURCE_VERTEX \
    #      --log-path $LOG_PATH \
    #      --threadnum $NUM_THREADS"
    #    ;;

     *)
       echo "Error: algorithm $ALGORITHM not defined."
       exit 1
       ;;
esac


echo "Executing platform job" "$COMMAND"

#$COMMAND
$COMMAND --jsondir=$LOG_PATH &> $LOG_PATH/gunrock.logs & echo $! > $LOG_PATH/executable.pid
wait $!
