package com.example.mealplanning.ingredientList.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.ingredientList.data.IngredientDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientListViewModel(private val ingredientDao: IngredientDao) : ViewModel() {

    val masterIngredients: StateFlow<List<Ingredient>> = ingredientDao.getAllIngredients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    suspend fun addIngredient(name: String, amount: String, unit: String) {
        val ingredient = Ingredient(Name = name, Unit = unit, isActive = 1)
        ingredientDao.insert(ingredient)
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientDao.markAsInactive(ingredient.ID)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientDao.update(ingredient)
        }
    }

    suspend fun getIngredientByName(name: String): Ingredient? {
        return ingredientDao.getIngredientByName(name)
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
