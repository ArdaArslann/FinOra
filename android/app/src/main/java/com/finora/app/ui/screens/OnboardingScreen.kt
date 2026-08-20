package com.finora.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.app.ui.components.AnimatedPrimaryButton
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SpaceDark
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onGetStartedClick: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fade"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "slide"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark),
        contentAlignment = Alignment.Center
    ) {
        // Decorative blurred circles would go here (requires RenderEffect for blur)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .alpha(alpha)
                .offset(y = offsetY),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Master your finances with AI",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 48.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "FinOra automatically categorizes your expenses, tracks your budgets, and gives you actionable insights.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ready to start?",
                        color = PrimaryNeon,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedPrimaryButton(
                        text = "Get Started",
                        onClick = onGetStartedClick
                    )
                }
            }
        }
    }
}
