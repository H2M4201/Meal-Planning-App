package com.example.mealplanning.recipe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Recipe")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val ID: Int = 0,
    val Name: String,
    val isActive: Int = 1
)
