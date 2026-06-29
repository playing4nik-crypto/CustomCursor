package com.customcursor.app.viewmodel
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.customcursor.app.model.CursorConfig
import com.customcursor.app.model.CursorShape
import com.customcursor.app.service.CursorOverlayService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cursor_prefs")
class CursorViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    private val _config = MutableStateFlow(CursorConfig())
    val config: StateFlow<CursorConfig> = _config.asStateFlow()
    private val KEY_SHAPE        = stringPreferencesKey("shape")
    private val KEY_SIZE         = floatPreferencesKey("size")
    private val KEY_FILL_COLOR   = intPreferencesKey("fill_color")
    private val KEY_BORDER_COLOR = intPreferencesKey("border_color")
    private val KEY_BORDER_THICK = floatPreferencesKey("border_thickness")
    private val KEY_OPACITY      = floatPreferencesKey("opacity")
    private val KEY_ENABLED      = booleanPreferencesKey("enabled")
    init { viewModelScope.launch { loadConfig() } }
    private suspend fun loadConfig() {
        val p = dataStore.data.first()
        _config.update { CursorConfig(
            shape=CursorShape.entries.find { s->s.name==p[KEY_SHAPE] }?:CursorShape.ARROW,
            sizeDp=p[KEY_SIZE]?:32f, fillColorArgb=p[KEY_FILL_COLOR]?:it.fillColorArgb,
            borderColorArgb=p[KEY_BORDER_COLOR]?:it.borderColorArgb,
            borderThicknessDp=p[KEY_BORDER_THICK]?:2f, opacity=p[KEY_OPACITY]?:1f,
            isEnabled=p[KEY_ENABLED]?:false
        ) }
    }
    fun updateShape(s: CursorShape)    = _config.update { it.copy(shape=s) }
    fun updateSize(s: Float)           = _config.update { it.copy(sizeDp=s) }
    fun updateFillColor(c: Int)        = _config.update { it.copy(fillColorArgb=c) }
    fun updateBorderColor(c: Int)      = _config.update { it.copy(borderColorArgb=c) }
    fun updateBorderThickness(d: Float)= _config.update { it.copy(borderThicknessDp=d) }
    fun updateOpacity(o: Float)        = _config.update { it.copy(opacity=o) }
    fun applyAndPersist() {
        viewModelScope.launch {
            val cfg = _config.value
            dataStore.edit { p ->
                p[KEY_SHAPE]=cfg.shape.name; p[KEY_SIZE]=cfg.sizeDp
                p[KEY_FILL_COLOR]=cfg.fillColorArgb; p[KEY_BORDER_COLOR]=cfg.borderColorArgb
                p[KEY_BORDER_THICK]=cfg.borderThicknessDp; p[KEY_OPACITY]=cfg.opacity; p[KEY_ENABLED]=true
            }
            _config.update { it.copy(isEnabled=true) }
            val ctx = getApplication<Application>()
            ctx.sendBroadcast(Intent(CursorOverlayService.ACTION_UPDATE_CONFIG).apply {
                putExtra(CursorOverlayService.EXTRA_CONFIG_SHAPE, cfg.shape.name)
                putExtra(CursorOverlayService.EXTRA_CONFIG_SIZE, cfg.sizeDp)
                putExtra(CursorOverlayService.EXTRA_FILL_COLOR, cfg.fillColorArgb)
                putExtra(CursorOverlayService.EXTRA_BORDER_COLOR, cfg.borderColorArgb)
                putExtra(CursorOverlayService.EXTRA_BORDER_THICK, cfg.borderThicknessDp)
                putExtra(CursorOverlayService.EXTRA_OPACITY, cfg.opacity)
                setPackage(ctx.packageName)
            })
        }
    }
}
