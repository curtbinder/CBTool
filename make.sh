#!/bin/sh

# The pde.jar file may be buried inside the .app file on Mac OS X.
PDE="/Applications/Arduino.app/Contents/Java/pde.jar"
CORE="/Applications/Arduino.app/Contents/Java/arduino-core.jar"

javac -target 1.8 -source 1.8 \
  -cp "$CORE:$PDE" \
  -d bin \
  src/CBTool.java

cd bin
zip -x *.DS_Store -r ../tool/cbtool.jar *
cd ..

# Make the zip file for distribution
mkdir CBTool/
cp -r tool CBTool
zip -r distribution/CBTool-version.zip CBTool/*
rm -rf CBTool/