#!/bin/bash
native-image --tool:truffle -cp language/target/PHPParser-1.0-SNAPSHOT-jar-with-dependencies.jar kis.phpparser.Loader interpreter 

