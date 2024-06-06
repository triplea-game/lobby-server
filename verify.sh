#!/bin/bash

scriptDir="$(dirname "$0")"
set -o pipefail
set -eu

"$scriptDir/gradlew" spotlessApply check $@
