package com.example.mealplanning.ingredientList

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.ingredientList.data.IngredientDao
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.ingredientList.components.MasterIngredientDialog
import com.example.mealplanning.shareUI.components.AppTopBar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListScreen(
    onNavigateUp: () -> Unit,
    vm: IngredientListViewModel
) {
    val masterIngredients by vm.masterIngredients.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }


    Scaffold(
        topBar = { AppTopBar(title = "Ingredient List", onNavigateUp = onNavigateUp) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color.LightGray,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, "Add Ingredient")
            }
        },
        containerColor = Color(0xFF2C2C2C)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- ADDED COLUMN TITLES ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp), // Adjust padding to match layout
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ingredient Name",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.5f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "Unit",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.5f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    // Spacer to account for the Edit/Delete button width in rows below
                    Spacer(modifier = Modifier.width(96.dp))
                }
            }

            items(masterIngredients, key = { it.ID }) { item ->
                MasterIngredientRow(
                    item = item,
                    onEdit = { editingIngredient = item },
                    onDelete = { vm.deleteIngredient(item) }
                )
            }
        }
    }

    if (showAddDialog) {
        MasterIngredientDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, unit ->
                vm.addIngredient(name, unit)
                showAddDialog = false
            }
        )
    }

    editingIngredient?.let { ingredientToEdit ->
        MasterIngredientDialog(
            ingredient = ingredientToEdit,
            onDismiss = { editingIngredient = null },
            onSave = { name, unit ->
                vm.updateIngredient(ingredientToEdit.copy(Name = name, Unit = unit))
                editingIngredient = null
            }
        )
    }
}

@Composable
fun MasterIngredientRow(item: Ingredient, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1.5f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF0703C).copy(alpha = 0.9f)
        ) {
            Text(
                text = item.Name,
                modifier = Modifier.padding(12.dp),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            modifier = Modifier.weight(0.5f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF0703C).copy(alpha = 0.9f)
        ) {
            Text(
                text = item.Unit,
                modifier = Modifier.padding(12.dp),
                color = Color.Black
            )
        }
        IconButton(
            onClick = onEdit,
            modifier = Modifier.background(Color.White, shape = RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Default.Edit, "Edit", tint = Color.Black)
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.background(Color(0xFFFF5252), shape = RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Default.Close, "Delete", tint = Color.White)
        }
    }
}

// Private fake DAO for previewing the screen
private class FakeIngredientDao : IngredientDao {
    override suspend fun insert(ingredient: Ingredient) {}
    override suspend fun delete(ingredient: Ingredient) {}
    override fun getAllIngredients(): Flow<List<Ingredient>> = flowOf(
        listOf(
            Ingredient(1, "Chicken", "kg"),
            Ingredient(2, "Carrot", "g")
        )
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun IngredientListScreenPreview() {
    MaterialTheme {
        IngredientListScreen(
            onNavigateUp = {},
            vm = IngredientListViewModel(FakeIngredientDao())
        )
    }
}
