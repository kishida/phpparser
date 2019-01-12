#!/bin/bash
java -Dtruffle.class.path.append=language/target/PHPParser-1.0-SNAPSHOT-jar-with-dependencies.jar -cp launcher/target/launcher-1.0-SNAPSHOT.jar kis.launcher.Loader $1

