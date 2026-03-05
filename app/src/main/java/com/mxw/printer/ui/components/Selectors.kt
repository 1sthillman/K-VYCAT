package com.mxw.printer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxw.printer.model.*
import com.mxw.printer.ui.theme.*

@Composable
fun PaperSizeSelector(
    selected: PaperSize,
    onSelect: (PaperSize) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PaperSize.entries) { size ->
            PaperSizeChip(
                size = size,
                isSelected = size == selected,
                onClick = { onSelect(size) }
            )
        }
    }
}

@Composable
fun PaperSizeChip(
    size: PaperSize,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.15f) else SurfaceVariant)
            .border(
                width = 2.dp,
                color = if (isSelected) Primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                size.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Primary else TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (size.isContinuous()) {
                Text(
                    "Sürekli",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Primary else TextSecondary
                )
            }
        }
    }
}

@Composable
fun OrientationSelector(
    selected: PrintOrientation,
    onSelect: (PrintOrientation) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OrientationButton(
            orientation = PrintOrientation.PORTRAIT,
            isSelected = selected == PrintOrientation.PORTRAIT,
            onClick = { onSelect(PrintOrientation.PORTRAIT) },
            modifier = Modifier.weight(1f)
        )
        OrientationButton(
            orientation = PrintOrientation.LANDSCAPE,
            isSelected = selected == PrintOrientation.LANDSCAPE,
            onClick = { onSelect(PrintOrientation.LANDSCAPE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OrientationButton(
    orientation: PrintOrientation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, label) = when (orientation) {
        PrintOrientation.PORTRAIT -> Icons.Default.PhoneAndroid to "Dikey"
        PrintOrientation.LANDSCAPE -> Icons.Default.PhoneAndroid to "Yatay" // Rotated icon
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.15f) else SurfaceVariant)
            .border(
                width = 2.dp,
                color = if (isSelected) Primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Primary else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Primary else TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun HeatLevelSlider(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Isı: $value%",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                when {
                    value < 30 -> "Düşük"
                    value < 60 -> "Orta"
                    value < 85 -> "Yüksek"
                    else -> "Maksimum"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    value < 30 -> Color(0xFF60A5FA)
                    value < 60 -> Success
                    value < 85 -> Warning
                    else -> Error
                }
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = SurfaceVariant
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0%", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            Text("50%", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            Text("100%", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
    }
}

@Composable
fun CopyCounter(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Adet",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (value > 1) onValueChange(value - 1) },
                enabled = value > 1
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = null,
                    tint = if (value > 1) Primary else TextTertiary
                )
            }
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = { if (value < 99) onValueChange(value + 1) },
                enabled = value < 99
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = if (value < 99) Primary else TextTertiary
                )
            }
        }
    }
}

@Composable
fun FontSizeSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (value == 0f) "Otomatik" else "${value.toInt()}px",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..200f,
            steps = 39,
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = SurfaceVariant
            )
        )
    }
}

@Composable
fun TextAlignSelector(
    selected: TextAlign,
    onSelect: (TextAlign) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextAlignButton(
            align = TextAlign.LEFT,
            icon = Icons.Default.FormatAlignLeft,
            isSelected = selected == TextAlign.LEFT,
            onClick = { onSelect(TextAlign.LEFT) },
            modifier = Modifier.weight(1f)
        )
        TextAlignButton(
            align = TextAlign.CENTER,
            icon = Icons.Default.FormatAlignCenter,
            isSelected = selected == TextAlign.CENTER,
            onClick = { onSelect(TextAlign.CENTER) },
            modifier = Modifier.weight(1f)
        )
        TextAlignButton(
            align = TextAlign.RIGHT,
            icon = Icons.Default.FormatAlignRight,
            isSelected = selected == TextAlign.RIGHT,
            onClick = { onSelect(TextAlign.RIGHT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TextAlignButton(
    align: TextAlign,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.15f) else SurfaceVariant)
            .border(
                width = 2.dp,
                color = if (isSelected) Primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) Primary else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}
