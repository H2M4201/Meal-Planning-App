// In data/RecipeDetail.kt
package com.example.mealplanning.recipe.data

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.mealplanning.ingredientList.data.Ingredient

@Entity(
    tableName = "RecipeDetail",
    primaryKeys = ["RecipeID", "IngredientID"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["ID"],
            childColumns = ["RecipeID"],
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
data class RecipeDetail(
    val RecipeID: Int,
    val IngredientID: Int,
    val Amount: Int
)
