#!/bin/bash

# This script runs all checks across the entire project.
"$scriptDir/gradlew" spotlessApply check $@

