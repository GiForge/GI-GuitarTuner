package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CalibrationSheet
import com.example.ui.screens.MetronomeScreen
import com.example.ui.screens.RecorderAndPresetsScreen
import com.example.ui.screens.ScalesScreen
import com.example.ui.screens.TunerScreen
import com.example.ui.theme.AccordatoreTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardSurface
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

enum class MainAppTab(val title: String, val icon: ImageVector) {
    TUNER("Accordatore", Icons.Default.GraphicEq),
    METRONOME("Metronomo", Icons.Default.Speed),
    SCALES("Scale", Icons.Default.School),
    RECORDER("Registra", Icons.Default.Bookmark)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccordatoreTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(MainAppTab.TUNER) }
    var showCalibrationSheet by remember { mutableStateOf(false) }

    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordAudioPermission = isGranted
        if (isGranted && currentTab == MainAppTab.TUNER) {
            viewModel.startTuner()
        }
    }

    // Auto start/stop tuner based on current tab and permission
    LaunchedEffect(currentTab, hasRecordAudioPermission) {
        if (currentTab == MainAppTab.TUNER && hasRecordAudioPermission) {
            viewModel.startTuner()
        } else {
            viewModel.stopTuner()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTuner()
        }
    }

    // State collections
    val tunerSettings by viewModel.tunerSettings.collectAsStateWithLifecycle()
    val tunerAudioState by viewModel.tunerAudioState.collectAsStateWithLifecycle()
    val metronomeState by viewModel.metronomeState.collectAsStateWithLifecycle()
    val recorderState by viewModel.recorderState.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val selectedScaleRoot by viewModel.selectedScaleRoot.collectAsStateWithLifecycle()
    val selectedScaleType by viewModel.selectedScaleType.collectAsStateWithLifecycle()
    val isGuitarScaleView by viewModel.isGuitarScaleView.collectAsStateWithLifecycle()
    val activePlayingNote by viewModel.activePlayingNote.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            MainAppTab.TUNER -> "ACCORDATORE PRO"
                            MainAppTab.METRONOME -> "METRONOMO & DRUMS"
                            MainAppTab.SCALES -> "SCALE & TUTORIAL"
                            MainAppTab.RECORDER -> "REGISTRATORE & PRESET"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    if (currentTab == MainAppTab.TUNER) {
                        IconButton(
                            onClick = { showCalibrationSheet = true },
                            modifier = Modifier.testTag("app_bar_calibration_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Calibrazione",
                                tint = ElectricCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioDarkBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioCardSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding().testTag("main_bottom_nav")
            ) {
                MainAppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = ElectricCyan,
                            indicatorColor = ElectricCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = StudioDarkBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainAppTab.TUNER -> {
                    TunerScreen(
                        tunerSettings = tunerSettings,
                        tunerAudioState = tunerAudioState,
                        hasRecordPermission = hasRecordAudioPermission,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onOpenCalibration = { showCalibrationSheet = true },
                        onSelectString = { viewModel.toggleLockString(it) },
                        onPlayReferenceTone = { viewModel.playReferenceTone(it) }
                    )
                }
                MainAppTab.METRONOME -> {
                    MetronomeScreen(
                        metronomeState = metronomeState,
                        onTogglePlay = { viewModel.toggleMetronome() },
                        onSetBpm = { viewModel.setMetronomeBpm(it) },
                        onSetBeatsPerBar = { viewModel.setBeatsPerBar(it) },
                        onSetDrumStyle = { viewModel.setDrumStyle(it) },
                        onTapTempo = { viewModel.tapTempo() },
                        onToggleHaptic = { viewModel.toggleMetronomeHaptic() }
                    )
                }
                MainAppTab.SCALES -> {
                    ScalesScreen(
                        selectedRoot = selectedScaleRoot,
                        selectedScaleType = selectedScaleType,
                        isGuitarView = isGuitarScaleView,
                        activePlayingNote = activePlayingNote,
                        onSelectRoot = { viewModel.setScaleRoot(it) },
                        onSelectScaleType = { viewModel.setScaleType(it) },
                        onToggleInstrument = { viewModel.toggleInstrumentScaleView(it) },
                        onPlayNote = { freq, name -> viewModel.playScaleNote(freq, name) },
                        onPlayFullScale = { viewModel.playFullScale() }
                    )
                }
                MainAppTab.RECORDER -> {
                    RecorderAndPresetsScreen(
                        recorderState = recorderState,
                        presets = presets,
                        onStartRecording = { name ->
                            if (!hasRecordAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.startRecording(name)
                            }
                        },
                        onStopRecording = { viewModel.stopRecording() },
                        onPlayRecording = { rec -> viewModel.playRecording(rec) },
                        onDeleteRecording = { rec -> viewModel.deleteRecording(rec) },
                        onShareRecording = { rec -> viewModel.shareRecording(rec) },
                        onSaveCurrentPreset = { name -> viewModel.saveCurrentAsPreset(name) },
                        onApplyPreset = { preset -> viewModel.applyPreset(preset) },
                        onDeletePreset = { preset -> viewModel.deletePreset(preset) }
                    )
                }
            }
        }

        if (showCalibrationSheet) {
            CalibrationSheet(
                a4Frequency = tunerSettings.a4Frequency,
                noiseThreshold = tunerSettings.noiseGateThreshold,
                currentRms = tunerAudioState.rmsLevel,
                selectedTuning = tunerSettings.selectedTuning,
                onSetA4 = { viewModel.setA4Frequency(it) },
                onSetNoiseThreshold = { viewModel.setNoiseGateThreshold(it) },
                onSelectTuning = { viewModel.selectTuning(it) },
                onDismiss = { showCalibrationSheet = false }
            )
        }
    }
}
