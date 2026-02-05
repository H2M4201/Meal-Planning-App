package com.example.mealplanning.recipe.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.recipe.data.Recipe
import com.example.mealplanning.recipe.data.RecipeDao
import com.example.mealplanning.recipe.data.RecipeDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(private val recipeDao: RecipeDao) : ViewModel() {

    val allRecipes: StateFlow<List<Recipe>> = recipeDao.getAllRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    suspend fun getRecipeDetails(recipeId: Int): List<RecipeDetail> {
        return recipeDao.getRecipeDetails(recipeId).first()
    }

    fun saveRecipe(recipe: Recipe, details: List<RecipeDetail>) {
        viewModelScope.launch {
            val recipeId = recipeDao.insertRecipe(recipe)
            recipeDao.deleteDetailsForRecipe(recipeId.toInt())
            details.forEach { 
                recipeDao.insertRecipeDetail(it.copy(RecipeID = recipeId.toInt()))
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeDao.markAsInactive(recipe.ID)
        }
    }
}

class RecipeViewModelFactory(private val recipeDao: RecipeDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(recipeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
