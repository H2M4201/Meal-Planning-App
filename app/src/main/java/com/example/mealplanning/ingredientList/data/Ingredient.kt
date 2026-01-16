package com.example.mealplanning.ingredientList.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients") // This tells Room to create a table named "ingredients"
data class Ingredient(
    @PrimaryKey(autoGenerate = true) // Room will automatically handle generating unique IDs
    val ID: Int = 0,
    var Name: String,
    var Unit: String
)