package com.example.mealplanning.recipe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeDetail(recipeDetail: RecipeDetail)

    @Query("SELECT * FROM Recipe")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM RecipeDetail WHERE RecipeID = :recipeId")
    fun getRecipeDetails(recipeId: Int): Flow<List<RecipeDetail>>
}
