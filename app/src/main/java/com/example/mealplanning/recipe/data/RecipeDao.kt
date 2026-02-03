package com.example.mealplanning.recipe.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeDetail(recipeDetail: RecipeDetail)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("UPDATE Recipe SET isActive = 0 WHERE ID = :recipeId")
    suspend fun markAsInactive(recipeId: Int)

//    @Transaction
//    @Query("SELECT * FROM Recipe WHERE isActive = 1 ORDER BY Name ASC")
//    fun getAllRecipesWithDetails(): Flow<Map<Recipe, List<RecipeDetail>>>

    @Query("SELECT * FROM Recipe WHERE isActive = 1 ORDER BY Name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM RecipeDetail WHERE RecipeID = :recipeId")
    fun getRecipeDetails(recipeId: Int): Flow<List<RecipeDetail>>

    @Query("DELETE FROM RecipeDetail WHERE RecipeID = :recipeId")
    suspend fun deleteDetailsForRecipe(recipeId: Int)
}
