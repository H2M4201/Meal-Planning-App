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

    val currentWeekItems by shoppingListVm.getCartForWeek(currentWeekStart).collectAsState()
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
                onClick = { onAddItemsToStock(currentWeekItems) },
                enabled = currentWeekItems.isNotEmpty() && !isBought,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Add to Stock")
            }
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

@Composable
fun ShoppingItemRow(ingredient: Ingredient, shoppingCart: ShoppingCart, onEdit: () -> Unit, onDelete: () -> Unit, isBought: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
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
            shape = MaterialTheme.shapes.medium,
            color = Color.LightGray
        ) {
            Text(
                text = shoppingCart.Amount.toString(),
                modifier = Modifier.padding(16.dp),
                color = Color.Black
            )
        }

        IconButton(
            onClick = onEdit,
            enabled = !isBought,
            modifier = Modifier.background(
                if (isBought) Color.Gray else Color.White,
                MaterialTheme.shapes.small
            )
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = Color.Black)
        }
        IconButton(
            onClick = onDelete,
            enabled = !isBought,
            modifier = Modifier.background(
                if (isBought) Color.DarkGray else Color(0xFFFF5252),
                MaterialTheme.shapes.small
            )
        ) {
            Icon(Icons.Default.Close, contentDescription = "Delete Item", tint = Color.White)
        }
    }
}

@Composable
fun ShoppingListScreenPreview() {
    MealPlanningTheme {
        // 1. Create fake data for the preview
        val fakeMasterIngredients = listOf(
            Ingredient(ID = 1, Name = "Milk", Unit = "ml"),
            Ingredient(ID = 2, Name = "Bread", Unit = "loaf")
        )
        val fakeCartItems = listOf(
            ShoppingCart(ID = 101, IngredientID = 1, Amount = 1000, week = LocalDate.now()),
            ShoppingCart(ID = 102, IngredientID = 2, Amount = 1, week = LocalDate.now())
        )

        // 2. Call the dumb component with the fake data
        ShoppingListScreenContent(
            onNavigateUp = {},
            currentWeekStart = LocalDate.now(),
            currentWeekItems = fakeCartItems,
            masterIngredients = fakeMasterIngredients,
            onWeekChange = {},
            onUpdateItem = {},
            onRemoveItem = {},
            onAddItemsToStock = {},
            ingredientListVm = IngredientListViewModel(FakeIngredientDaoPreview())
        )
    }
}

// Add a private fake DAO just for this preview file
private class FakeIngredientDaoPreview : IngredientDao {
    override suspend fun insert(ingredient: Ingredient) {}
    override suspend fun delete(ingredient: Ingredient) {}
    override fun getAllIngredients(): kotlinx.coroutines.flow.Flow<List<Ingredient>> = kotlinx.coroutines.flow.flowOf(emptyList())
}