package com.finora.app.ui.screens.budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finora.app.data.network.BudgetDto
import com.finora.app.data.network.CreateBudgetRequest
import com.finora.app.domain.model.Resource
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.ErrorRed
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SpaceDark
import com.finora.app.ui.theme.SuccessGreen
import com.finora.app.ui.viewmodels.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val budgetState by viewModel.budgets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchBudgets()
    }

    LaunchedEffect(budgetState) {
        if (budgetState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = budgetState.message ?: "An error occurred"
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryNeon
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget", tint = Color.Black)
            }
        },
        containerColor = SpaceDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Budgets",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (budgetState is Resource.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryNeon)
                }
            } else if (budgetState is Resource.Success) {
                val budgets = budgetState.data ?: emptyList()
                if (budgets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No budgets found.", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(budgets, key = { it.id }) { budget ->
                            var isDeleted by remember { mutableStateOf(false) }
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        isDeleted = true
                                        viewModel.deleteBudget(budget.id)
                                        true
                                    } else false
                                }
                            )

                            AnimatedVisibility(
                                visible = !isDeleted,
                                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut()
                            ) {
                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(ErrorRed, shape = MaterialTheme.shapes.large)
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    BudgetItem(
                                        budget = budget,
                                        onClick = { editingBudget = budget }
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (budgetState is Resource.Error) {
                if (budgetState.data.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No budgets found.", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { amount, period, categoryId ->
                viewModel.createBudget(CreateBudgetRequest(amount, period, categoryId))
                showAddDialog = false
            }
        )
    }

    if (editingBudget != null) {
        EditBudgetDialog(
            budget = editingBudget!!,
            onDismiss = { editingBudget = null },
            onUpdate = { id, amount, period, categoryId ->
                viewModel.updateBudget(
                    id, 
                    com.finora.app.data.network.UpdateBudgetRequest(
                        amount = amount, 
                        period = period, 
                        startDate = editingBudget!!.startDate, 
                        endDate = editingBudget!!.endDate, 
                        categoryId = categoryId
                    )
                )
                editingBudget = null
            }
        )
    }
}

@Composable
fun BudgetItem(
    budget: BudgetDto,
    onClick: () -> Unit = {}
) {
    // We mock the spent amount for UI demonstration until we join it with transactions properly
    val spent = budget.amount * 0.75 
    val percentage = (spent / budget.amount).toFloat()
    
    val progressColor = when {
        percentage > 0.9f -> ErrorRed
        percentage > 0.7f -> Color(0xFFF59E0B) // Amber
        else -> SuccessGreen
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(budget.period, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("₺${String.format("%.2f", spent)} / ₺${String.format("%.2f", budget.amount)}", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(percentage * 100).toInt()}% used", color = progressColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onAdd: (amount: Double, period: String, categoryId: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("MONTHLY") }
    var categoryId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        title = { Text("New Budget", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon,
                        focusedLabelColor = PrimaryNeon
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = categoryId,
                    onValueChange = { categoryId = it },
                    label = { Text("Category") }, // Normally a dropdown, simplify for now
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon,
                        focusedLabelColor = PrimaryNeon
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amountStr.toDoubleOrNull()
                if (amt != null && categoryId.isNotBlank()) {
                    onAdd(amt, selectedPeriod, categoryId)
                }
            }) {
                Text("Add", color = PrimaryNeon)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun EditBudgetDialog(
    budget: BudgetDto,
    onDismiss: () -> Unit,
    onUpdate: (id: String, amount: Double, period: String, categoryId: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(budget.amount.toString()) }
    var selectedPeriod by remember { mutableStateOf(budget.period) }
    var categoryId by remember { mutableStateOf(budget.categoryId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        title = { Text("Edit Budget", color = Color.White) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { selectedPeriod = "WEEKLY" }) {
                        Text("Week", color = if (selectedPeriod == "WEEKLY") PrimaryNeon else Color.White.copy(alpha = 0.5f))
                    }
                    TextButton(onClick = { selectedPeriod = "MONTHLY" }) {
                        Text("Month", color = if (selectedPeriod == "MONTHLY") PrimaryNeon else Color.White.copy(alpha = 0.5f))
                    }
                    TextButton(onClick = { selectedPeriod = "YEARLY" }) {
                        Text("Year", color = if (selectedPeriod == "YEARLY") PrimaryNeon else Color.White.copy(alpha = 0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon,
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amountStr.toDoubleOrNull()
                if (amt != null && categoryId.isNotBlank()) {
                    onUpdate(budget.id, amt, selectedPeriod, categoryId)
                }
            }) {
                Text("Save", color = PrimaryNeon)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}
