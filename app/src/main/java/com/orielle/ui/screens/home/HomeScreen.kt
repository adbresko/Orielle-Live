package com.orielle.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.orielle.ui.theme.DarkGray
import com.orielle.ui.theme.WaterBlue
import com.orielle.ui.theme.Typography
import com.orielle.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeDashboardScreen(
        userName = uiState.userName,
        journalEntries = uiState.journalEntries,
        navController = navController
    )
}

@Composable
fun HomeDashboardScreen(
    userName: String?,
    journalEntries: List<com.orielle.domain.model.JournalEntry>,
    navController: NavController,
    profileImageUrl: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var showLogoutMenu by remember { mutableStateOf(false) }
    val backgroundColor = DarkGray
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Orielle logo and text
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Orielle Logo",
                        modifier = Modifier.size(28.dp),
                        tint = WaterBlue
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ORIELLE",
                        style = Typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Normal)
                    )
                }
                Spacer(Modifier.weight(1f))
                // Profile icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.navigate("profile_settings") },
                    tint = Color.LightGray
                )
                Spacer(Modifier.width(16.dp))
                // Logout menu (three-dot ellipsis)
                Box {
                    IconButton(onClick = { showLogoutMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color.LightGray
                        )
                    }
                    DropdownMenu(
                        expanded = showLogoutMenu,
                        onDismissRequest = { showLogoutMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Log Out") },
                            onClick = {
                                showLogoutMenu = false
                                // Navigate to sign in and clear back stack
                                navController.navigate("sign_in") {
                                    popUpTo("home_graph") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DashboardNavItem(icon = R.drawable.home, label = "Home", selected = true)
                    DashboardNavItem(icon = R.drawable.reflect, label = "Reflect", selected = false)
                    DashboardNavItem(icon = R.drawable.ask, label = "Ask", selected = false)
                    DashboardNavItem(icon = R.drawable.remember, label = "Remember", selected = false)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))
            if (!expanded) {
                // Collapsed state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.airwave),
                            contentDescription = "Airwave",
                            modifier = Modifier.size(48.dp),
                            tint = WaterBlue
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "How is your inner weather ?",
                            style = Typography.headlineLarge.copy(color = Color.White),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap here to begin your check-in.",
                            style = Typography.bodyLarge.copy(color = Color.LightGray),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Expanded state
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Good morning, ${userName ?: "User"}.",
                    style = Typography.displayLarge.copy(color = Color.White),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 48.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "A THOUGHT FROM YOUR PAST",
                            style = Typography.bodyMedium.copy(color = Color.LightGray),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        val quote = journalEntries.lastOrNull()?.content ?: "No reflections yet."
                        Text(
                            text = quote,
                            style = Typography.bodyLarge.copy(
                                color = Color.White,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardNavItem(icon: Int, label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = if (selected) WaterBlue else Color.LightGray
        )
        Text(
            text = label,
            style = Typography.bodyMedium.copy(color = if (selected) WaterBlue else Color.LightGray)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun HomeDashboardScreenPreview() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()
    HomeDashboardScreen(
        userName = "Mona",
        journalEntries = listOf(
            com.orielle.domain.model.JournalEntry(
                id = "1",
                userId = "user1",
                content = "Felt a real sense of growth today after that challenging conversation.",
                mood = "Reflective"
            )
        ),
        navController = fakeNavController
    )
}