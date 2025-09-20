package com.orielle.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.orielle.ui.util.ScreenUtils
import com.orielle.R
import com.orielle.domain.model.DayMoodData
import com.orielle.domain.model.MoodType
import com.orielle.domain.model.WeeklyMoodView
import com.orielle.ui.theme.*

@Composable
fun WeeklyMoodView(
    weeklyView: WeeklyMoodView,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkGray
    val accentColor = WaterBlue



    // Breathing animation for today's indicator
    val breathingTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by breathingTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ), label = "breathingScale"
    )

    // Debug logging
    androidx.compose.runtime.LaunchedEffect(weeklyView) {
        println("WeeklyMoodView: Received data with ${weeklyView.days.size} days")
        if (weeklyView.days.isEmpty()) {
            println("WeeklyMoodView: WARNING - No days data!")
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "YOUR INNER WEATHER",
            style = Typography.bodyMedium.copy(color = if (isDark) SoftSand else Charcoal),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(ScreenUtils.responsivePadding()))

        if (weeklyView.days.isEmpty()) {
            // Temporary fallback - generate basic week structure with correct today calculation
            val fallbackDays = listOf("M", "T", "W", "T", "F", "S", "S")
            val calendar = java.util.Calendar.getInstance()
            val todayDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            // Convert to Monday=0 based index (Sunday=7 -> 6, Monday=2 -> 0, etc.)
            val todayIndex = when (todayDayOfWeek) {
                java.util.Calendar.MONDAY -> 0
                java.util.Calendar.TUESDAY -> 1
                java.util.Calendar.WEDNESDAY -> 2
                java.util.Calendar.THURSDAY -> 3
                java.util.Calendar.FRIDAY -> 4
                java.util.Calendar.SATURDAY -> 5
                java.util.Calendar.SUNDAY -> 6
                else -> 0
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                fallbackDays.forEachIndexed { index, dayLabel ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayLabel,
                            style = Typography.bodyMedium.copy(color = if (isDark) SoftSand else Charcoal)
                        )
                        Spacer(Modifier.height(ScreenUtils.responsiveTextSpacing()))
                        Box(
                            modifier = Modifier
                                .size(ScreenUtils.responsiveIconSize(40.dp))
                                .background(
                                    color = if (index == todayIndex) WaterBlue.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(ScreenUtils.responsiveIconSize(20.dp))
                                    .background(
                                        color = if (isDark) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f) else Charcoal.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weeklyView.days.forEachIndexed { index, dayData ->
                    WeeklyMoodDayItem(
                        dayData = dayData,
                        isToday = dayData.isToday,
                        breathingScale = if (dayData.isToday) breathingScale else 1f,
                        accentColor = accentColor,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyMoodDayItem(
    dayData: DayMoodData,
    isToday: Boolean,
    breathingScale: Float,
    accentColor: Color,
    isDark: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayData.dayLabel,
            style = Typography.bodyMedium.copy(color = if (isDark) SoftSand else Charcoal)
        )
        Spacer(Modifier.height(ScreenUtils.responsiveTextSpacing()))
        Box(
            modifier = Modifier
                .size(ScreenUtils.responsiveIconSize(40.dp))
                .scale(breathingScale),
            contentAlignment = Alignment.Center
        ) {
            // Background circle for today
            if (isToday) {
                Box(
                    modifier = Modifier
                        .size(ScreenUtils.responsiveIconSize(40.dp))
                        .background(
                            color = WaterBlue.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                )
            }

            // Mood icon or empty state
            if (dayData.moodCheckIn != null) {
                // Show the actual mood icon
                val moodType = MoodType.fromString(dayData.moodCheckIn.mood)
                val iconRes = moodType?.iconResId ?: R.drawable.ic_peaceful // fallback

                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Mood: ${dayData.moodCheckIn.mood}",
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
                )
            } else {
                // Show empty state indicator
                EmptyDayIndicator(
                    isToday = isToday,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun EmptyDayIndicator(
    isToday: Boolean,
    isDark: Boolean
) {
    // Show a subtle indicator for days without check-ins
    Box(
        modifier = Modifier
            .size(ScreenUtils.responsiveIconSize(20.dp))
            .background(
                color = if (isToday) {
                    // For today, show a more prominent empty state
                    if (isDark) Color.White.copy(alpha = 0.4f) else Charcoal.copy(alpha = 0.4f)
                } else {
                    // For other days, show a very subtle indicator
                    if (isDark) Color.White.copy(alpha = 0.2f) else Charcoal.copy(alpha = 0.2f)
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Small dot or dash to indicate missing data
        Box(
            modifier = Modifier
                .size(if (isToday) 8.dp else 6.dp)
                .background(
                    color = if (isToday) {
                        if (isDark) Color.White.copy(alpha = 0.6f) else Charcoal.copy(alpha = 0.6f)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.3f) else Charcoal.copy(alpha = 0.3f)
                    },
                    shape = CircleShape
                )
        )
    }
}
