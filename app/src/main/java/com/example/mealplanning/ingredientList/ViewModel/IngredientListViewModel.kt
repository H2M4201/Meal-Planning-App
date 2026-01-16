package com.example.mealplanning.ingredientList.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.ingredientList.data.IngredientDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientListViewModel(private val ingredientDao: IngredientDao) : ViewModel() {

    val masterIngredients: StateFlow<List<Ingredient>> = ingredientDao.getAllIngredients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addIngredient(name: String, unit: String) {
        viewModelScope.launch {
            ingredientDao.insert(Ingredient(Name = name, Unit = unit))
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientDao.delete(ingredient)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientDao.insert(ingredient)
        }
    }
}

class IngredientListViewModelFactory(private val ingredientDao: IngredientDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngredientListViewModel(ingredientDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
