#!/bin/bash
set -e

# Build the project
echo "Building project..."
./gradlew clean build

# Install the binary
BINARY="build/bin/macosArm64/releaseExecutable/spendex.kexe"
TARGET="/usr/local/bin/spndx"

if [ ! -f "$BINARY" ]; then
  echo "Error: Binary not found at $BINARY"
  echo "Please run './gradlew build' first"
  exit 1
fi

echo "Installing spndx to $TARGET..."
sudo cp "$BINARY" "$TARGET"
sudo chmod +x "$TARGET"

echo ""
echo "Successfully installed spndx to /usr/local/bin"
echo "You can now run: spndx --help"
