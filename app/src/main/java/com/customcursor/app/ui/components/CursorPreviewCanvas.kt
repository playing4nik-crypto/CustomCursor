package com.customcursor.app.ui.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.customcursor.app.model.CursorConfig
import com.customcursor.app.model.CursorShape
import kotlin.math.*
@Composable
fun CursorPreviewCanvas(config: CursorConfig, modifier: Modifier=Modifier) {
    val s by animateFloatAsState(config.sizeDp, tween(250), label="s")
    Box(modifier.height(180.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant).border(1.dp,MaterialTheme.colorScheme.outline,RoundedCornerShape(24.dp)),Alignment.Center){
        Canvas(Modifier.size(s.dp)){
            val fill=Color(config.fillColorArgb).copy(alpha=config.opacity); val border=Color(config.borderColorArgb)
            val stroke=Stroke(config.borderThicknessDp*density, cap=StrokeCap.Round, join=StrokeJoin.Round)
            val sz=s*density; val cx=size.width/2f; val cy=size.height/2f
            when(config.shape){
                CursorShape.ARROW->{val p=Path().apply{moveTo(0f,0f);lineTo(0f,sz*.75f);lineTo(sz*.28f,sz*.55f);lineTo(sz*.44f,sz*.92f);lineTo(sz*.56f,sz*.88f);lineTo(sz*.4f,sz*.5f);lineTo(sz*.62f,sz*.5f);close()};drawPath(p,fill);drawPath(p,border,style=stroke)}
                CursorShape.CIRCLE->{drawCircle(fill,sz/2,Offset(cx,cy));drawCircle(border,sz/2,Offset(cx,cy),style=stroke)}
                CursorShape.CROSSHAIR->{val g=sz*.15f;drawLine(fill,Offset(cx-sz/2,cy),Offset(cx-g,cy),stroke.width);drawLine(fill,Offset(cx+g,cy),Offset(cx+sz/2,cy),stroke.width);drawLine(fill,Offset(cx,cy-sz/2),Offset(cx,cy-g),stroke.width);drawLine(fill,Offset(cx,cy+g),Offset(cx,cy+sz/2),stroke.width);drawCircle(border,g,Offset(cx,cy),style=stroke)}
                CursorShape.DIAMOND->{val p=Path().apply{moveTo(cx,cy-sz/2);lineTo(cx+sz/2,cy);lineTo(cx,cy+sz/2);lineTo(cx-sz/2,cy);close()};drawPath(p,fill);drawPath(p,border,style=stroke)}
                CursorShape.STAR->{val p=Path();for(i in 0 until 10){val a=Math.PI/5*i-Math.PI/2;val r=if(i%2==0)sz/2 else sz/2*.4f;val x=cx+r*cos(a).toFloat();val y=cy+r*sin(a).toFloat();if(i==0)p.moveTo(x,y) else p.lineTo(x,y)};p.close();drawPath(p,fill);drawPath(p,border,style=stroke)}
                CursorShape.HAND->{val p=Path().apply{moveTo(sz*.3f,sz*.4f);lineTo(sz*.3f,sz*.85f);lineTo(sz*.7f,sz*.85f);lineTo(sz*.7f,sz*.25f);lineTo(sz*.62f,sz*.25f);lineTo(sz*.62f,sz*.15f);lineTo(sz*.52f,sz*.15f);lineTo(sz*.52f,sz*.1f);lineTo(sz*.42f,sz*.1f);lineTo(sz*.42f,sz*.15f);lineTo(sz*.38f,sz*.15f);lineTo(sz*.38f,sz*.25f);lineTo(sz*.3f,sz*.25f);close()};drawPath(p,fill);drawPath(p,border,style=stroke)}
            }
        }
    }
}
