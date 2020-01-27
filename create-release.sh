#!/bin/sh

# Make the zip file for distribution
mkdir CBTool/
cp -r tool CBTool
zip -r distribution/CBTool-version.zip CBTool/*
rm -rf CBTool/
