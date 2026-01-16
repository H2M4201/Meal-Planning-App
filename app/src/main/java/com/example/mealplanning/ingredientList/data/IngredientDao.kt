package com.example.mealplanning.ingredientList.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict  = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(ingredient: Ingredient)

    // NEW: Add the delete function
    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>
}