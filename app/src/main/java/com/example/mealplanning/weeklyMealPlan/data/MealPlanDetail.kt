package com.example.mealplanning.weeklyMealPlan.data

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.mealplanning.ingredientList.data.Ingredient

@Entity(
    tableName = "MealPlanDetail",
    primaryKeys = ["MealPlanID", "IngredientID"],
    foreignKeys = [
        ForeignKey(
            entity = MealPlan::class,
            parentColumns = ["ID"],
            childColumns = ["MealPlanID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["ID"],
            childColumns = ["IngredientID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MealPlanDetail(
    val MealPlanID: Int,
    val IngredientID: Int,
    val Amount: Int
)
