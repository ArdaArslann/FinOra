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
import com.finora.app.ui.viewmodels.CategoryViewModel
import com.finora.app.data.network.CategoryDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val budgetState by viewModel.budgets.collectAsState()
    val categoryState by categoryViewModel.categories.collectAsState()
    val categories = categoryState.data ?: emptyList()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchBudgets()
        categoryViewModel.fetchCategories()
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

            val isLoading = budgetState is Resource.Loading
            val budgets = budgetState.data ?: emptyList()

            if (isLoading && budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryNeon)
                }
            } else if (budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No budgets found.", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryNeon,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onAdd = { amount, period, categoryId ->
                val today = java.time.LocalDate.now()
                val (start, end) = when(period) {
                    "WEEKLY" -> {
                        val monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                        monday.toString() to monday.plusDays(6).toString()
                    }
                    "MONTHLY" -> {
                        val firstDay = today.withDayOfMonth(1)
                        firstDay.toString() to today.withDayOfMonth(today.lengthOfMonth()).toString()
                    }
                    "YEARLY" -> {
                        val firstDay = today.withDayOfYear(1)
                        firstDay.toString() to today.withDayOfYear(today.lengthOfYear()).toString()
                    }
                    else -> today.toString() to today.plusMonths(1).toString()
                }
                viewModel.createBudget(CreateBudgetRequest(amount, period, start, end, categoryId))
                showAddDialog = false
            }
        )
    }

    if (editingBudget != null) {
        EditBudgetDialog(
            budget = editingBudget!!,
            categories = categories,
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
    // Use the actual spent amount from backend, fallback to 0.0
    val spent = budget.spent ?: 0.0
    val percentage = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f
    
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    categories: List<CategoryDto>,
    onDismiss: () -> Unit,
    onAdd: (amount: Double, period: String, categoryId: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("MONTHLY") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        title = { Text("New Budget", color = Color.White) },
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
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier.menuAnchor(),
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amountStr.toDoubleOrNull()
                if (amt != null && selectedCategory != null) {
                    onAdd(amt, selectedPeriod, selectedCategory!!.id)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetDialog(
    budget: BudgetDto,
    categories: List<CategoryDto>,
    onDismiss: () -> Unit,
    onUpdate: (id: String, amount: Double, period: String, categoryId: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(budget.amount.toString()) }
    var selectedPeriod by remember { mutableStateOf(budget.period) }
    var selectedCategory by remember { mutableStateOf(categories.find { it.id == budget.categoryId }) }
    var expanded by remember { mutableStateOf(false) }

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
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier.menuAnchor(),
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amountStr.toDoubleOrNull()
                if (amt != null && selectedCategory != null) {
                    onUpdate(budget.id, amt, selectedPeriod, selectedCategory!!.id)
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
