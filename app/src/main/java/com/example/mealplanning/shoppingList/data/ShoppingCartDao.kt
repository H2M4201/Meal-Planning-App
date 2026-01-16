package com.example.mealplanning.shoppingList.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ShoppingCartDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ShoppingCart)

    @Delete
    suspend fun delete(item: ShoppingCart)

    @Query("SELECT * FROM ShoppingCart WHERE week = :week")
    fun getCartForWeek(week: LocalDate): Flow<List<ShoppingCart>>
}
