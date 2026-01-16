package com.example.mealplanning.shoppingList.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.mealplanning.ingredientList.data.Ingredient
import java.time.LocalDate

@Entity(
    tableName = "ShoppingCart",
    foreignKeys = [
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["ID"],
            childColumns = ["IngredientID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShoppingCart(
    @PrimaryKey(autoGenerate = true)
    val ID: Int = 0,
    val IngredientID: Int,
    val Amount: Int,
    val week: LocalDate // Room can handle LocalDate with the TypeConverter
)