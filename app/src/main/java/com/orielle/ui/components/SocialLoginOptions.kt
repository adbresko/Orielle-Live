package com.orielle.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.orielle.R

/**
 * A reusable composable that displays the "or continue with" text
 * and the social login provider buttons (Google, Apple).
 */
@Composable
fun SocialLoginOptions(
    onGoogleSignInClick: () -> Unit,
    onAppleSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(
                text = "or continue with",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            OrielleOutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier.size(56.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Continue with Google",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
            OrielleOutlinedButton(
                onClick = onAppleSignInClick,
                modifier = Modifier.size(56.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_apple),
                    contentDescription = "Continue with Apple",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}
