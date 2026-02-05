package com.example.mealplanning.weeklyMealPlan.components

import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mealplanning.ingredientList.data.Ingredient // CORRECTED IMPORT
import com.example.mealplanning.weeklyMealPlan.data.MealPlan
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDetail
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.recipe.ViewModel.RecipeViewModel
import com.example.mealplanning.recipe.data.Recipe
import com.example.mealplanning.shareUI.components.IngredientDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.collections.toTypedArray

@Composable
fun CookDialog(
    meal: UIMeal, // Use the UIMeal wrapper class
    ingredientListVm: IngredientListViewModel,
    recipeVm: RecipeViewModel,
    onDismiss: () -> Unit,
    // MODIFICATION: onSave now returns the MealPlan and its details
    onSave: (mealPlan: MealPlan, details: List<MealPlanDetail>) -> Unit,
    onRemove: () -> Unit,
    onSetEatOut: () -> Unit
) {
     var dishName by remember { mutableStateOf(meal.mealPlan?.MealName ?: "") }
     var dishNameError by remember { mutableStateOf(false) }

     // This holds the ingredients for the current recipe being edited
     val recipeIngredients = remember { mutableStateListOf(*meal.details.toTypedArray()) }
     val masterIngredients by ingredientListVm.masterIngredients.collectAsState()
    val scope = rememberCoroutineScope() // Needed for async recipe fetch

     var showAddIngredientDialog by remember { mutableStateOf(false) }
     var editingDetail by remember { mutableStateOf<MealPlanDetail?>(null) }
     val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
    var showRecipeSearchDialog by remember { mutableStateOf(false) } // MODIFICATION: New state

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${getMealTypeName(meal.mealType)} - ${meal.date.format(dateFormatter)}",
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top=16.dp)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Search saved recipe") },
                    shape = RoundedCornerShape(50),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRecipeSearchDialog = true }, // Trigger dialog
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dishName,
                    onValueChange = {
                        dishName = it
                        if (it.isNotBlank()) dishNameError = false
                    },
                    label = { Text("Enter Dish Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = dishNameError,
                    supportingText = {
                        if (dishNameError) Text("Dish Name cannot be empty", color = MaterialTheme.colorScheme.error)
                    }
                )
                Spacer(Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    itemsIndexed(recipeIngredients, key = { index, detail -> "${detail.IngredientID}-$index" }) { _, detail ->
                        masterIngredients.find { it.ID == detail.IngredientID }?.let { masterIngredient ->
                            IngredientRow(
                                masterIngredient = masterIngredient,
                                amount = detail.Amount,
                                onEdit = { editingDetail = detail },
                                onDelete = { recipeIngredients.remove(detail) }
                            )
                        }
                    }
                }



                TextButton(onClick = { showAddIngredientDialog = true }) {
                    Text("Add more ingredient")
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = {
                        if (dishName.isBlank()) {
                            dishNameError = true
                        } else {
                            val newMealPlan = MealPlan(
                                ID = meal.mealPlan?.ID ?: 0,
                                Date = meal.date,
                                MealType = meal.mealType,
                                Status = 1,
                                MealName = dishName
                            )
                            onSave(newMealPlan, recipeIngredients.toList())
                        }
                    }) { Text("Save") }
                    Button(onClick = onSetEatOut) { Text("Eat Out") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DialogButton(text = "Remove", onClick = onRemove, color = MaterialTheme.colorScheme.error)
                    DialogButton(text = "Cancel", onClick = onDismiss)
                }
            }
        }
    }

    if (showAddIngredientDialog) {
        IngredientDialog(
            ingredientListVm = ingredientListVm,
            isFromCookDialog = true, // Enables the Search bar
            onDismiss = { showAddIngredientDialog = false },
            onSave = { name, amount, unit ->
                val existingIngredient = masterIngredients.find { it.Name.equals(name, ignoreCase = true) }
                val parsedAmount = amount.toIntOrNull()

                if (existingIngredient != null && parsedAmount != null) {
                    val newDetail = MealPlanDetail(
                        MealPlanID = meal.mealPlan?.ID ?: 0,
                        IngredientID = existingIngredient.ID,
                        Amount = parsedAmount
                    )
                    recipeIngredients.add(newDetail)
                }
                // if parsedAmount == null, skip adding
                showAddIngredientDialog = false
            }
        )
    }

    editingDetail?.let { detailToEdit ->
        val masterIngredient = masterIngredients.find { it.ID == detailToEdit.IngredientID }
        if (masterIngredient != null) {
            IngredientDialog(
                ingredient = masterIngredient,
                initialAmount = detailToEdit.Amount.toString(),
                ingredientListVm = ingredientListVm,
                onDismiss = { editingDetail = null },
                onSave = { _, newAmount, _ ->
                    val parsedAmount = newAmount.toIntOrNull()
                    if (parsedAmount != null) {
                        val index = recipeIngredients.indexOfFirst { it.IngredientID == detailToEdit.IngredientID }
                        if (index != -1) {
                            recipeIngredients[index] = detailToEdit.copy(Amount = parsedAmount)
                        }
                    }
                    // if parsedAmount == null, skip updating
                    editingDetail = null
                }
            )
        }
    }

    if (showRecipeSearchDialog) {
        RecipeSearchDialog(
            recipeVm = recipeVm,
            onDismiss = { showRecipeSearchDialog = false },
            onRecipeSelected = { selectedRecipe ->
                scope.launch {
                    // 1. Fill dish name
                    dishName = selectedRecipe.Name

                    // 2. Fetch recipe details
                    val recipeDetails = recipeVm.getRecipeDetails(selectedRecipe.ID).first()

                    // 3. Merge ingredients
                    recipeDetails.forEach { detail ->
                        val existingIndex = recipeIngredients.indexOfFirst { it.IngredientID == detail.IngredientID }
                        if (existingIndex != -1) {
                            // Ingredient exists: sum the amounts
                            val existing = recipeIngredients[existingIndex]
                            recipeIngredients[existingIndex] = existing.copy(Amount = existing.Amount + detail.Amount)
                        } else {
                            // New ingredient: add to list
                            recipeIngredients.add(
                                MealPlanDetail(
                                    MealPlanID = meal.mealPlan?.ID ?: 0,
                                    IngredientID = detail.IngredientID,
                                    Amount = detail.Amount
                                )
                            )
                        }
                    }
                }
                showRecipeSearchDialog = false
            }
        )
    }
}

// Helper function to get meal type name
fun getMealTypeName(type: Int): String {
    return when (type) {
        0 -> "Breakfast"
        1 -> "Lunch"
        else -> "Dinner"
    }
}

@Composable
fun IngredientRow(
    masterIngredient: Ingredient,
    amount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier.width(130.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0703C)
        ) {
            Text(
                // Get Name from master ingredient
                text = masterIngredient.Name,
                modifier = Modifier.padding(8.dp),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Surface(
            modifier = Modifier.width(65.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0703C)
        ) {
            Text(
                // MODIFICATION 4: Get Amount from recipe ingredient and Unit from master ingredient
                text = "$amount${masterIngredient.Unit}",
                modifier = Modifier.padding(8.dp),
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    IconButton(
        onClick = onEdit,
        modifier = Modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .size(width = 40.dp, height = 40.dp)
    ) {
        Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = Color.Black)
    }

    IconButton(
        onClick = onDelete,
        modifier = Modifier
            .background(Color(0xFFFF5252), shape = RoundedCornerShape(8.dp))
            .size(width = 40.dp, height = 40.dp)
    ) {
        Icon(Icons.Default.Close, contentDescription = "Delete Item", tint = Color.White)
    }
}
}

@Composable
fun DialogButton(text: String, onClick: () -> Unit, color: Color = Color.Gray) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text)
    }
}

@Composable
fun RecipeSearchDialog(
    recipeVm: RecipeViewModel,
    onDismiss: () -> Unit,
    onRecipeSelected: (Recipe) -> Unit
) {
    val recipes by recipeVm.allRecipes.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.heightIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select a Recipe", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recipes) { recipe ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRecipeSelected(recipe) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0703C)
                        ) {
                            Text(
                                text = recipe.Name,
                                modifier = Modifier.padding(12.dp),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
