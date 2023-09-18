#!/bin/sh

# The pde.jar file may be buried inside the .app file on Mac OS X.
#PDE="/Users/binder/Applications/Arduino.app/Contents/Java/pde.jar"
#CORE="/Users/binder/Applications/Arduino.app/Contents/Java/arduino-core.jar"
PDE="/home/binder/.local/share/flatpak/app/cc.arduino.arduinoide/current/active/files/Arduino/lib/pde.jar"
CORE="/home/binder/.local/share/flatpak/app/cc.arduino.arduinoide/current/active/files/Arduino/lib/arduino-core.jar"

javac -target 1.8 -source 1.8 \
  -cp "$CORE:$PDE" \
  -d bin \
  src/CBTool.java \
  src/RABaseFile.java \
  src/RAFeatures.java \
  src/RALabels.java \
  src/RALibsVersion.java \
  src/RACustomSettings.java

if [ ! -d "tool" ]; then
  # tool directory doesn't exist, so create it
  mkdir tool
fi

cd bin
zip -x *.DS_Store -r ../tool/cbtool.jar *
cd ..
