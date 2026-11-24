package compress.joshattic.us.ui.tabs

import compress.joshattic.us.model.getLocalizedLabel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material3.Slider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compress.joshattic.us.R
import compress.joshattic.us.model.CompressorUiState
import compress.joshattic.us.model.QualityPreset
import compress.joshattic.us.model.WHATSAPP_ULTRA_TARGET_MB
import androidx.media3.common.MimeTypes
import compress.joshattic.us.utils.ExpressiveSpatialSpring
import compress.joshattic.us.utils.expressiveScale
import compress.joshattic.us.viewmodel.CompressorViewModel

@Composable
fun PresetsTab(state: CompressorUiState, viewModel: CompressorViewModel) {
    val scrollState = rememberScrollState()
    val haptics = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        
        var showPlayer by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        val playUri = state.selectedUri
        val previewContext = androidx.compose.ui.platform.LocalContext.current
        if (playUri != null) {
            androidx.compose.material3.OutlinedButton(
                onClick = { showPlayer = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.player_play_original))
            }
            androidx.compose.material3.OutlinedButton(
                onClick = { viewModel.previewCompress(previewContext) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.preview_button))
            }
            Text(
                stringResource(R.string.preview_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (showPlayer) {
                compress.joshattic.us.ui.components.VideoPlayerDialog(uri = playUri, onDismiss = { showPlayer = false })
            }
        }

        Text(
            stringResource(R.string.crop_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        val cropNone = stringResource(R.string.crop_none)
        val cropOptions = listOf(0f to cropNone, 1f to "1:1", 4f / 5f to "4:5", 9f / 16f to "9:16", 16f / 9f to "16:9")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cropOptions.forEach { (ratio, label) ->
                FilterChip(
                    selected = kotlin.math.abs(state.cropRatio - ratio) < 0.001f,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.setCropRatio(ratio)
                    },
                    label = { Text(label) }
                )
            }
        }
        Text(
            stringResource(R.string.crop_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )

        if (state.durationMs > 5_000L) {
            Text(
                stringResource(R.string.trim_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val fullLabel = stringResource(R.string.trim_full)
            val lengthOptions = listOf(0L to fullLabel, 30_000L to "30 s", 60_000L to "60 s", 90_000L to "90 s")
                .filter { it.first == 0L || it.first < state.durationMs }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lengthOptions.forEach { (lengthMs, label) ->
                    FilterChip(
                        selected = state.trimLengthMs == lengthMs,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setTrim(state.trimStartMs, lengthMs)
                        },
                        label = { Text(label) }
                    )
                }
            }
            if (state.trimLengthMs > 0L) {
                val maxStartSec = ((state.durationMs - state.trimLengthMs) / 1000f).coerceAtLeast(0f)
                var startSec by remember(state.trimStartMs, state.trimLengthMs) {
                    mutableFloatStateOf(state.trimStartMs / 1000f)
                }
                fun clock(seconds: Float): String {
                    val total = seconds.toInt()
                    return String.format("%d:%02d", total / 60, total % 60)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.trim_info, clock(startSec), clock(state.effectiveDurationMs / 1000f)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (maxStartSec > 0.5f) {
                    Slider(
                        value = startSec.coerceIn(0f, maxStartSec),
                        onValueChange = { startSec = it },
                        onValueChangeFinished = {
                            viewModel.setTrim((startSec * 1000f).toLong(), state.trimLengthMs)
                        },
                        valueRange = 0f..maxStartSec
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            stringResource(R.string.quality_preset),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val presets = listOf(
            Triple(QualityPreset.HIGH, state.highPresetConfig.label ?: stringResource(R.string.preset_high), stringResource(R.string.preset_high_desc)),
            Triple(QualityPreset.MEDIUM, state.mediumPresetConfig.label ?: stringResource(R.string.preset_medium), stringResource(R.string.preset_medium_desc)),
            Triple(QualityPreset.LOW, state.lowPresetConfig.label ?: stringResource(R.string.preset_low), stringResource(R.string.preset_low_desc))
        )
        
        presets.forEach { (preset, title, sub) ->
            val selected = state.activePreset == preset
            val isEnabled = when(preset) {
                QualityPreset.MEDIUM -> state.originalHeight >= 1080 
                QualityPreset.LOW -> state.originalHeight >= 720
                else -> true
            }

            val selectionScale by animateFloatAsState(if (selected) 1.02f else 1f, animationSpec = ExpressiveSpatialSpring, label = "selectionScale")
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val pressScale by animateFloatAsState(if (isPressed) 0.96f else 1f, animationSpec = ExpressiveSpatialSpring, label = "pressScale")
            
            OutlinedCard(
                onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.applyPreset(preset) 
                },
                enabled = isEnabled,
                shape = RoundedCornerShape(24.dp),
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .graphicsLayer { 
                        scaleX = selectionScale * pressScale
                        scaleY = selectionScale * pressScale
                    },
                colors = if (selected) {
                    CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                } else {
                    CardDefaults.outlinedCardColors(
                        containerColor = if (isEnabled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha=0.3f),
                        contentColor = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha=0.38f)
                    )
                },
                border = if (selected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, if (isEnabled) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.38f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha=0.38f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            sub, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.38f)
                        )
                    }
                    if (selected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
        
        run {
            val originalMb = state.originalSize.toFloat() / (1024f * 1024f)
            val ultraEnabled = originalMb > WHATSAPP_ULTRA_TARGET_MB
            val ultraSelected = ultraEnabled &&
                state.activePreset == QualityPreset.CUSTOM &&
                state.videoCodec == MimeTypes.VIDEO_H264 &&
                kotlin.math.abs(state.targetSizeMb - WHATSAPP_ULTRA_TARGET_MB) < 0.05f

            OutlinedCard(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.applyWhatsAppUltra()
                },
                enabled = ultraEnabled,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = if (ultraSelected) {
                    CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                } else {
                    CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                },
                border = if (ultraSelected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.ultra_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            stringResource(if (ultraEnabled) R.string.ultra_desc else R.string.ultra_small_enough),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (ultraEnabled && state.effectiveDurationMs in 45_001L..90_000L) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.ultra_quality_note),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (ultraEnabled && state.effectiveDurationMs > 90_000L) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.ultra_long_note),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    if (ultraSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        }

        run {
            val smartSelected = state.smartQualitySizeMb > 0f &&
                state.activePreset == QualityPreset.CUSTOM &&
                kotlin.math.abs(state.targetSizeMb - state.smartQualitySizeMb) < 0.05f
            OutlinedCard(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.applySmartQuality()
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = if (smartSelected) {
                    CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                } else {
                    CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                },
                border = if (smartSelected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.smart_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            stringResource(R.string.smart_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (smartSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val sizePresets = state.targetSizePresets.filter { preset -> 
            preset.sizeMb < (state.originalSize.toFloat() / (1024f * 1024f))
        }

        if (state.showTargetSizePreset && sizePresets.isNotEmpty()) {
            Text(
                stringResource(R.string.target_size_limits),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sizePresets.forEach { preset ->
                    val interactionSource = remember { MutableInteractionSource() }
                    FilterChip(
                        selected = state.targetSizeMb == preset.sizeMb,
                        onClick = { 
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.selectTargetSizePreset(preset.sizeMb) 
                        },
                        interactionSource = interactionSource,
                        label = { 
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val unitGb = stringResource(R.string.unit_gb)
                                val unitMb = stringResource(R.string.unit_mb)
                                val unitKb = stringResource(R.string.unit_kb)
                                val formattedSize = when {
                                    preset.sizeMb >= 1024f -> "${(preset.sizeMb / 1024f).toInt()} $unitGb"
                                    preset.sizeMb < 1f -> "${(preset.sizeMb * 1024f).toInt()} $unitKb"
                                    else -> "${preset.sizeMb.toInt()} $unitMb"
                                }
                                Text(
                                    formattedSize, 
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(preset.getLocalizedLabel(), style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .expressiveScale(interactionSource)
                    )
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 24.dp))
        val audioContext = androidx.compose.ui.platform.LocalContext.current
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.extractAudio(audioContext) },
            enabled = !state.audioExtracting && state.selectedUri != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.audio_extract_button))
        }
        Text(
            state.audioExtractMessage ?: stringResource(R.string.audio_extract_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
