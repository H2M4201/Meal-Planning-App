package com.example.mealplanning.weeklyMealPlan.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "MealPlan")
data class MealPlan(
    @PrimaryKey(autoGenerate = true)
    val ID: Int = 0,
    val Date: LocalDate,
    val MealType: Int, // e.g., 0 for Breakfast, 1 for Lunch, 2 for Dinner, 3 for Snack
    val Status: Int, // Renamed from IsEatOut
    val MealName: String
)
