package com.example.mealplanning.ingredientList.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mealplanning.ingredientList.data.Ingredient

@Composable
fun MasterIngredientDialog(
    ingredient: Ingredient? = null, // Accept an optional ingredient to edit
    onDismiss: () -> Unit,
    onSave: (name: String, unit: String) -> Unit
) {
    var ingredientName by remember { mutableStateOf(ingredient?.Name ?: "") }
    var ingredientUnit by remember { mutableStateOf(ingredient?.Unit ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Search saved item") },
                    shape = RoundedCornerShape(50),
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    label = { Text("Enter Ingredient Name") },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredientUnit,
                    onValueChange = { ingredientUnit = it },
                    label = { Text("Enter Unit") },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (ingredientName.isNotBlank() && ingredientUnit.isNotBlank()) {
                                onSave(ingredientName, ingredientUnit)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Save", color = Color.Black)
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
