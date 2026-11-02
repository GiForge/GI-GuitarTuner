package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.PitchResult
import com.example.dsp.TuningPreset
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldTune
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StringPegsSelector(
    tuning: TuningPreset,
    lockedStringIndex: Int?,
    isAutoString: Boolean,
    currentPitchResult: PitchResult,
    isSignalActive: Boolean,
    onSelectString: (Int?) -> Unit,
    onPlayTone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioCardElevated)
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        // Header: FIXED HEIGHT 24.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CORDE (${tuning.name})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Auto Mode Button: fixed height 24.dp
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isAutoString) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF1E2638))
                    .border(
                        1.dp,
                        if (isAutoString) ElectricCyan else StudioBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectString(null) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoMode,
                        contentDescription = "Auto String Mode",
                        tint = if (isAutoString) ElectricCyan else TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AUTO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isAutoString) ElectricCyan else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Row of Strings / Pegs: FIXED HEIGHT 70.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tuning.strings.forEachIndexed { index, stringNoteName ->
                val isSelected = lockedStringIndex == index
                val detectedNoteName = currentPitchResult.note?.fullName
                val isDetectedString = isSignalActive && detectedNoteName.equals(stringNoteName, ignoreCase = true)
                val isTuned = isDetectedString && currentPitchResult.isInTune

                val borderAnimColor by animateColorAsState(
                    targetValue = when {
                        isTuned -> EmeraldTune
                        isSelected -> ElectricCyan
                        isDetectedString -> ElectricCyan.copy(alpha = 0.7f)
                        else -> StudioBorder
                    },
                    label = "peg_border"
                )

                val bgAnimColor by animateColorAsState(
                    targetValue = when {
                        isTuned -> EmeraldTune.copy(alpha = 0.18f)
                        isSelected -> ElectricCyan.copy(alpha = 0.15f)
                        isDetectedString -> Color(0xFF1B2434)
                        else -> Color(0xFF121722)
                    },
                    label = "peg_bg"
                )

                // Fixed size peg card (width = 54.dp, height = 66.dp) so nothing ever shifts
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(66.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgAnimColor)
                        .border(1.5.dp, borderAnimColor, RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectString(if (isSelected) null else index)
                        }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .testTag("string_peg_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // String Number badge (e.g. 1ª, 6ª)
                        Text(
                            text = "${index + 1}ª",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )

                        // Note Name (e.g. E2, G3) with fixed height
                        Box(
                            modifier = Modifier.height(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringNoteName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTuned) EmeraldTune else if (isSelected) ElectricCyan else TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Audio Tone Preview Icon Button
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2838))
                                .clickable { onPlayTone(stringNoteName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTuned) Icons.Default.Check else Icons.Default.VolumeUp,
                                contentDescription = "Ascolta $stringNoteName",
                                tint = if (isTuned) EmeraldTune else ElectricCyan,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
