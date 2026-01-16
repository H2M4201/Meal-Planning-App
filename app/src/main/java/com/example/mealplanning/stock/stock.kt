package com.example.mealplanning.stock

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.ingredientList.data.IngredientDao
//import com.example.mealplanning.data.FakeIngredientDao
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.shareUI.components.AppTopBar
import com.example.mealplanning.shareUI.components.IngredientDialog
import com.example.mealplanning.shareUI.theme.MealPlanningTheme
import com.example.mealplanning.stock.ViewModel.StockViewModel
import com.example.mealplanning.stock.data.Stock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    onNavigateUp: () -> Unit,
    vm: StockViewModel,
    ingredientListVm: IngredientListViewModel
) {
    val stockItems by vm.stockItems.collectAsState()
    val masterIngredients by ingredientListVm.masterIngredients.collectAsState()

    StockScreenContent(
        onNavigateUp = onNavigateUp,
        stockItems = stockItems,
        masterIngredients = masterIngredients,
        onAddStockItem = { ingredientId, amount ->
            val newStockItem = Stock(IngredientID = ingredientId, Amount = amount)
            vm.addStockItems(listOf(newStockItem))
        },
        onUpdateStockItem = { stockItem -> vm.updateStockItem(stockItem) },
        onRemoveStockItem = { stockItem -> vm.removeStockItem(stockItem) },
        ingredientListVm = ingredientListVm // Still needed for the dialog's search
    )
}

// MODIFICATION 2: The "Dumb" Composable that only knows how to display data
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreenContent(
    onNavigateUp: () -> Unit,
    stockItems: List<Stock>,
    masterIngredients: List<Ingredient>,
    onAddStockItem: (ingredientId: Int, amount: Int) -> Unit,
    onUpdateStockItem: (Stock) -> Unit,
    onRemoveStockItem: (Stock) -> Unit,
    ingredientListVm: IngredientListViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Stock?>(null) }

    Scaffold(
        topBar = { AppTopBar(title = "Stock", onNavigateUp = onNavigateUp) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color.LightGray,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        },
        containerColor = Color(0xFF2C2C2C)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stockItems, key = { it.IngredientID }) { item ->
                val ingredient = masterIngredients.find { it.ID == item.IngredientID }
                if (ingredient != null) {
                    StockItemRow(
                        ingredient = ingredient,
                        stock = item,
                        onEdit = { editingItem = item },
                        onDelete = { onRemoveStockItem(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        IngredientDialog(
            ingredientListVm = ingredientListVm,
            onDismiss = { showAddDialog = false },
            onSave = { name, amount, unit ->
                val ingredient = masterIngredients.find { it.Name == name && it.Unit == unit }
                if (ingredient != null) {
                    onAddStockItem(ingredient.ID, amount.toIntOrNull() ?: 0)
                }
                showAddDialog = false
            }
        )
    }

    editingItem?.let { itemToEdit ->
        val ingredient = masterIngredients.find { it.ID == itemToEdit.IngredientID }
        if (ingredient != null) {
            IngredientDialog(
                ingredient = ingredient,
                initialAmount = itemToEdit.Amount.toString(),
                onDismiss = { editingItem = null },
                onSave = { name, amount, unit ->
                    onUpdateStockItem(itemToEdit.copy(Amount = amount.toIntOrNull() ?: 0))
                    editingItem = null
                },
                ingredientListVm = ingredientListVm
            )
        }
    }
}
@Composable
fun StockItemRow(ingredient: Ingredient, stock: Stock, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            color = Color.LightGray
        ) {
            Text(
                text = ingredient.Name,
                modifier = Modifier.padding(16.dp),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            modifier = Modifier.width(100.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.LightGray
        ) {
            Text(
                text = stock.Amount.toString(),
                modifier = Modifier.padding(16.dp),
                color = Color.Black
            )
        }

        IconButton(
            onClick = onEdit,
            modifier = Modifier.background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = Color.Black)
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.background(Color(0xFFFF5252), shape = RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.Close, contentDescription = "Delete Item", tint = Color.White)
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun StockScreenPreview() {
    MealPlanningTheme {
        // 1. Create fake data for the preview to display
        val fakeMasterIngredients = listOf(
            Ingredient(ID = 1, Name = "Chicken", Unit = "g"),
            Ingredient(ID = 2, Name = "Carrot", Unit = "g")
        )
        val fakeStockItems = listOf(
            Stock(IngredientID = 1, Amount = 500),
            Stock(IngredientID = 2, Amount = 300)
        )

        // 2. Call the dumb component with the fake data. No DAOs needed!
        StockScreenContent(
            onNavigateUp = {},
            stockItems = fakeStockItems,
            masterIngredients = fakeMasterIngredients,
            onAddStockItem = { _, _ -> },
            onUpdateStockItem = {},
            onRemoveStockItem = {},
            // The dialog still needs this, so we must provide a dummy VM.
            // A more advanced solution would be to make the search dialog also "dumb".
            ingredientListVm = IngredientListViewModel(FakeIngredientDaoPreview())
        )
    }
}

// Add a private fake DAO just for this preview file to satisfy the IngredientListViewModel
private class FakeIngredientDaoPreview : IngredientDao {
    override suspend fun insert(ingredient: Ingredient) {}
    override suspend fun delete(ingredient: Ingredient) {}
    override fun getAllIngredients(): kotlinx.coroutines.flow.Flow<List<Ingredient>> = kotlinx.coroutines.flow.flowOf(emptyList())
}

