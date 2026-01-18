package com.example.mealplanning.shoppingList.ViewModel

import androidx.compose.animation.core.copy
import androidx.compose.foundation.gestures.forEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.shoppingList.data.ShoppingCart
import com.example.mealplanning.shoppingList.data.ShoppingCartDao
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

    fun addItemsToCart(items: List<ShoppingCart>) {
        viewModelScope.launch {
            items.forEach { item ->
                shoppingCartDao.insert(item)
            }
        }
    }

    fun updateShoppingListFromMealPlan(mealPlanDetails: List<MealPlanDetail>, weekStartDate: LocalDate) {
        viewModelScope.launch {
            // 1. Get the total required amount of each ingredient from the Meal Plan for this week
            val mealPlanTotals = mealPlanDetails.groupBy { it.IngredientID }
                .mapValues { (_, details) -> details.sumOf { it.Amount } }

            // 2. Get the current Shopping Cart items for this week from the DB
            // We convert it to a map for easy lookup: IngredientID -> ShoppingCart object
            val currentCartItems = shoppingCartDao.getCartForWeek(weekStartDate)
                .first()
                .associateBy { it.IngredientID }

            // 3. Reconcile Meal Plan Totals with Shopping Cart

            // Handle items that ARE in the Meal Plan
            mealPlanTotals.forEach { (ingredientId, totalRequired) ->
                val existingCartItem = currentCartItems[ingredientId]

                if (totalRequired > 0) {
                    if (existingCartItem != null) {
                        // Scenario: Mismatch in total amount -> Update
                        if (existingCartItem.Amount != totalRequired) {
                            shoppingCartDao.insert(existingCartItem.copy(Amount = totalRequired))
                        }
                    } else {
                        // Scenario: Meal plan has it, but Shopping Cart doesn't -> Add
                        shoppingCartDao.insert(
                            ShoppingCart(
                                IngredientID = ingredientId,
                                Amount = totalRequired,
                                week = weekStartDate
                            )
                        )
                    }
                } else if (totalRequired == 0 && existingCartItem != null) {
                    // Scenario: Meal plan total is 0, but item exists in cart -> Delete
                    shoppingCartDao.delete(existingCartItem)
                }
            }

            // 4. Handle items that are in the Shopping Cart but NO LONGER in the Meal Plan at all
            currentCartItems.forEach { (ingredientId, cartItem) ->
                if (!mealPlanTotals.containsKey(ingredientId)) {
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
