package com.example.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.db.dao.DailyTotal
import com.example.expensetracker.util.toLocalDateCompat

@Composable
fun SpendingTrendChart(
    dailyTotals: List<DailyTotal>,
    daysInMonth: Int = 30,
    modifier: Modifier = Modifier
) {
    if (dailyTotals.isEmpty()) {
        Text("暂无数据", modifier = modifier, style = MaterialTheme.typography.bodyMedium)
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val gridColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val paddingLeft = 44.dp.toPx()
        val paddingRight = 12.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 24.dp.toPx()

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val maxTotal = dailyTotals.maxOf { it.total }.coerceAtLeast(1.0)
        val niceMax = ((maxTotal / 100).toInt() + 1) * 100.0

        // Draw horizontal grid lines
        val gridCount = 4
        val labelPaint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 10.dp.toPx()
            textAlign = android.graphics.Paint.Align.LEFT
        }
        for (i in 0..gridCount) {
            val y = paddingTop + chartHeight * (1 - i.toFloat() / gridCount)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
            )
            val label = "¥${(niceMax * i / gridCount).toInt()}"
            drawContext.canvas.nativeCanvas.drawText(
                label, 4.dp.toPx(), y + 4.dp.toPx(), labelPaint
            )
        }

        // Build data points
        val points = dailyTotals.map { daily ->
            val day = daily.date.toLocalDateCompat().dayOfMonth
            val x = paddingLeft + chartWidth * (day - 1).toFloat() / (daysInMonth - 1).coerceAtLeast(1)
            val y = paddingTop + chartHeight * (1 - (daily.total / niceMax).toFloat())
            Offset(x, y)
        }

        // Draw filled area under the line with vertical gradient
        if (points.size >= 2) {
            val fillPath = Path().apply {
                moveTo(points.first().x, paddingTop + chartHeight)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, paddingTop + chartHeight)
                close()
            }
            val gradient = android.graphics.LinearGradient(
                0f, paddingTop, 0f, paddingTop + chartHeight,
                intArrayOf(primaryColor.copy(alpha = 0.25f).toArgb(), primaryColor.copy(alpha = 0.0f).toArgb()),
                null, android.graphics.Shader.TileMode.CLAMP
            )
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    shader = gradient
                    isAntiAlias = true
                }
                val path = android.graphics.Path().apply {
                    moveTo(points.first().x, paddingTop + chartHeight)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, paddingTop + chartHeight)
                    close()
                }
                drawPath(path, paint)
            }
        }

        // Draw line
        if (points.size >= 2) {
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cx1 = (prev.x + curr.x) / 2
                    val cx2 = (prev.x + curr.x) / 2
                    cubicTo(cx1, prev.y, cx2, curr.y, curr.x, curr.y)
                }
            }
            drawPath(linePath, primaryColor, style = Stroke(width = 2.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }

        // Draw data point dots
        if (points.size <= 31) {
            points.forEach { point ->
                drawCircle(primaryColor, radius = 3.dp.toPx(), center = point)
                drawCircle(Color.White, radius = 1.5.dp.toPx(), center = point)
            }
        }

        // X axis labels
        val xLabelPaint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 10.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val step = (daysInMonth / 6).coerceAtLeast(1)
        val xLabelDays = (1..daysInMonth step step).toList()
        xLabelDays.forEach { day ->
            val x = paddingLeft + chartWidth * (day - 1).toFloat() / (daysInMonth - 1).coerceAtLeast(1)
            drawContext.canvas.nativeCanvas.drawText("$day", x, size.height - 2.dp.toPx(), xLabelPaint)
        }
    }
}
