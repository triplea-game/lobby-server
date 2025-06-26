#!/bin/bash

# This script runs all checks across the entire project.
scriptDir="$(dirname "$0")"
"$scriptDir/gradlew" spotlessApply check $@

