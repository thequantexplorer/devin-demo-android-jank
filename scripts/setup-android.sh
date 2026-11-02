#!/usr/bin/env bash
# Installs the Android SDK, creates an emulator and boots it. Safe to run more than once.
# Usage: ./scripts/setup-android.sh            (emulator window on the desktop, so it can be recorded)
#        HEADLESS=1 ./scripts/setup-android.sh (no window)
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
AVD_NAME="${AVD_NAME:-fieldnotes}"
IMAGE="system-images;android-34;google_apis;x86_64"
export ANDROID_HOME
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
cd "$(dirname "$0")/.."

if [ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "==> Installing Android command-line tools into $ANDROID_HOME"
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  tmp="$(mktemp -d)"
  curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -o "$tmp/tools.zip"
  unzip -q "$tmp/tools.zip" -d "$tmp"
  mv "$tmp/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
  rm -rf "$tmp"
fi

if [ ! -d "$ANDROID_HOME/system-images/android-34/google_apis/x86_64" ] || [ ! -d "$ANDROID_HOME/platforms/android-35" ]; then
  echo "==> Installing SDK packages (a few minutes the first time)"
  yes | sdkmanager --licenses > /dev/null 2>&1 || true
  sdkmanager --install "platform-tools" "platforms;android-35" "build-tools;35.0.0" "emulator" "$IMAGE" > /dev/null
fi

echo "sdk.dir=$ANDROID_HOME" > local.properties

if ! grep -q "ANDROID_HOME=$ANDROID_HOME" ~/.bashrc 2>/dev/null; then
  {
    echo "export ANDROID_HOME=$ANDROID_HOME"
    echo 'export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH'
  } >> ~/.bashrc
fi

if [ -e /dev/kvm ] && [ ! -w /dev/kvm ]; then
  echo "==> Giving this user access to /dev/kvm"
  sudo chmod 666 /dev/kvm
fi

# The emulator and Gradle together need more memory than a small machine has. Without swap the
# emulator freezes or is killed part way through a benchmark.
if [ -z "$(swapon --show --noheadings 2>/dev/null)" ] && [ ! -e /swapfile ]; then
  echo "==> Adding a 4 GB swap file"
  sudo fallocate -l 4G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile > /dev/null && sudo swapon /swapfile
fi

if ! avdmanager list avd -c 2>/dev/null | grep -qx "$AVD_NAME"; then
  echo "==> Creating emulator $AVD_NAME (API 34, 720x1600)"
  echo "no" | avdmanager create avd -n "$AVD_NAME" -k "$IMAGE" --device "pixel_6" > /dev/null
  # A smaller screen keeps software rendering cheap enough for a 2-core machine.
  cat >> "$HOME/.android/avd/$AVD_NAME.avd/config.ini" <<'CONFIG'
hw.lcd.width=720
hw.lcd.height=1600
hw.lcd.density=320
hw.cpu.ncore=2
hw.ramSize=2048
CONFIG
fi

if adb devices | grep -q "^emulator-"; then
  echo "==> An emulator is already running"
else
  echo "==> Booting emulator (log: /tmp/emulator.log)"
  flags=(-avd "$AVD_NAME" -no-audio -no-boot-anim -no-snapshot -gpu guest)
  if [ "${HEADLESS:-0}" = "1" ]; then flags+=(-no-window); fi
  DISPLAY="${DISPLAY:-:0}" nohup emulator "${flags[@]}" > /tmp/emulator.log 2>&1 &
fi

adb wait-for-device
for _ in $(seq 1 180); do
  if [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
    adb shell input keyevent 82 > /dev/null 2>&1 || true
    echo "==> Emulator is ready"
    exit 0
  fi
  sleep 2
done
echo "Emulator did not finish booting. See /tmp/emulator.log" >&2
exit 1
