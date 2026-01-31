package com.example.mealplanning.shoppingList.ViewModel

import androidx.compose.animation.core.copy
import androidx.compose.foundation.gestures.forEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.shoppingList.data.ShoppingCart
import com.example.mealplanning.shoppingList.data.ShoppingCartDao
import com.example.mealplanning.weeklyMealPlan.data.IngredientSummary
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class ShoppingListViewModel(private val shoppingCartDao: ShoppingCartDao) : ViewModel() {

    fun getCartForWeek(week: LocalDate): StateFlow<List<ShoppingCart>> {
        return shoppingCartDao.getCartForWeek(week)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    fun addShoppingCart(ingredientId: Int, amount: Int, week: LocalDate) {
        viewModelScope.launch {
            val newCartItem = ShoppingCart(IngredientID = ingredientId, Amount = amount, week = week)
            shoppingCartDao.insert(newCartItem)
        }
    }

    fun removeShoppingCartItem(item: ShoppingCart) {
        viewModelScope.launch {
            shoppingCartDao.delete(item)
        }
    }

    fun updateShoppingCartItem(item: ShoppingCart) {
        viewModelScope.launch {
            shoppingCartDao.insert(item)
        }
    }

    fun updateLastStockSyncAmount(items: List<ShoppingCart>) {
        viewModelScope.launch {
            // Step 1: Sync the current database state (Amount -> LastUpdatedStock)
            shoppingCartDao.updateLastStockSyncAmountLoop(items)
        }
    }

    fun updateShoppingListFromMealPlan(ingredientSummaries: List<IngredientSummary>, weekStartDate: LocalDate) {
        viewModelScope.launch {
            // 1. The "Source of Truth" is now the Delta provided by the summaries
            val ingredientDeltas = ingredientSummaries.associate {
                it.IngredientID to (it.TotalAmount - it.TotalLastUpdated)
            }

            // 2. Get current Cart snapshot
            val currentCartItems = shoppingCartDao.getCartForWeek(weekStartDate)
                .first()
                .associateBy { it.IngredientID }

            // 3. Apply deltas to Shopping Cart
            ingredientDeltas.forEach { (ingredientId, delta) ->
                val existingCartItem = currentCartItems[ingredientId]

                if (existingCartItem != null) {
                    val newAmount = (existingCartItem.Amount + delta).coerceAtLeast(0)
                    if (newAmount == 0) {
                        shoppingCartDao.delete(existingCartItem)
                    } else {
                        // Explicitly use .update() as requested
                        shoppingCartDao.update(existingCartItem.copy(Amount = newAmount))
                    }
                } else if (delta > 0) {
                    shoppingCartDao.insert(
                        ShoppingCart(
                            IngredientID = ingredientId,
                            Amount = delta,
                            week = weekStartDate
                        )
                    )
                }
            }
        }
    }
}

class ShoppingListViewModelFactory(private val shoppingCartDao: ShoppingCartDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(shoppingCartDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
