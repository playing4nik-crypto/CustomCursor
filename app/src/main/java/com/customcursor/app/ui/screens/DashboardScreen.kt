package com.customcursor.app.ui.screens
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.customcursor.app.model.CursorShape
import com.customcursor.app.service.CursorOverlayService
import com.customcursor.app.ui.components.ColorPickerDialog
import com.customcursor.app.ui.components.CursorPreviewCanvas
import com.customcursor.app.ui.theme.NeonCyan
import com.customcursor.app.viewmodel.CursorViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: CursorViewModel) {
    val config by vm.config.collectAsStateWithLifecycle()
    val ctx=LocalContext.current; val view=LocalView.current
    var showFill by remember{mutableStateOf(false)}; var showBorder by remember{mutableStateOf(false)}
    var applied by remember{mutableStateOf(false)}
    val serviceOn=remember{mutableStateOf(isAccOn(ctx))}
    val scale by animateFloatAsState(if(applied) 0.95f else 1f, spring(Spring.DampingRatioMediumBouncy),"s")
    Scaffold(containerColor=MaterialTheme.colorScheme.background,
        topBar={CenterAlignedTopAppBar(title={Text("Custom Cursor",style=MaterialTheme.typography.titleLarge.copy(fontWeight=FontWeight.Bold),color=NeonCyan)},
            colors=TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor=MaterialTheme.colorScheme.background),
            actions={IconButton(onClick={ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}){Icon(Icons.Outlined.Tune,null,tint=NeonCyan)}})}
    ){pad->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=16.dp,vertical=8.dp),Arrangement.spacedBy(20.dp)){
            val brush=if(serviceOn.value) Brush.horizontalGradient(listOf(Color(0xFF1B5E20),Color(0xFF2E7D32))) else Brush.horizontalGradient(listOf(Color(0xFF4A148C),Color(0xFF6A1B9A)))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(brush).clickable(enabled=!serviceOn.value){ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}.padding(16.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Icon(if(serviceOn.value)Icons.Filled.CheckCircle else Icons.Outlined.Tune,null,tint=Color.White,modifier=Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column{Text(if(serviceOn.value)"Service: Active" else "Service: Off — Tap to enable",color=Color.White,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.bodyMedium)}
                }
            }
            Card("Live Preview"){CursorPreviewCanvas(config,Modifier.fillMaxWidth())}
            Card("Shape"){LazyRow(horizontalArrangement=Arrangement.spacedBy(10.dp)){items(CursorShape.entries){sh->val sel=sh==config.shape;Surface(RoundedCornerShape(12.dp),color=if(sel)NeonCyan else MaterialTheme.colorScheme.surface,modifier=Modifier.clickable{vm.updateShape(sh)}.border(if(sel)0.dp else 1.dp,MaterialTheme.colorScheme.outline,RoundedCornerShape(12.dp))){Text(sh.label,color=if(sel)Color.Black else MaterialTheme.colorScheme.onSurface,fontWeight=if(sel)FontWeight.Bold else FontWeight.Normal,modifier=Modifier.padding(horizontal=16.dp,vertical=10.dp))}}}}
            Card("Size  ${config.sizeDp.toInt()} dp"){Slider(config.sizeDp,vm::updateSize,valueRange=16f..80f,colors=SliderDefaults.colors(thumbColor=NeonCyan,activeTrackColor=NeonCyan,inactiveTrackColor=NeonCyan.copy(.2f)))}
            Card("Colors"){Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(12.dp)){listOf("Fill" to Color(config.fillColorArgb),"Border" to Color(config.borderColorArgb)).forEachIndexed{i,(lbl,clr)->Surface(RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.weight(1f).height(72.dp).clickable{if(i==0)showFill=true else showBorder=true}.border(1.dp,MaterialTheme.colorScheme.outline,RoundedCornerShape(16.dp))){Row(Modifier.padding(12.dp),Alignment.CenterVertically,Arrangement.spacedBy(12.dp)){Box(Modifier.size(36.dp).background(clr,RoundedCornerShape(8.dp)).border(1.dp,MaterialTheme.colorScheme.outline,RoundedCornerShape(8.dp)));Column{Text(lbl,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurface.copy(.6f));Text("Tap to change",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurface.copy(.4f))}}}}}}
            Card("Border Thickness  ${"%.1f".format(config.borderThicknessDp)} dp"){Slider(config.borderThicknessDp,vm::updateBorderThickness,valueRange=0f..8f,colors=SliderDefaults.colors(thumbColor=NeonCyan,activeTrackColor=NeonCyan,inactiveTrackColor=NeonCyan.copy(.2f)))}
            Card("Opacity  ${(config.opacity*100).toInt()}%"){Slider(config.opacity,vm::updateOpacity,valueRange=0.2f..1f,colors=SliderDefaults.colors(thumbColor=NeonCyan,activeTrackColor=NeonCyan,inactiveTrackColor=NeonCyan.copy(.2f)))}
            Button(onClick={
                if(Build.VERSION.SDK_INT>=35)view.performHapticFeedback(HapticFeedbackConstants.CONFIRM) else view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                vm.applyAndPersist()
                val si=Intent(ctx,CursorOverlayService::class.java)
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)ctx.startForegroundService(si) else ctx.startService(si)
                applied=true
            },Modifier.fillMaxWidth().height(60.dp).scale(scale),RoundedCornerShape(18.dp),ButtonDefaults.buttonColors(containerColor=NeonCyan)){
                AnimatedVisibility(applied,enter=fadeIn()+scaleIn(),exit=fadeOut()){Row{Icon(Icons.Filled.CheckCircle,null,tint=Color.Black);Spacer(Modifier.width(8.dp))}}
                Text(if(applied)"Applied!" else "Set Now",Color.Black,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
    if(showFill)ColorPickerDialog("Fill Color",Color(config.fillColorArgb),vm::updateFillColor){showFill=false}
    if(showBorder)ColorPickerDialog("Border Color",Color(config.borderColorArgb),vm::updateBorderColor){showBorder=false}
}
@Composable fun Card(title:String,content:@Composable ColumnScope.()->Unit){Surface(RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surfaceVariant,tonalElevation=4.dp){Column(Modifier.padding(16.dp)){Text(title,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurface.copy(.6f));Spacer(Modifier.height(12.dp));content()}}}
fun isAccOn(ctx:Context):Boolean{val n="${ctx.packageName}/.accessibility.CursorAccessibilityService";return(Settings.Secure.getString(ctx.contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?:"").split(':').any{it.equals(n,true)}}
