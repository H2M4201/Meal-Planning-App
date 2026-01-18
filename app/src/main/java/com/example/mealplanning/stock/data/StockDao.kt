package com.example.mealplanning.stock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(stock: Stock)

    @Delete
    suspend fun delete(stock: Stock)

    @Query("SELECT * FROM Stock")
    fun getAllStock(): Flow<List<Stock>>

    @Query("SELECT * FROM Stock WHERE IngredientID = :id LIMIT 1")
    suspend fun getStockById(id: Int): Stock?
}