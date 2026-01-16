package com.example.mealplanning.stock.data

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.mealplanning.ingredientList.data.Ingredient

@Entity(
    tableName = "Stock",
    primaryKeys = ["IngredientID"],
    foreignKeys = [
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["ID"],
            childColumns = ["IngredientID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Stock(
    val IngredientID: Int,
    val Amount: Int
)
