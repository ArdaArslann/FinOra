package com.finora.app.ui.screens.transactions

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
import com.finora.app.data.network.CreateTransactionRequest
import com.finora.app.data.network.TransactionDto
import com.finora.app.domain.model.Resource
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.ErrorRed
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SpaceDark
import com.finora.app.ui.theme.SuccessGreen
import com.finora.app.ui.viewmodels.TransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    categoryViewModel: com.finora.app.ui.viewmodels.CategoryViewModel = hiltViewModel()
) {
    val transactionState by viewModel.transactions.collectAsState()
    val categoryState by categoryViewModel.categories.collectAsState()
    val categories = categoryState.data ?: emptyList()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchTransactions()
    }

    LaunchedEffect(transactionState) {
        if (transactionState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = transactionState.message ?: "An error occurred"
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
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = Color.Black)
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
                text = "Transactions",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (transactionState is Resource.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryNeon)
                }
            } else if (transactionState is Resource.Success) {
                val transactions = transactionState.data ?: emptyList()
                
                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions found.", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(transactions, key = { it.id }) { transaction ->
                            var isDeleted by remember { mutableStateOf(false) }
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        isDeleted = true
                                        viewModel.deleteTransaction(transaction.id)
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
                                    TransactionItem(
                                        transaction = transaction,
                                        onClick = { editingTransaction = transaction }
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (transactionState is Resource.Error) {
                // Snackbar handles the error, show empty state if no data
                if (transactionState.data.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions found.", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onAdd = { amount, type, desc, categoryId ->
                viewModel.createTransaction(CreateTransactionRequest(amount, type, desc, LocalDate.now().toString(), categoryId))
                showAddDialog = false
            }
        )
    }

    if (editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!,
            categories = categories,
            onDismiss = { editingTransaction = null },
            onUpdate = { id, amount, type, desc, categoryId ->
                viewModel.updateTransaction(
                    id, 
                    CreateTransactionRequest(amount, type, desc, editingTransaction!!.transactionDate, categoryId)
                )
                editingTransaction = null
            }
        )
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionDto,
    onClick: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = transaction.description,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            val amountColor = if (transaction.type == "INCOME") SuccessGreen else ErrorRed
            val prefix = if (transaction.type == "INCOME") "+" else "-"
            
            Text(
                text = "$prefix₺${String.format("%.2f", transaction.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    categories: List<com.finora.app.data.network.CategoryDto>,
    onDismiss: () -> Unit,
    onAdd: (amount: Double, type: String, description: String, categoryId: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("EXPENSE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        title = { Text("New Transaction", color = Color.White) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { type = "INCOME" }) {
                        Text("Income", color = if (type == "INCOME") SuccessGreen else Color.White.copy(alpha = 0.5f))
                    }
                    TextButton(onClick = { type = "EXPENSE" }) {
                        Text("Expense", color = if (type == "EXPENSE") ErrorRed else Color.White.copy(alpha = 0.5f))
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
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
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
                if (amt != null && description.isNotBlank() && selectedCategory != null) {
                    onAdd(amt, type, description, selectedCategory!!.id)
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
fun EditTransactionDialog(
    transaction: TransactionDto,
    categories: List<com.finora.app.data.network.CategoryDto>,
    onDismiss: () -> Unit,
    onUpdate: (id: String, amount: Double, type: String, description: String, categoryId: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(transaction.amount.toString()) }
    var description by remember { mutableStateOf(transaction.description) }
    var selectedCategory by remember { mutableStateOf(categories.find { it.id == transaction.categoryId }) }
    var expanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(transaction.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        title = { Text("Edit Transaction", color = Color.White) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { type = "INCOME" }) {
                        Text("Income", color = if (type == "INCOME") SuccessGreen else Color.White.copy(alpha = 0.5f))
                    }
                    TextButton(onClick = { type = "EXPENSE" }) {
                        Text("Expense", color = if (type == "EXPENSE") ErrorRed else Color.White.copy(alpha = 0.5f))
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
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
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
                if (amt != null && description.isNotBlank() && selectedCategory != null) {
                    onUpdate(transaction.id, amt, type, description, selectedCategory!!.id)
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
