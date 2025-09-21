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
import com.orielle.ui.util.ScreenUtils
import androidx.compose.ui.unit.sp
import com.orielle.R
import com.orielle.ui.components.OrielleScreenHeader
import androidx.compose.foundation.Image

@Composable
fun MoodFinalScreen(
    onDone: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 5))
        OrielleScreenHeader(
            text = "Saved for today"
        )
        Card(
            shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .padding(horizontal = ScreenUtils.responsivePadding() * 1.5f)
                .fillMaxWidth()
                .height(ScreenUtils.responsivePadding() * 13.75f),
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
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = ScreenUtils.responsivePadding())
                )
                Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))
                Text(
                    text = "Abraham Lincoln",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))
                Image(
                    painter = painterResource(id = R.drawable.ic_orielle_drop),
                    contentDescription = "Orielle Drop",
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(32.dp))
                    // No colorFilter - using native colors for consistency
                )
            }
        }
        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 3))
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { onShare() }
                .padding(8.dp)
        )
    }
}