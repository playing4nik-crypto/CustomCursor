package com.customcursor.app.shizuku

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import rikka.shizuku.Shizuku

/**
 * METHOD 2 — SHIZUKU / ADB (Privileged PointerIcon Override)
 * ───────────────────────────────────────────────────────────
 * Shizuku grants a shell-level UID (2000) which can call hidden APIs normally
 * inaccessible to regular apps. On Android 14+ the InputManager exposes
 * setCustomPointerIcon() via reflection under this privilege level.
 *
 * SETUP REQUIREMENTS:
 *  1. Developer Options → Wireless debugging → Pair device
 *  2. Run once: `adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh`
 *  3. Grant Shizuku permission to this app at runtime (see grantPermission below)
 *
 * Note: This approach sets the REAL system PointerIcon — no overlay needed.
 * The cursor change is invisible to accessibility services and persists at
 * the InputFlinger level until device reboot or explicit reset.
 */
object ShizukuCursorHelper {

    private const val TAG = "ShizukuCursor"
    private const val SHIZUKU_PERMISSION_CODE = 42

    /**
     * Returns true if Shizuku is installed, running, and this app has permission.
     */
    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && hasPermission()
        } catch (e: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                false
            } else {
                Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) { false }
    }

    fun requestPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.addRequestPermissionResultListener(listener)
        Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
    }

    /**
     * Applies a custom pointer icon at the InputManager level via reflection.
     * This requires shell UID (provided by Shizuku) on Android 14+ (API 34+).
     *
     * @param bitmap  32-bit ARGB_8888 bitmap of the cursor (hotspot at 0,0)
     * @param hotspotX  x offset of the cursor hot point in the bitmap
     * @param hotspotY  y offset of the cursor hot point in the bitmap
     */
    fun applyCustomPointerIcon(context: Context, bitmap: Bitmap, hotspotX: Int = 0, hotspotY: Int = 0) {
        if (!isAvailable()) {
            Log.w(TAG, "Shizuku not available — falling back to accessibility overlay")
            return
        }

        try {
            // IInputManager is a hidden system service interface
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
            val inputManagerBinder = getServiceMethod.invoke(null, "input")

            val iInputManagerStubClass = Class.forName("android.hardware.input.IInputManager\$Stub")
            val asInterfaceMethod = iInputManagerStubClass.getMethod("asInterface", android.os.IBinder::class.java)
            val iInputManager = asInterfaceMethod.invoke(null, inputManagerBinder)

            // PointerIcon.createCustomIcon(bitmap, hotspotX, hotspotY)
            val pointerIconClass = Class.forName("android.view.PointerIcon")
            val createMethod = pointerIconClass.getMethod(
                "createCustomIcon",
                Bitmap::class.java, Float::class.java, Float::class.java
            )
            val pointerIcon = createMethod.invoke(null, bitmap, hotspotX.toFloat(), hotspotY.toFloat())

            // IInputManager.setCustomPointerIcon(PointerIcon) — shell-only API
            val setMethod = iInputManager!!.javaClass.getMethod(
                "setCustomPointerIcon",
                pointerIconClass
            )
            setMethod.invoke(iInputManager, pointerIcon)

            Log.i(TAG, "Custom PointerIcon applied via IInputManager (Shizuku)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply custom pointer icon via Shizuku: ${e.message}", e)
        }
    }

    /**
     * Resets the pointer icon to the system default.
     */
    fun resetToDefault(context: Context) {
        if (!isAvailable()) return
        try {
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
            val inputManagerBinder = getServiceMethod.invoke(null, "input")
            val iInputManagerStubClass = Class.forName("android.hardware.input.IInputManager\$Stub")
            val iInputManager = iInputManagerStubClass.getMethod("asInterface", android.os.IBinder::class.java)
                .invoke(null, inputManagerBinder)
            iInputManager!!.javaClass.getMethod("setCustomPointerIcon", Class.forName("android.view.PointerIcon"))
                .invoke(iInputManager, null)
        } catch (e: Exception) {
            Log.e(TAG, "Reset failed: ${e.message}", e)
        }
    }
}
