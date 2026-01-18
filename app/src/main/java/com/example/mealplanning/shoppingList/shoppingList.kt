package com.example.mealplanning.shoppingList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.shoppingList.data.ShoppingCart
import com.example.mealplanning.ingredientList.data.IngredientDao
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.shareUI.components.AppTopBar
import com.example.mealplanning.shareUI.components.IngredientDialog
import com.example.mealplanning.shareUI.theme.MealPlanningTheme
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

    ShoppingListScreenContent(
        onNavigateUp = onNavigateUp,
        currentWeekStart = currentWeekStart,
        currentWeekItems = currentWeekItems,
        masterIngredients = masterIngredients,
        onWeekChange = { newDate -> currentWeekStart = newDate },
        onUpdateItem = { item -> shoppingListVm.updateShoppingCartItem(item) },
        onRemoveItem = { item -> shoppingListVm.removeShoppingCartItem(item) },
        onAddItemsToStock = { cartItems ->
            // Convert List<ShoppingCart> to List<Stock>
            val stockItemsToAdd = cartItems.map { cartItem ->
                Stock(IngredientID = cartItem.IngredientID, Amount = cartItem.Amount)
            }
            stockVm.addStockItems(stockItemsToAdd)
        },        ingredientListVm = ingredientListVm
    )
}

// MODIFICATION 2: The "Dumb" Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreenContent(
    onNavigateUp: () -> Unit,
    currentWeekStart: LocalDate,
    currentWeekItems: List<ShoppingCart>,
    masterIngredients: List<Ingredient>,
    onWeekChange: (LocalDate) -> Unit,
    onUpdateItem: (ShoppingCart) -> Unit,
    onRemoveItem: (ShoppingCart) -> Unit,
    onAddItemsToStock: (List<ShoppingCart>) -> Unit,
    ingredientListVm: IngredientListViewModel
) {
    var editingItem by remember { mutableStateOf<ShoppingCart?>(null) }
    // Logic for 'isBought' can be managed here or passed down if needed
    val isBought = false

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
                onLastWeek = { onWeekChange(currentWeekStart.minusWeeks(1)) },
                onNextWeek = { onWeekChange(currentWeekStart.plusWeeks(1)) }
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
                                onDelete = { onRemoveItem(item) },
                                isBought = isBought
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    // 1. Filter only items NOT already in stock to avoid double-adding
                    val itemsToAdd = currentWeekItems.filter { !it.isUpdateStock }
                    if (itemsToAdd.isNotEmpty()) {
                        onAddItemsToStock(itemsToAdd)

                        // 2. Mark these specific items as pushed to stock in DB
                        itemsToAdd.forEach { item ->
                            onUpdateItem(item.copy(isUpdateStock = true))
                        }
                    }
                },
                enabled = currentWeekItems.isNotEmpty() && !isBought,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(if (currentWeekItems.all { it.isUpdateStock }) "Items Added to Stock" else "Add to Stock")            }
        }
    }

    editingItem?.let { itemToEdit ->
        val ingredient = masterIngredients.find { it.ID == itemToEdit.IngredientID }
        if (ingredient != null) {
            IngredientDialog(
                ingredient = ingredient,
                initialAmount = itemToEdit.Amount.toString(),
                onDismiss = { editingItem = null },
                onSave = { _, amount, _ ->
                    onUpdateItem(itemToEdit.copy(Amount = amount.toIntOrNull() ?: 0))
                    editingItem = null
                },
                ingredientListVm = ingredientListVm
            )
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