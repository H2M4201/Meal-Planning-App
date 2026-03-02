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
    val availableStock by vm.availableStockItems.collectAsState()
    val masterIngredients by ingredientListVm.masterIngredients.collectAsState()

    StockScreenContent(
        onNavigateUp = onNavigateUp,
        stockItems = availableStock,
        masterIngredients = masterIngredients,
        onUpdateStockItem = { stockItem -> vm.updateStockItem(stockItem) },
        onRemoveStockItem = { stockItem -> vm.removeStockItem(stockItem) },
        onClearStock = { vm.clearStock() },
        ingredientListVm = ingredientListVm // Still needed for the dialog's search
    )

    // Add to StockScreenContent's TopControls or Column
}

// MODIFICATION 2: The "Dumb" Composable that only knows how to display data
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreenContent(
    onNavigateUp: () -> Unit,
    stockItems: List<Stock>,
    masterIngredients: List<Ingredient>,
    onUpdateStockItem: (Stock) -> Unit,
    onRemoveStockItem: (Stock) -> Unit,
    onClearStock: () -> Unit,
    ingredientListVm: IngredientListViewModel
) {
    var editingItem by remember { mutableStateOf<Stock?>(null) }

    Scaffold(
        topBar = { AppTopBar(title = "Stock", onNavigateUp = onNavigateUp) },
        containerColor = Color(0xFF2C2C2C)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
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
                            text = "Amount",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.5f),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.width(96.dp))
                    }
                }

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

            // MODIFICATION: Logic for the Clear Stock Button
            Button(
                onClick = onClearStock, // Now correctly linked
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Clear All Stock", fontWeight = FontWeight.Bold)
            }
        }
    }

    editingItem?.let { itemToEdit ->
        val ingredient = masterIngredients.find { it.ID == itemToEdit.IngredientID }
        if (ingredient != null) {
            IngredientDialog(
                ingredient = ingredient,
                initialAmount = itemToEdit.Amount.toString(),
                ingredientListVm = ingredientListVm,
                isFromCookDialog = false,
                isMasterIngredient = false,
                onDismiss = { editingItem = null },
                onSave = { _, amount, _ ->
                    onUpdateStockItem(itemToEdit.copy(Amount = amount.toIntOrNull() ?: 0))
                    editingItem = null
                }
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
            modifier = Modifier.weight(1.5f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0703C).copy(alpha = 0.9f)
        ) {
            Text(
                text = ingredient.Name,
                modifier = Modifier.padding(16.dp),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            modifier = Modifier.weight(0.8f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0703C).copy(alpha = 0.9f)
        ) {
            Text(
                text = "${stock.Amount} ${ingredient.Unit}",
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
