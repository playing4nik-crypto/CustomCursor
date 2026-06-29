package com.customcursor.app.accessibility
import android.accessibilityservice.AccessibilityService
import android.content.*
import android.graphics.*
import android.os.Build
import android.view.*
import android.view.accessibility.AccessibilityEvent
import com.customcursor.app.model.CursorConfig
import com.customcursor.app.model.CursorShape
import com.customcursor.app.service.CursorOverlayService
import kotlin.math.*
class CursorAccessibilityService : AccessibilityService() {
    private lateinit var wm: WindowManager
    private var overlay: CursorOverlayView? = null
    private var cfg = CursorConfig()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, i: Intent) {
            if (i.action==CursorOverlayService.ACTION_UPDATE_CONFIG) {
                cfg=cfg.copy(
                    shape=CursorShape.entries.find{it.name==i.getStringExtra(CursorOverlayService.EXTRA_CONFIG_SHAPE)}?:cfg.shape,
                    sizeDp=i.getFloatExtra(CursorOverlayService.EXTRA_CONFIG_SIZE,cfg.sizeDp),
                    fillColorArgb=i.getIntExtra(CursorOverlayService.EXTRA_FILL_COLOR,cfg.fillColorArgb),
                    borderColorArgb=i.getIntExtra(CursorOverlayService.EXTRA_BORDER_COLOR,cfg.borderColorArgb),
                    borderThicknessDp=i.getFloatExtra(CursorOverlayService.EXTRA_BORDER_THICK,cfg.borderThicknessDp),
                    opacity=i.getFloatExtra(CursorOverlayService.EXTRA_OPACITY,cfg.opacity))
                overlay?.updateConfig(cfg)
            }
        }
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        wm=getSystemService(WINDOW_SERVICE) as WindowManager
        val v=CursorOverlayView(this,cfg)
        val p=WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT)
        wm.addView(v,p); overlay=v
        val f=IntentFilter(CursorOverlayService.ACTION_UPDATE_CONFIG)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) registerReceiver(receiver,f,RECEIVER_NOT_EXPORTED)
        else @Suppress("UnspecifiedRegisterReceiverFlag") registerReceiver(receiver,f)
    }
    override fun onAccessibilityEvent(e: AccessibilityEvent?){}
    override fun onInterrupt(){overlay?.let{runCatching{wm.removeView(it)}};overlay=null}
    override fun onDestroy(){onInterrupt();runCatching{unregisterReceiver(receiver)};super.onDestroy()}
}
class CursorOverlayView(ctx: Context, private var cfg: CursorConfig) : View(ctx) {
    private var cx=-999f; private var cy=-999f
    private val fp=Paint(Paint.ANTI_ALIAS_FLAG)
    private val bp=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE}
    init{apply()}
    fun updateConfig(c: CursorConfig){cfg=c;apply();invalidate()}
    private fun apply(){fp.color=cfg.fillColorArgb;fp.alpha=(cfg.opacity*255).toInt();bp.color=cfg.borderColorArgb;bp.strokeWidth=cfg.borderThicknessDp*resources.displayMetrics.density}
    override fun onGenericMotionEvent(e: MotionEvent): Boolean {
        if(e.source and InputDevice.SOURCE_MOUSE!=0){cx=e.x;cy=e.y;invalidate();return true}
        return super.onGenericMotionEvent(e)
    }
    override fun onDraw(canvas: Canvas) {
        if(cx<0) return
        val s=cfg.sizeDp*resources.displayMetrics.density
        when(cfg.shape){
            CursorShape.ARROW->{val p=Path().apply{moveTo(cx,cy);lineTo(cx,cy+s*.75f);lineTo(cx+s*.28f,cy+s*.55f);lineTo(cx+s*.44f,cy+s*.92f);lineTo(cx+s*.56f,cy+s*.88f);lineTo(cx+s*.4f,cy+s*.5f);lineTo(cx+s*.62f,cy+s*.5f);close()};canvas.drawPath(p,fp);canvas.drawPath(p,bp)}
            CursorShape.CIRCLE->{canvas.drawCircle(cx+s/2,cy+s/2,s/2,fp);canvas.drawCircle(cx+s/2,cy+s/2,s/2,bp)}
            CursorShape.CROSSHAIR->{val g=s*.15f;canvas.drawLine(cx-s/2,cy,cx-g,cy,fp);canvas.drawLine(cx+g,cy,cx+s/2,cy,fp);canvas.drawLine(cx,cy-s/2,cx,cy-g,fp);canvas.drawLine(cx,cy+g,cx,cy+s/2,fp);canvas.drawCircle(cx,cy,g,bp)}
            CursorShape.DIAMOND->{val p=Path().apply{moveTo(cx+s/2,cy);lineTo(cx+s,cy+s/2);lineTo(cx+s/2,cy+s);lineTo(cx,cy+s/2);close()};canvas.drawPath(p,fp);canvas.drawPath(p,bp)}
            CursorShape.STAR->{val p=Path();val ocx=cx+s/2;val ocy=cy+s/2;for(i in 0 until 10){val a=Math.PI/5*i-Math.PI/2;val r=if(i%2==0)s/2 else s/2*.4f;val x=ocx+r*cos(a).toFloat();val y=ocy+r*sin(a).toFloat();if(i==0)p.moveTo(x,y) else p.lineTo(x,y)};p.close();canvas.drawPath(p,fp);canvas.drawPath(p,bp)}
            CursorShape.HAND->{val p=Path().apply{moveTo(cx+s*.3f,cy+s*.4f);lineTo(cx+s*.3f,cy+s*.85f);lineTo(cx+s*.7f,cy+s*.85f);lineTo(cx+s*.7f,cy+s*.25f);lineTo(cx+s*.62f,cy+s*.25f);lineTo(cx+s*.62f,cy+s*.15f);lineTo(cx+s*.52f,cy+s*.15f);lineTo(cx+s*.52f,cy+s*.1f);lineTo(cx+s*.42f,cy+s*.1f);lineTo(cx+s*.42f,cy+s*.15f);lineTo(cx+s*.38f,cy+s*.15f);lineTo(cx+s*.38f,cy+s*.25f);lineTo(cx+s*.3f,cy+s*.25f);close()};canvas.drawPath(p,fp);canvas.drawPath(p,bp)}
        }
    }
}
