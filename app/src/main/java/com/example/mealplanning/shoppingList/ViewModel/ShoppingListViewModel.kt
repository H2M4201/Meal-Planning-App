package com.example.mealplanning.shoppingList.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.shoppingList.data.ShoppingCart
import com.example.mealplanning.shoppingList.data.ShoppingCartDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
