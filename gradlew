#!/bin/sh

# Gradle wrapper script for Android builds

# Find Java
if [ -n "$JAVA_HOME" ] ; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="java"
fi

# Set JVM options
JVM_OPTS="-Xmx1024m -Xms256m"

# Find the wrapper jar
DIR=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

# Check if wrapper jar exists
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "gradle-wrapper.jar not found. Downloading..."
    mkdir -p "$DIR/gradle/wrapper"
    curl -L -o "$WRAPPER_JAR" https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar
fi

# Run Gradle
exec "$JAVA" $JVM_OPTS -cp "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
