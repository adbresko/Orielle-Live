package com.orielle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.orielle.R
import com.orielle.ui.theme.*
import com.orielle.ui.util.ScreenUtils

@Composable
fun BottomNavigation(
    navController: NavController,
    themeManager: com.orielle.ui.theme.ThemeManager
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: "home_graph"
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)
    val backgroundColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenUtils.responsivePadding() * 1.25f, vertical = ScreenUtils.responsivePadding()),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 1.5f),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenUtils.responsivePadding() * 1.25f, vertical = ScreenUtils.responsivePadding()),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardNavItem(
                icon = R.drawable.ic_orielle_drop,
                label = "Home",
                selected = currentRoute == "home_graph",
                isDarkTheme = isDarkTheme,
                onClick = {
                    if (currentRoute != "home_graph") {
                        navController.navigate("home_graph") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
            DashboardNavItem(
                icon = R.drawable.reflect,
                label = "Reflect",
                selected = currentRoute == "reflect",
                isDarkTheme = isDarkTheme,
                onClick = {
                    if (currentRoute != "reflect") {
                        navController.navigate("reflect") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
            DashboardNavItem(
                icon = R.drawable.ask,
                label = "Ask",
                selected = currentRoute == "ask",
                isDarkTheme = isDarkTheme,
                onClick = {
                    if (currentRoute != "ask") {
                        navController.navigate("ask") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
            DashboardNavItem(
                icon = R.drawable.remember,
                label = "Remember",
                selected = currentRoute == "remember",
                isDarkTheme = isDarkTheme,
                onClick = {
                    if (currentRoute != "remember") {
                        navController.navigate("remember") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun DashboardNavItem(
    icon: Int,
    label: String,
    selected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit = {}
) {
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false, radius = ScreenUtils.responsivePadding() * 1.5f)
            ) { onClick() }
            .padding(ScreenUtils.responsiveSpacing())
    ) {
        // Icon - no background, just the icon itself
        // Special handling for water drop icon to keep its native color
        if (icon == R.drawable.ic_orielle_drop) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = if (selected) WaterBlue else WaterBlue, // Always keep water drop color
                modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
            )
        } else {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = if (selected) WaterBlue else unselectedColor,
                modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
            )
        }

        Spacer(modifier = Modifier.height(ScreenUtils.responsiveTextSpacing()))

        // Text with optional underline for selected state
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) WaterBlue else unselectedColor,
                textAlign = TextAlign.Center
            )

            // Underline for selected state
            if (selected) {
                Spacer(modifier = Modifier.height(ScreenUtils.responsiveTextSpacing() * 0.5f))
                Box(
                    modifier = Modifier
                        .width(ScreenUtils.responsiveSpacing() * 2)
                        .height(ScreenUtils.responsiveTextSpacing() * 0.5f)
                        .background(WaterBlue, RoundedCornerShape(ScreenUtils.responsiveTextSpacing() * 0.25f))
                )
            }
        }
    }
}
