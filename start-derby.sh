#!/bin/bash

# Define variables
DERBY_VERSION="10.15.2.0"  # Change this to the desired Derby version
DERBY_BASE_DIR="target/derby"
DERBY_JAR="$DERBY_BASE_DIR/derby-$DERBY_VERSION.jar"

# Create Derby base directory
mkdir -p "$DERBY_BASE_DIR"

# Download Derby using Maven
echo "Downloading Apache Derby version $DERBY_VERSION..."
mvn dependency:get -Dartifact=org.apache.derby:derby:$DERBY_VERSION -Ddest="$DERBY_JAR"

# Check if the download was successful
if [ ! -f "$DERBY_JAR" ]; then
    echo "Failed to download Derby. Please check your Maven configuration."
    exit 1
fi

# Start Derby Network Server
echo "Starting Apache Derby Network Server..."
java -jar "$DERBY_JAR" start

# Output connection details
echo "Apache Derby is running. You can connect to it using:"
echo "jdbc:derby://localhost:1527/yourDatabaseName;create=true"

# Keep the script running to keep the server alive
echo "Press [CTRL+C] to stop the server."
wait
