#!/bin/bash

##############################################################################
##  Gradle start up script for UNIX
##############################################################################

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here
DEFAULT_JVM_OPTS='"-Xmx1024m" "-Xms256m"'

# Use the maximum available file descriptors
MAX_FD_LIMIT=`ulimit -H -n`
if [ $? -eq 0 ] ; then
    MAX_FD=`ulimit -n`
    if [ $MAX_FD = "maximum" -o $MAX_FD = "max" ] ; then
        MAX_FD="$MAX_FD_LIMIT"
    fi
    ulimit -n $MAX_FD
    if [ $? -ne 0 ] ; then
        warn "Could not set maximum file descriptor limit: $MAX_FD"
    fi
else
    warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
fi

# Find the Java command
if [ -n "$JAVA_HOME" ] ; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

# Locate the gradle wrapper jar
SCRIPT_DIR=`dirname "$0"`
WRAPPER_JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ] ; then
    echo "ERROR: gradle-wrapper.jar not found!"
    exit 1
fi

# Execute Gradle
exec "$JAVA_CMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    -classpath "$WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
