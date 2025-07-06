package com.orielle.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orielle.R // <-- CORRECTED: Added the missing import for R file
import com.orielle.ui.theme.OrielleTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MoodArcGaugeCard(
    moods: List<Mood>,
    modifier: Modifier = Modifier
) {
    var targetAngle by remember { mutableFloatStateOf(-90f) }
    val animatedAngle by animateFloatAsState(targetValue = targetAngle, label = "MoodAngleAnimation")
    val selectedMood = angleToMood(animatedAngle, moods)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How do you feel?",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            MoodGauge(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                moods = moods,
                selectedAngle = animatedAngle,
                onAngleChange = { newAngle -> targetAngle = newAngle }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selectedMood.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = selectedMood.color
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Save the selected mood */ },
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp)
            ) {
                Text(text = "I Feel ${selectedMood.name}")
            }
        }
    }
}

@Composable
private fun MoodGauge(
    modifier: Modifier = Modifier,
    moods: List<Mood>,
    selectedAngle: Float,
    onAngleChange: (Float) -> Unit
) {
    val moodPainters = moods.map { painterResource(id = it.iconResId) }
    val arcBackgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val center = Offset(size.width / 2f, size.height * 1.1f)
                    val touchPoint = change.position
                    val newAngle = calculateAngle(touchPoint, center)
                    onAngleChange(newAngle)
                    change.consume()
                }
            }
    ) {
        val arcCenter = Offset(x = size.width / 2f, y = size.height * 1.1f)
        val arcRadius = size.width / 2.3f
        val sweepAngle = 160f
        val startAngle = -170f

        drawArc(
            color = arcBackgroundColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 36.dp.toPx(), cap = StrokeCap.Round)
        )

        val indicatorSweep = (selectedAngle - startAngle).coerceAtLeast(0f)
        drawArc(
            brush = Brush.sweepGradient(
                colors = moods.map { it.color },
                center = arcCenter
            ),
            startAngle = startAngle,
            sweepAngle = indicatorSweep,
            useCenter = false,
            style = Stroke(width = 36.dp.toPx(), cap = StrokeCap.Round)
        )

        drawMoodIconsOnCanvas(moodPainters, moods, startAngle, sweepAngle, arcCenter, arcRadius)

        drawSleekNeedle(selectedAngle, arcCenter, arcRadius)
    }
}

private fun DrawScope.drawMoodIconsOnCanvas(
    painters: List<Painter>,
    moods: List<Mood>,
    startAngle: Float,
    sweepAngle: Float,
    center: Offset,
    radius: Float
) {
    val segmentAngle = sweepAngle / painters.size
    painters.forEachIndexed { index, painter ->
        val angleForIcon = startAngle + (index * segmentAngle) + (segmentAngle / 2)
        val iconX = center.x + (radius * cos(Math.toRadians(angleForIcon.toDouble())).toFloat())
        val iconY = center.y + (sin(Math.toRadians(angleForIcon.toDouble())).toFloat())
        this.drawIntoCanvas { canvas ->
            val painterIntrinsicSize = painter.intrinsicSize
            canvas.nativeCanvas.save()
            canvas.translate(iconX - painterIntrinsicSize.width / 2, iconY - painterIntrinsicSize.height / 2)
            with(painter) {
                draw(
                    size = painterIntrinsicSize,
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.9f))
                )
            }
            canvas.nativeCanvas.restore()
        }
    }
}


private fun DrawScope.drawSleekNeedle(angle: Float, center: Offset, radius: Float) {
    val needleLength = radius * 1.1f
    val needleBaseRadius = 16.dp.toPx()

    rotate(degrees = angle + 90f, pivot = center) {
        drawLine(
            color = Color.Black.copy(alpha = 0.3f),
            start = center.copy(y = center.y + 4.dp.toPx()),
            end = Offset(center.x, center.y - needleLength + 4.dp.toPx()),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    rotate(degrees = angle + 90f, pivot = center) {
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFEEEEEE), Color(0xFFBDBDBD)),
            ),
            start = Offset(center.x, center.y - needleLength),
            end = center,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.DarkGray, Color.Black),
            center = center,
            radius = needleBaseRadius
        ),
        radius = needleBaseRadius,
        center = center
    )
    drawCircle(
        color = Color.LightGray,
        radius = needleBaseRadius * 0.4f,
        center = center
    )
}

private fun calculateAngle(touchPoint: Offset, center: Offset): Float {
    val dx = touchPoint.x - center.x
    val dy = touchPoint.y - center.y
    val angle = -Math.toDegrees(atan2(dx.toDouble(), dy.toDouble())).toFloat()
    return angle.coerceIn(-170f, -10f)
}

private fun angleToMood(angle: Float, moods: List<Mood>): Mood {
    val totalAngleRange = 160f
    val startOffset = -170f
    val segmentAngle = totalAngleRange / moods.size
    val positiveAngle = angle - startOffset
    val index = (positiveAngle / segmentAngle).toInt().coerceIn(0, moods.lastIndex)
    return moods[index]
}

@Preview(showBackground = true)
@Composable
private fun MoodArcGaugeCardPreview() {
    OrielleTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MoodArcGaugeCard(moods = allMoods)
        }
    }
}