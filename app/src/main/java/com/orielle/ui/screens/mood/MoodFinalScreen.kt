package com.orielle.ui.screens.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orielle.R
import com.orielle.ui.components.OrielleScreenHeader

@Composable
fun MoodFinalScreen(
    onDone: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F3)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        OrielleScreenHeader(
            text = "Saved for today"
        )
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(220.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "The best way to predict the future is to create it.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF222222),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Abraham Lincoln",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    painter = painterResource(id = R.drawable.orielle_drop),
                    contentDescription = "Orielle Drop",
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF8EC6C6)
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8EC6C6)),
            modifier = Modifier
                .width(180.dp)
                .height(44.dp)
        ) {
            Text("Done", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Share your insight  ➔",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF8EC6C6),
            modifier = Modifier
                .clickable { onShare() }
                .padding(8.dp)
        )
    }
}