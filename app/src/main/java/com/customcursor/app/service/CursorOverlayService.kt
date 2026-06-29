package com.customcursor.app.service
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.customcursor.app.MainActivity
class CursorOverlayService : Service() {
    companion object {
        const val CHANNEL_ID="cursor_overlay_channel"; const val NOTIF_ID=1001
        const val ACTION_UPDATE_CONFIG="com.customcursor.app.ACTION_UPDATE_CONFIG"
        const val EXTRA_CONFIG_SHAPE="shape"; const val EXTRA_CONFIG_SIZE="size"
        const val EXTRA_FILL_COLOR="fill_color"; const val EXTRA_BORDER_COLOR="border_color"
        const val EXTRA_BORDER_THICK="border_thick"; const val EXTRA_OPACITY="opacity"
    }
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            val ch=NotificationChannel(CHANNEL_ID,"Custom Cursor Overlay",NotificationManager.IMPORTANCE_MIN).apply{setShowBadge(false)}
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
        startForeground(NOTIF_ID, NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("Custom Cursor Active")
            .setContentText("Your custom mouse pointer is running system-wide")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentIntent(PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE))
            .setOngoing(true).setSilent(true).setPriority(NotificationCompat.PRIORITY_MIN).build())
    }
    override fun onStartCommand(i: Intent?, f: Int, s: Int)=START_STICKY
    override fun onBind(i: Intent?): IBinder?=null
}
