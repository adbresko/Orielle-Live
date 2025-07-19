package com.orielle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.orielle.R

@Composable
fun AccountRequiredModal(
    onSignUp: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon (use a user/account icon or your brand logo)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF8EC6C6), shape = RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Replace with your own icon if desired
                    androidx.compose.material3.Icon(
                        painter = painterResource(id = R.drawable.ic_orielle_drop),
                        contentDescription = "Account Required",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Account Required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF222222)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign up for a free account to unlock this feature and save your progress.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSignUp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8EC6C6))
                ) {
                    Text("Sign Up", fontSize = 18.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Text("Maybe Later", fontSize = 18.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}