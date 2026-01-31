package com.example.mealplanning.shoppingList.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ShoppingCartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingCart)

    @Delete
    suspend fun delete(item: ShoppingCart)

    @Query("SELECT * FROM ShoppingCart WHERE week = :week")
    fun getCartForWeek(week: LocalDate): Flow<List<ShoppingCart>>

    @Query("SELECT * FROM ShoppingCart WHERE IngredientID = :id AND week = :week LIMIT 1")
    suspend fun getCartItem(id: Int, week: LocalDate): ShoppingCart?

    @Update
    suspend fun update(shoppingCart: ShoppingCart)

    @Query("UPDATE ShoppingCart SET LastUpdatedStock = Amount WHERE ID = :id")
    suspend fun saveLastUpdatedStock(id: Int)

    @Transaction
    suspend fun updateLastStockSyncAmountLoop(items: List<ShoppingCart>) {
        items.forEach { item ->
            saveLastUpdatedStock(item.ID)
        }
    }
}
