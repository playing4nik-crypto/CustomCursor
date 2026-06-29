# Custom Cursor — Android 15 App

A premium Android 15 app that lets you customize your physical mouse pointer (Bluetooth/OTG) system-wide using two methods: Accessibility Service overlay (no-root) and Shizuku/ADB privileged API.

---

## Architecture Overview

```
com.customcursor.app/
├── MainActivity.kt                      ← Single-activity Compose host
├── model/CursorConfig.kt                ← Data model (shape, size, colors, opacity)
├── viewmodel/CursorViewModel.kt         ← State + DataStore persistence
├── ui/
│   ├── theme/  Color.kt, Theme.kt       ← Material You dark theme
│   ├── screens/DashboardScreen.kt       ← Main Compose dashboard UI
│   └── components/
│       ├── CursorPreviewCanvas.kt       ← Animated live cursor preview
│       └── ColorPickerDialog.kt         ← Color swatch + hex input picker
├── accessibility/
│   └── CursorAccessibilityService.kt   ← METHOD 1: Overlay via AccessibilityService
├── service/
│   ├── CursorOverlayService.kt          ← Foreground service (keeps overlay alive)
│   └── BootReceiver.kt                  ← Auto-start on reboot
└── shizuku/
    └── ShizukuCursorHelper.kt           ← METHOD 2: True PointerIcon via Shizuku
```

---

## Method 1: Accessibility Service + Overlay (No Root)

**How it works:**
1. `CursorAccessibilityService` registers as an Android Accessibility Service
2. On connect it adds a `TYPE_ACCESSIBILITY_OVERLAY` transparent window via `WindowManager`
3. The overlay window captures `MotionEvent` with `SOURCE_MOUSE` to track raw cursor X/Y coordinates
4. On each mouse move it calls `invalidate()` and redraws the custom cursor shape in `onDraw()`
5. Config updates are delivered via a local `BroadcastReceiver` — no service restart needed

**Permissions needed:**
- `BIND_ACCESSIBILITY_SERVICE` (granted by the user in Settings → Accessibility)
- `SYSTEM_ALERT_WINDOW` (for the overlay window)
- `FOREGROUND_SERVICE` (to keep service alive)

**Limitation:** The native system cursor is still visible underneath on some ROMs. On AOSP Android 15 the system cursor is hidden when an accessibility overlay covers it.

---

## Method 2: Shizuku / ADB — True PointerIcon Override

**How it works:**
1. Shizuku runs a persistent binder service at shell UID (2000)
2. `ShizukuCursorHelper` uses reflection to reach `IInputManager.setCustomPointerIcon()`
3. This directly replaces the InputFlinger-level pointer icon — no overlay, no lag
4. Change is system-wide and persists until reboot or explicit reset

**Setup:**
```bash
# 1. Enable Developer Options and Wireless Debugging on the device
# 2. Pair via ADB:
adb pair <ip>:<port>
adb connect <ip>:<debug-port>

# 3. Install Shizuku from Play Store, then start it:
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh

# 4. Open Shizuku app → grant permission to Custom Cursor
```

---

## Build Instructions

### Option A — Android Studio (Recommended)
1. Extract this ZIP
2. Open the `CustomCursor/` folder in Android Studio Hedgehog or newer
3. Copy `local.properties.template` → `local.properties` and set your SDK path
4. Build → Make Project  (`Ctrl+F9`)
5. Run on device or emulator  (`Shift+F10`)

### Option B — Termux (on-device build)

```bash
# ── Install prerequisites ──────────────────────────────────────────────────
pkg update && pkg upgrade -y
pkg install -y openjdk-17 wget unzip git

# ── Install Android SDK command-line tools ─────────────────────────────────
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip
mv cmdline-tools latest
rm commandlinetools-linux-*.zip

export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# ── Accept licenses & install SDK components ──────────────────────────────
yes | sdkmanager --licenses
sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"

# ── Clone / extract project ───────────────────────────────────────────────
# (copy CustomCursor.zip to Termux storage first)
cp /sdcard/CustomCursor.zip ~/
cd ~
unzip CustomCursor.zip

# ── Write local.properties ────────────────────────────────────────────────
cd CustomCursor
echo "sdk.dir=$ANDROID_HOME" > local.properties

# ── Grant execute permission to gradlew ───────────────────────────────────
chmod +x gradlew

# ── Build debug APK ───────────────────────────────────────────────────────
./gradlew assembleDebug

# APK will be at:
# app/build/outputs/apk/debug/app-debug.apk

# ── Install directly to connected device ──────────────────────────────────
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Option C — GitHub Actions CI (auto-build on push)
See `.github/workflows/build.yml` template below if you want cloud builds.

---

## Post-Install Setup

1. Open **Custom Cursor** app
2. Tap the purple banner → goes to **Settings → Accessibility**
3. Enable **"Custom Cursor Overlay"** accessibility service
4. Back in the app — configure shape, size, colors
5. Tap **"Set Now"** (feel the haptic!) — cursor is now active system-wide
6. Connect a Bluetooth or USB-OTG mouse and move it — you'll see your custom cursor

---

## Feature Checklist

| Feature | Status |
|---|---|
| 6 cursor shapes (Arrow, Hand, Crosshair, Circle, Diamond, Star) | ✅ |
| Size slider (16–80 dp) | ✅ |
| Fill color picker (palette + hex input) | ✅ |
| Border color picker | ✅ |
| Border thickness slider | ✅ |
| Opacity control | ✅ |
| Animated live preview | ✅ |
| Material You (Dynamic Color) dark theme | ✅ |
| Android 15 haptic feedback on Set Now | ✅ |
| DataStore persistence across app restarts | ✅ |
| Auto-start on device reboot | ✅ |
| Accessibility Service overlay (no root) | ✅ |
| Shizuku IInputManager override (privileged) | ✅ |

---

## Troubleshooting

| Problem | Fix |
|---|---|
| Overlay not showing | Enable the Accessibility Service in Settings |
| Cursor position is offset | Rotate screen once to recalibrate overlay bounds |
| Shizuku permission denied | Re-run start.sh via ADB, then grant in Shizuku app |
| Build fails: `sdk.dir` not set | Copy `local.properties.template` → `local.properties` |
| Gradle download hangs in Termux | Use a VPN or set `distributionUrl` to a local cache |
