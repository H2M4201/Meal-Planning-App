package com.example.mealplanning.ingredientList.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: Ingredient)

    @Update
    suspend fun update(ingredient: Ingredient)

    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients WHERE name = :name")
    suspend fun getIngredientByName(name: String): Ingredient?
    @Query("SELECT * FROM ingredients WHERE isActive = 1 ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("UPDATE ingredients SET isActive = 0 WHERE ID = :ingredientId")
    suspend fun markAsInactive(ingredientId: Int)
}
