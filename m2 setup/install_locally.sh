#!/bin/sh

mvn install:install-file -Dfile=informa.jar -DgroupId=de.nava.informa \
    -DartifactId=informa -Dversion=0.6.5 -Dpackaging=jar

mvn install:install-file -Dfile=Hiatus_Environment_2008.jar -DgroupId=com.hiatus \
    -DartifactId=environment -Dversion=1.0-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=Hiatus_HTL_2008.jar -DgroupId=com.hiatus \
    -DartifactId=HTL -Dversion=1.0-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=Hiatus_HTTP_2008.jar -DgroupId=com.hiatus \
    -DartifactId=http -Dversion=1.0-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=Hiatus_Mail_2008.jar -DgroupId=com.hiatus \
    -DartifactId=mail -Dversion=1.0-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=Hiatus_Utils_2008.jar -DgroupId=com.hiatus \
    -DartifactId=utils -Dversion=1.0-SNAPSHOT -Dpackaging=jar
