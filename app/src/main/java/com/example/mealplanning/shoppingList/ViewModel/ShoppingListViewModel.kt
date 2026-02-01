package com.example.mealplanning.shoppingList.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.shoppingList.data.ShoppingCart
import com.example.mealplanning.shoppingList.data.ShoppingCartDao
import com.example.mealplanning.weeklyMealPlan.data.IngredientSummary
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


    fun updateShoppingListFromMealPlan(ingredientSummaries: List<IngredientSummary>, weekStartDate: LocalDate) {
        viewModelScope.launch {
            // 1. Map the required totals from the Meal Plan for easy lookup
            val requiredTotals = ingredientSummaries.associate {
                it.IngredientID to it.TotalAmount
            }

            // 2. Get current Cart snapshot for this week from the DB
            val currentCartItems = shoppingCartDao.getCartForWeek(weekStartDate)
                .first()
                .associateBy { it.IngredientID }

            // 3. Reconcile: Update or Insert items based on Meal Plan requirements
            requiredTotals.forEach { (ingredientId, totalRequired) ->
                val existingCartItem = currentCartItems[ingredientId]

                if (existingCartItem != null) {
                    // Update only if the amount changed and it hasn't been pushed to stock yet
                    if (existingCartItem.Amount != totalRequired && !existingCartItem.isUpdatedToStock) {
                        shoppingCartDao.update(existingCartItem.copy(Amount = totalRequired))
                    }
                } else if (totalRequired > 0) {
                    // If it doesn't exist in the cart at all, insert it as a new entry
                    shoppingCartDao.insert(
                        ShoppingCart(
                            IngredientID = ingredientId,
                            Amount = totalRequired,
                            week = weekStartDate,
                            isUpdatedToStock = false
                        )
                    )
                }
            }

            // 4. Cleanup: Remove items in the Cart that are no longer in the Meal Plan for this week
            currentCartItems.forEach { (ingredientId, cartItem) ->
                if (!requiredTotals.containsKey(ingredientId)) {
                    shoppingCartDao.delete(cartItem)
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
