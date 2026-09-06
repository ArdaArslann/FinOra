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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.finora.app.data.network.CategoryDto
import com.finora.app.data.network.ConfirmReceiptRequest
import androidx.activity.result.PickVisualMediaRequest

enum class ReceiptState {
    IDLE, SCANNING, SUCCESS, ERROR
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel = hiltViewModel(),
    categoryViewModel: com.finora.app.ui.viewmodels.CategoryViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit = {}
) {
    val uploadState by viewModel.uploadState.collectAsState()
    val categoryState by categoryViewModel.categories.collectAsState()
    val categories = categoryState.data ?: emptyList()
    val confirmState by viewModel.confirmState.collectAsState()
    var currentState by remember { mutableStateOf(ReceiptState.IDLE) }
    var errorMessage by remember { mutableStateOf("") }
    var extractedData by remember { mutableStateOf<ReceiptDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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
                snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    LaunchedEffect(confirmState) {
        if (confirmState is Resource.Success && currentState == ReceiptState.SUCCESS) {
            viewModel.resetState()
            onNavigateHome()
        } else if (confirmState is Resource.Error) {
            snackbarHostState.showSnackbar(confirmState.message ?: "Confirmation failed")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SpaceDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    photoPickerLauncher.launch("image/*") 
                })
                ReceiptState.SCANNING -> ScanningAnimation()
                ReceiptState.SUCCESS -> ExtractedDataCard(
                    data = extractedData,
                    categories = categories,
                    onConfirm = { request -> 
                        if (extractedData != null) {
                            viewModel.confirmReceipt(extractedData!!.id, request) 
                        }
                    }
                )
                ReceiptState.ERROR -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedPrimaryButton(text = "Try Again", onClick = { viewModel.resetState() })
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractedDataCard(
    data: ReceiptDto?, 
    categories: List<CategoryDto>,
    onConfirm: (ConfirmReceiptRequest) -> Unit
) {
    val extraction = data?.extraction
    var amountStr by remember { mutableStateOf(extraction?.totalAmount?.toString() ?: "") }
    var description by remember { mutableStateOf(extraction?.merchantName ?: "") }
    var transactionDate by remember { mutableStateOf(extraction?.transactionDate ?: java.time.LocalDate.now().toString()) }
    var selectedCategory by remember { mutableStateOf<CategoryDto?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            val suggested = categories.find { it.name.equals(extraction?.suggestedCategory, ignoreCase = true) }
            selectedCategory = suggested ?: categories.firstOrNull()
        }
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Extraction Successful", color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Merchant / Description") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PrimaryNeon,
                    focusedLabelColor = PrimaryNeon
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Total Amount") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PrimaryNeon,
                    focusedLabelColor = PrimaryNeon
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = transactionDate,
                onValueChange = { transactionDate = it },
                label = { Text("Transaction Date (YYYY-MM-DD)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PrimaryNeon,
                    focusedLabelColor = PrimaryNeon
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Select Category",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon,
                        focusedLabelColor = PrimaryNeon
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AnimatedPrimaryButton(text = "Confirm & Save", onClick = {
                val amt = amountStr.toDoubleOrNull()
                if (amt != null && description.isNotBlank() && selectedCategory != null) {
                    onConfirm(ConfirmReceiptRequest(amt, description, transactionDate, selectedCategory!!.id))
                }
            })
        }
    }
}
