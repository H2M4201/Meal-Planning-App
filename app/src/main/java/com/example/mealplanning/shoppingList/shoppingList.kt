package com.example.mealplanning.shoppingList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealplanning.shoppingList.data.ShoppingCart
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.shareUI.components.AppTopBar
import com.example.mealplanning.shareUI.components.IngredientDialog
import com.example.mealplanning.shoppingList.ViewModel.ShoppingListViewModel
import com.example.mealplanning.shoppingList.components.ShoppingItemRow
import com.example.mealplanning.stock.ViewModel.StockViewModel
import com.example.mealplanning.stock.data.Stock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onNavigateUp: () -> Unit,
    shoppingListVm: ShoppingListViewModel,
    stockVm: StockViewModel,
    ingredientListVm: IngredientListViewModel
) {
    var currentWeekStart by remember {
        mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)))
    }

    val currentWeekItems by remember(currentWeekStart) {
        shoppingListVm.getCartForWeek(currentWeekStart)
    }.collectAsState(initial = emptyList())
    val masterIngredients by ingredientListVm.masterIngredients.collectAsState()
    var editingItem by remember { mutableStateOf<ShoppingCart?>(null) }

    Scaffold(
        topBar = { AppTopBar(title = "Shopping List", onNavigateUp = onNavigateUp) },
        containerColor = Color(0xFF2C2C2C)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            ShoppingListTopControls(
                weekStartDate = currentWeekStart,
                onLastWeek = { currentWeekStart = currentWeekStart.minusWeeks(1) },
                onNextWeek = { currentWeekStart = currentWeekStart.plusWeeks(1) }
            )

            if (currentWeekItems.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No items for this week.", color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentWeekItems, key = { it.ID }) { item ->
                        val ingredient = masterIngredients.find { it.ID == item.IngredientID }
                        if (ingredient != null) {
                            ShoppingItemRow(
                                ingredient = ingredient,
                                shoppingCart = item,
                                onEdit = { editingItem = item },
                                onDelete = { shoppingListVm.removeShoppingCartItem(item) },
                                isBought = false // Logic for bought items can be added via a DB column later
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    // 1. Identify items that haven't been pushed to stock yet
                    val itemsToUpdate = currentWeekItems.filter { !it.isUpdatedToStock }

                    if (itemsToUpdate.isNotEmpty()) {
                        // 2. MODIFICATION: Call the optimized sync function
                        // This function now handles summing amounts and updating existing rows
                        stockVm.updateStockFromShoppingList(itemsToUpdate)

                        // 3. Mark these items as "Updated to Stock" in the ShoppingCart
                        itemsToUpdate.forEach { item ->
                            shoppingListVm.updateShoppingCartItem(
                                item.copy(isUpdatedToStock = true)
                            )
                        }
                    }
                },
                enabled = currentWeekItems.any { !it.isUpdatedToStock },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                val allSynced = currentWeekItems.isNotEmpty() &&
                        currentWeekItems.all { it.isUpdatedToStock }
                Text(if (allSynced) "All Items in Stock" else "Add Items to Stock")
            }
        }

        editingItem?.let { itemToEdit ->
            val ingredient = masterIngredients.find { it.ID == itemToEdit.IngredientID }
            if (ingredient != null) {
                IngredientDialog(
                    ingredient = ingredient,
                    initialAmount = itemToEdit.Amount.toString(),
                    ingredientListVm = ingredientListVm,
                    isMasterIngredient = false,
                    onDismiss = { editingItem = null },
                    onSave = { _, amount, _ ->
                        shoppingListVm.updateShoppingCartItem(itemToEdit.copy(Amount = amount.toIntOrNull() ?: 0))
                        editingItem = null
                    }
                )
            }
        }
    }
}

// Added the missing TopControls function
@Composable
fun ShoppingListTopControls(
    weekStartDate: LocalDate,
    onLastWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onLastWeek) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Last Week", tint = Color.White)
        }
        Text(
            text = "Week of ${weekStartDate.format(formatter)}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(onClick = onNextWeek) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Week", tint = Color.White)
        }
    }
}
