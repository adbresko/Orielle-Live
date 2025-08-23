package com.orielle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun BottomNavigation(
    navController: NavController,
    currentRoute: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // Light gray background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardNavItem(
                icon = R.drawable.ic_orielle_drop,
                label = "Home",
                selected = currentRoute == "home_graph",
                onClick = { navController.navigate("home_graph") }
            )
            DashboardNavItem(
                icon = R.drawable.reflect,
                label = "Reflect",
                selected = currentRoute == "reflect",
                onClick = { navController.navigate("reflect") }
            )
            DashboardNavItem(
                icon = R.drawable.ask,
                label = "Ask",
                selected = currentRoute == "ask",
                onClick = { navController.navigate("ask") }
            )
            DashboardNavItem(
                icon = R.drawable.remember,
                label = "Remember",
                selected = currentRoute == "remember",
                onClick = { navController.navigate("remember") }
            )
        }
    }
}

@Composable
private fun DashboardNavItem(
    icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        // Icon - no background, just the icon itself
        // Special handling for water drop icon to keep its native color
        if (icon == R.drawable.ic_orielle_drop) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = if (selected) WaterBlue else WaterBlue, // Always keep water drop color
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = if (selected) WaterBlue else Charcoal,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Text with optional underline for selected state
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) WaterBlue else Charcoal,
                textAlign = TextAlign.Center
            )

            // Underline for selected state
            if (selected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(WaterBlue, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}
