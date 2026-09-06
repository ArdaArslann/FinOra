package com.finora.app.ui.screens.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finora.app.data.network.CategoryDto
import com.finora.app.data.network.CreateCategoryRequest
import com.finora.app.domain.model.Resource
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SpaceDark
import com.finora.app.ui.viewmodels.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categoriesState by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(categoriesState) {
        if (categoriesState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = categoriesState.message ?: "An error occurred"
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Categories", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpaceDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryNeon
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category", tint = Color.Black)
            }
        },
        containerColor = SpaceDark
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (categoriesState is Resource.Loading) {
                CircularProgressIndicator(color = PrimaryNeon, modifier = Modifier.align(Alignment.Center))
            } else if (categoriesState is Resource.Success) {
                val list = categoriesState.data ?: emptyList()
                if (list.isEmpty()) {
                    Text("No categories yet.", color = Color.White.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(list, key = { it.id }) { cat ->
                            var isDeleted by remember { mutableStateOf(false) }
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        isDeleted = true
                                        viewModel.deleteCategory(cat.id)
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
                                                    .background(com.finora.app.ui.theme.ErrorRed, shape = MaterialTheme.shapes.large)
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    CategoryItem(
                                        category = cat,
                                        onClick = { editingCategory = cat }
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (categoriesState is Resource.Error) {
                if (categoriesState.data.isNullOrEmpty()) {
                    Text("No categories yet.", color = Color.White.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, icon, color ->
                viewModel.createCategory(CreateCategoryRequest(name, icon, color))
                showAddDialog = false
            }
        )
    }

    if (editingCategory != null) {
        EditCategoryDialog(
            category = editingCategory!!,
            onDismiss = { editingCategory = null },
            onUpdate = { id, name, icon, color ->
                viewModel.updateCategory(id, CreateCategoryRequest(name, icon, color))
                editingCategory = null
            }
        )
    }
}

@Composable
fun CategoryItem(
    category: CategoryDto,
    onClick: () -> Unit = {}
) {
    val parsedColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { PrimaryNeon }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(parsedColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val iconVector = when (category.icon) {
                    "HOME" -> Icons.Default.Home
                    "CART" -> Icons.Default.ShoppingCart
                    else -> Icons.Default.Star
                }
                Icon(iconVector, contentDescription = null, tint = parsedColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(category.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("STAR") }
    var selectedColor by remember { mutableStateOf("#00FF87") } // PrimaryNeon

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        title = { Text("New Category", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon,
                        focusedLabelColor = PrimaryNeon
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select Color", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors = listOf("#00FF87", "#FF3366", "#00C2FF", "#F59E0B")
                    colors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Icon", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val icons = listOf(
                        "STAR" to Icons.Default.Star,
                        "HOME" to Icons.Default.Home,
                        "CART" to Icons.Default.ShoppingCart
                    )
                    icons.forEach { (iconName, vector) ->
                        val tint = if (selectedIcon == iconName) PrimaryNeon else Color.White.copy(alpha = 0.5f)
                        Icon(
                            imageVector = vector,
                            contentDescription = iconName,
                            tint = tint,
                            modifier = Modifier.clickable { selectedIcon = iconName }.size(32.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onAdd(name, selectedIcon, selectedColor)
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
fun EditCategoryDialog(
    category: CategoryDto,
    onDismiss: () -> Unit,
    onUpdate: (id: String, name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var selectedIcon by remember { mutableStateOf(category.icon) }
    var selectedColor by remember { mutableStateOf(category.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        title = { Text("Edit Category", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon,
                        focusedLabelColor = PrimaryNeon
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select Color", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors = listOf("#00FF87", "#FF3366", "#00C2FF", "#F59E0B")
                    colors.forEach { hex ->
                        val colorVal = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(colorVal, CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Icon", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val icons = listOf(
                        "STAR" to Icons.Default.Star,
                        "HOME" to Icons.Default.Home,
                        "CART" to Icons.Default.ShoppingCart
                    )
                    icons.forEach { (iconName, vector) ->
                        val tint = if (selectedIcon == iconName) PrimaryNeon else Color.White.copy(alpha = 0.5f)
                        Icon(
                            imageVector = vector,
                            contentDescription = iconName,
                            tint = tint,
                            modifier = Modifier.clickable { selectedIcon = iconName }.size(32.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onUpdate(category.id, name, selectedIcon, selectedColor)
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
