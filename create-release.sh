#!/bin/sh

# variables
VERSION=`egrep -o '[[:digit:]]{1,3}\.[[:digit:]]{1,3}\.[[:digit:]]{1,3}' src/CBTool.java`
ZIPNAME="CBTool-v$VERSION.zip"
SHANAME="$ZIPNAME.sha256"

printf "Creating release v$VERSION.\n"

# Make the zip file for distribution
printf "Creating zip file..."
mkdir CBTool/
cp -r tool CBTool
zip -qr distribution/$ZIPNAME CBTool/*
rm -rf CBTool/
printf "OK.\n"

# compute shasum for file
printf "Creating checksum..."
cd distribution
shasum -a 256 $ZIPNAME > $SHANAME
printf "OK.\n"

printf "Output files stored in distribution/ folder.\n"