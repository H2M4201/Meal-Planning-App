package com.example.mealplanning.weeklyMealPlan.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate



@Dao
interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlanDetail(mealPlanDetail: MealPlanDetail)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlan)

    @Transaction
    @Query("""
    SELECT * FROM MealPlan 
    LEFT JOIN MealPlanDetail ON MealPlan.ID = MealPlanDetail.MealPlanID 
    WHERE Date BETWEEN :startOfWeek AND :endOfWeek    """)
    fun getMealPlanWithDetailsByWeek(
        startOfWeek: LocalDate,
        endOfWeek: LocalDate
    ): Flow<Map<MealPlan, List<MealPlanDetail>>>

    @Query("""
    SELECT 
        md.IngredientID, 
        SUM(md.Amount) AS TotalAmount, 
        SUM(md.LastCartUpdated) AS TotalLastUpdated
    FROM MealPlanDetail md
    INNER JOIN MealPlan mp ON md.MealPlanID = mp.ID
    WHERE mp.Date BETWEEN :startOfWeek AND :endOfWeek
    GROUP BY md.IngredientID
""")
    fun getIngredientsForWeek(
        startOfWeek: LocalDate,
        endOfWeek: LocalDate
    ): Flow<List<IngredientSummary>>
    @Update
    suspend fun updateMealPlan(mealPlan: MealPlan)
    @Update
    suspend fun updateMealPlanDetail(detail: MealPlanDetail)

    @Query("UPDATE MealPlanDetail SET LastCartUpdated = Amount WHERE MealPlanID = :mealPlanId AND IngredientID = :ingredientId")
    suspend fun syncCartAmount(mealPlanId: Int, ingredientId: Int)

    @Transaction
    suspend fun updateLastCartSyncAmount(details: List<MealPlanDetail>) {
        details.forEach { detail ->
            // Using raw query for performance or standard update
            syncCartAmount(detail.MealPlanID, detail.IngredientID)
        }
    }
}
