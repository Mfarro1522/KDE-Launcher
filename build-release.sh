#!/bin/bash

VERSION="1.0.0"
OUTPUT_DIR="app/build/outputs/apk/release"
RELEASES_DIR="releases"

set -e

echo "Building TAPO-Launcher v$VERSION..."

cd "$(dirname "$0")/app"

mkdir -p "../$RELEASES_DIR"

./gradlew assembleRelease --no-daemon

if [ -f "$OUTPUT_DIR/app-release.apk" ]; then
    cp "$OUTPUT_DIR/app-release.apk" "../$RELEASES_DIR/TAPO-Launcher_${VERSION}.apk"
    echo "APK created: $RELEASES_DIR/TAPO-Launcher_${VERSION}.apk"
else
    echo "Error: APK not found at $OUTPUT_DIR/app-release.apk"
    exit 1
fi
