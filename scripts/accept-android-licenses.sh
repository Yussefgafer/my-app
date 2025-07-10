#!/bin/bash

# Create the licenses directory if it doesn't exist
mkdir -p "$ANDROID_HOME/licenses"

# Accept all standard Android SDK licenses
echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"
echo "8933bad161af4178b1185d1a37fbf41ea5269c55" >> "$ANDROID_HOME/licenses/android-sdk-license"
echo "d56f5187479451eabf01fb78af6dfcb131a6481e" >> "$ANDROID_HOME/licenses/android-sdk-license"
echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" >> "$ANDROID_HOME/licenses/android-sdk-license"
echo "d56f5187479451eabf01fb78af6dfcb131a6481e" >> "$ANDROID_HOME/licenses/android-sdk-license"
echo "33b6c2e2681ae2633006a445f6d1eab059b21380" >> "$ANDROID_HOME/licenses/android-sdk-license"

# Accept Google TV license
echo "601085b94cc71ea42425f5c6230a62893f4eac9d" > "$ANDROID_HOME/licenses/android-googletv-license"

# Accept SDK Patch Applier license
echo "d975f751698a77b662f9334dbea2b1e4a3f6a4a8" > "$ANDROID_HOME/licenses/android-sdk-license-2746d5c9"

# Accept Intel x86 Emulator Accelerator (HAXM) license
echo "d9a2a6739ff824256d43d80971c3d1c69a3c389f" > "$ANDROID_HOME/licenses/intel-android-extra-license"

# Accept MIPS system image license
echo "e9acab5b5fbb560a72cfaecce8946896ff6aab9d" > "$ANDROID_HOME/licenses/mips-android-sysimage-license"

# Accept Android SDK Platform-Tools license
echo "33b6c2e2681ae2633006a445f6d1eab059b21380" > "$ANDROID_HOME/licenses/android-sdk-license"

# Accept Android SDK Build-Tools license
echo "601085b94cc77c0f54f8ca3922b5227e1cd45526" >> "$ANDROID_HOME/licenses/android-sdk-license"

# Accept Google Play services license
echo "33b6c2e2681ae2633006a445f6d1eab059b21380" >> "$ANDROID_HOME/licenses/android-sdk-license"

# Accept Google Repository license
echo "601085b94cc77c0f54f8ca3922b5227e1cd45526" >> "$ANDROID_HOME/licenses/android-sdk-license"

# Accept Android SDK Platform license
echo "d56f5187479451eabf01fb78af6dfcb131a6481e" >> "$ANDROID_HOME/licenses/android-sdk-license"

# Accept Android SDK Preview license
echo "84831b9409646a918e30573bab4c9c91346d8abd" > "$ANDROID_HOME/licenses/android-sdk-preview-license"

# Accept ARM EABI v7a System Image license
echo "d975f751698a77b662f9334dbea2b1e4a3f6a4a8" > "$ANDROID_HOME/licenses/android-sdk-arm-dbt-license"

# Verify licenses were created
echo "License files created:"
ls -la "$ANDROID_HOME/licenses/"

# List licenses for debugging
echo "License file contents:"
cat "$ANDROID_HOME/licenses/"*

# Accept all licenses
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses >/dev/null 2>&1 || true

# Update SDK components
echo y | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --update >/dev/null 2>&1 || true
