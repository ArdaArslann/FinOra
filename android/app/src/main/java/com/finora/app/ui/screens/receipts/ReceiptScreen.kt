package com.finora.app.ui.screens.receipts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finora.app.domain.model.Resource
import com.finora.app.ui.components.AnimatedPrimaryButton
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.*
import com.finora.app.ui.viewmodels.ReceiptViewModel
import com.finora.app.data.network.ReceiptDto
import androidx.activity.result.PickVisualMediaRequest

enum class ReceiptState {
    IDLE, SCANNING, SUCCESS, ERROR
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    val uploadState by viewModel.uploadState.collectAsState()
    var currentState by remember { mutableStateOf(ReceiptState.IDLE) }
    var errorMessage by remember { mutableStateOf("") }
    var extractedData by remember { mutableStateOf<ReceiptDto?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadReceipt(uri)
        } else {
            currentState = ReceiptState.IDLE
        }
    }

    LaunchedEffect(uploadState) {
        when (uploadState) {
            is Resource.Loading -> currentState = ReceiptState.SCANNING
            is Resource.Success -> {
                if (uploadState.data != null) {
                    extractedData = uploadState.data
                    currentState = ReceiptState.SUCCESS
                } else {
                    currentState = ReceiptState.IDLE
                }
            }
            is Resource.Error -> {
                errorMessage = uploadState.message ?: "Upload failed"
                currentState = ReceiptState.ERROR
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "AI Receipt Scanner",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Upload a receipt and let Gemini Vision extract the details automatically.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        AnimatedContent(targetState = currentState, label = "Receipt State") { state ->
            when (state) {
                ReceiptState.IDLE -> UploadPlaceholder(onUploadClick = { 
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                })
                ReceiptState.SCANNING -> ScanningAnimation()
                ReceiptState.SUCCESS -> ExtractedDataCard(
                    data = extractedData,
                    onConfirm = { 
                        // Typically we would show confirmation dialog here, or just reset for now
                        viewModel.resetState() 
                    }
                )
                ReceiptState.ERROR -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = errorMessage, color = Color.Red, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        AnimatedPrimaryButton(text = "Try Again", onClick = { viewModel.resetState() })
                    }
                }
            }
        }
    }
}

@Composable
fun UploadPlaceholder(onUploadClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .clickable { onUploadClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = "Upload",
                tint = PrimaryNeon,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Tap to upload receipt", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ScanningAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .background(PrimaryNeon.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Scanning",
                tint = PrimaryNeon,
                modifier = Modifier
                    .size(64.dp)
                    .rotate(rotation)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Gemini Vision is analyzing...", color = SecondaryNeon, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun ExtractedDataCard(data: ReceiptDto?, onConfirm: () -> Unit) {
    val extraction = data?.extraction
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Extraction Successful", color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Merchant", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            Text(extraction?.merchantName ?: "Unknown", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Total Amount", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            Text("${extraction?.currency ?: "$"}${extraction?.totalAmount ?: "0.00"}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Suggested Category", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            Text(extraction?.suggestedCategory ?: "Uncategorized", color = PrimaryNeon, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AnimatedPrimaryButton(text = "Confirm & Save", onClick = onConfirm)
        }
    }
}
