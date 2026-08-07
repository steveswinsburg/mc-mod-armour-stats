#!/usr/bin/env bash
#
# Cloud Agent install script for mc-mod-armour-stats.
#
# Minecraft 26.2 requires Java 25, which is not part of the default image, so
# this script provisions a Temurin JDK 25, makes it the default toolchain and
# warms the Gradle/Loom caches by building the mod. It is idempotent: re-running
# it reuses an already-installed JDK and only rebuilds.
set -euo pipefail

JDK_DIR=/opt/java/temurin-25
JDK_URL="https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse"

# 1. Ensure a Java 25 JDK is present.
if [ ! -x "${JDK_DIR}/bin/java" ]; then
	echo "Installing Temurin JDK 25 (required by Minecraft 26.2)..."
	sudo mkdir -p /opt/java
	tmp="$(mktemp -d)"
	curl -fsSL "${JDK_URL}" -o "${tmp}/jdk.tar.gz"
	extracted="$(tar -tzf "${tmp}/jdk.tar.gz" | head -1 | cut -d/ -f1)"
	sudo rm -rf "/opt/java/${extracted}"
	sudo tar -xzf "${tmp}/jdk.tar.gz" -C /opt/java
	sudo ln -sfn "/opt/java/${extracted}" "${JDK_DIR}"
	rm -rf "${tmp}"
fi

# 2. Make JDK 25 the default java/javac so Gradle runs on it.
sudo update-alternatives --install /usr/bin/java java "${JDK_DIR}/bin/java" 2500
sudo update-alternatives --install /usr/bin/javac javac "${JDK_DIR}/bin/javac" 2500
sudo update-alternatives --set java "${JDK_DIR}/bin/java"
sudo update-alternatives --set javac "${JDK_DIR}/bin/javac"

export JAVA_HOME="${JDK_DIR}"
echo "Using Java: $(java -version 2>&1 | head -1)"

# 3. Warm the Gradle/Loom caches (downloads Minecraft 26.2 + libraries) and
#    verify the mod compiles.
./gradlew --no-daemon build

echo "Environment ready. Build the mod with './gradlew build' and launch a dev client with './gradlew runClient'."
