package com.example.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.db.dao.CategoryBreakdown
import com.example.expensetracker.util.formatMoney

@Composable
fun CategoryPieChart(
    breakdown: List<CategoryBreakdown>,
    modifier: Modifier = Modifier
) {
    if (breakdown.isEmpty()) {
        Text("暂无数据", modifier = modifier, style = MaterialTheme.typography.bodyMedium)
        return
    }

    val total = breakdown.sumOf { it.total }
    val textColorArgb = MaterialTheme.colorScheme.onSurface.toArgb()

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val canvasSize = minOf(size.width, size.height)
            val topLeft = Offset(
                (size.width - canvasSize) / 2,
                (size.height - canvasSize) / 2
            )
            val arcSize = Size(canvasSize, canvasSize)
            val strokeWidth = canvasSize * 0.25f

            var startAngle = -90f
            breakdown.forEach { item ->
                val sweepAngle = (item.total / total * 360).toFloat()
                drawArc(
                    color = Color(item.colorHex),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }

            // Center total text
            drawContext.canvas.nativeCanvas.drawText(
                total.formatMoney(),
                size.width / 2,
                size.height / 2 + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColorArgb
                    textSize = 12.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
            )
        }

        // Legend
        Column(modifier = Modifier.padding(top = 8.dp)) {
            breakdown.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = Color(item.colorHex))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        item.total.formatMoney(),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "%.0f%%".format(item.total / total * 100),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
