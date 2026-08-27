#!/usr/bin/env bash
# Builds a self-contained app image with jpackage: the bundled runtime INCLUDES
# jdk.compiler, so students get working coding exercises with zero JDK setup.
#
# Usage:  ./packaging/build-app-image.sh            (app-image in target/dist)
#         TYPE=deb ./packaging/build-app-image.sh   (or rpm / msi / dmg — needs
#                                                    the platform's packaging tools)
set -euo pipefail
cd "$(dirname "$0")/.."

JAR=$(ls target/java-study-program-*.jar | grep -v original | head -1)
[ -f "$JAR" ] || { echo "Run 'mvn package' first."; exit 1; }

TYPE="${TYPE:-app-image}"
rm -rf target/dist target/pkg-input
mkdir -p target/dist target/pkg-input
cp "$JAR" target/pkg-input/   # stage only the fat jar, nothing else from target/

jpackage \
  --type "$TYPE" \
  --name JavaStudyProgram \
  --app-version 1.0 \
  --input target/pkg-input \
  --main-jar "$(basename "$JAR")" \
  --add-modules java.se,jdk.compiler,jdk.zipfs \
  --jlink-options "--strip-debug --no-man-pages --no-header-files" \
  --dest target/dist \
  --java-options -Dfile.encoding=UTF-8
# NOTE: --jlink-options above deliberately omits jpackage's default
# --strip-native-commands: the coding harness runs student tests in a `java`
# subprocess, so the bundled runtime must keep its bin/java launcher.

echo
echo "Built: target/dist/"
echo "The bundled runtime includes the Java compiler — coding exercises work"
echo "even for students with no JDK installed."
