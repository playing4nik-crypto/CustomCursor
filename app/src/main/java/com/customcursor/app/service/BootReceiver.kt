package com.customcursor.app.service
import android.content.*
import android.os.Build
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        if (i.action==Intent.ACTION_BOOT_COMPLETED||i.action==Intent.ACTION_MY_PACKAGE_REPLACED) {
            val si=Intent(ctx,CursorOverlayService::class.java)
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) ctx.startForegroundService(si) else ctx.startService(si)
        }
    }
}
