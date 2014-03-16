#!/bin/sh

mvn clean install
java -jar target/microbenchmarks.jar -tu s -f 2 -wi 1 -i 4 -rf json -rff results.json -bm thrpt ".*AtomicLongBenchmark.*" -prof stack
java -jar target/microbenchmarks.jar -tu s -f 2 -wi 1 -i 4 -rf json -rff results.json -bm thrpt ".*MapBenchmark.*" -prof stack

