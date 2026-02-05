package com.example.mealplanning.recipe.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.recipe.data.Recipe
import com.example.mealplanning.recipe.data.RecipeDetail
import com.example.mealplanning.recipe.ViewModel.RecipeViewModel
import com.example.mealplanning.shareUI.components.IngredientDialog
import com.example.mealplanning.weeklyMealPlan.components.DialogButton

@Composable
fun RecipeDetailDialog(
    recipe: Recipe,
    isNew: Boolean,
    recipeVm: RecipeViewModel,
    ingredientListVm: IngredientListViewModel,
    onDismiss: () -> Unit
) {
    var recipeName by remember { mutableStateOf(recipe.Name) }
    var recipeNameError by remember { mutableStateOf(false) }

    val details = remember { mutableStateListOf<RecipeDetail>() }
    val masterIngredients by ingredientListVm.masterIngredients.collectAsState()

    LaunchedEffect(recipe) {
        if (!isNew) {
            // Since getRecipeDetails is now a suspend function returning a List
            val recipeDetails = recipeVm.getRecipeDetails(recipe.ID)
            details.clear()
            details.addAll(recipeDetails)
        }
    }

    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var editingDetail by remember { mutableStateOf<RecipeDetail?>(null) }

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
                    text = if (isNew) "Add Recipe" else "Edit Recipe",
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Search saved formula") },
                    shape = RoundedCornerShape(50),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = {
                        recipeName = it
                        if (it.isNotBlank()) recipeNameError = false
                    },
                    label = { Text("Enter Recipe Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = recipeNameError,
                    supportingText = {
                        if (recipeNameError) Text("Recipe Name cannot be empty", color = MaterialTheme.colorScheme.error)
                    }
                )
                Spacer(Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    itemsIndexed(details, key = { index, detail -> "${detail.IngredientID}-$index" }) { _, detail ->
                        masterIngredients.find { it.ID == detail.IngredientID }?.let { masterIngredient ->
                            RecipeIngredientRow(
                                masterIngredient = masterIngredient,
                                amount = detail.Amount,
                                onEdit = { editingDetail = detail },
                                onDelete = { details.remove(detail) }
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
                        if (recipeName.isBlank()) {
                            recipeNameError = true
                        } else {
                            recipeVm.saveRecipe(recipe.copy(Name = recipeName), details.toList())
                            onDismiss()
                        }
                    }) { Text("Save") }
                    DialogButton(text = "Cancel", onClick = onDismiss)
                }
            }
        }
    }

    if (showAddIngredientDialog) {
        IngredientDialog(
            ingredientListVm = ingredientListVm,
            isFromCookDialog = true,
            onDismiss = { showAddIngredientDialog = false },
            onSave = { name, amount, unit ->
                val existingIngredient = masterIngredients.find { it.Name.equals(name, ignoreCase = true) }
                val parsedAmount = amount.toIntOrNull()

                if (existingIngredient != null && parsedAmount != null) {
                    details.add(RecipeDetail(RecipeID = recipe.ID, IngredientID = existingIngredient.ID, Amount = parsedAmount))
                }
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
                        val index = details.indexOfFirst { it.IngredientID == detailToEdit.IngredientID }
                        if (index != -1) {
                            details[index] = detailToEdit.copy(Amount = parsedAmount)
                        }
                    }
                    editingDetail = null
                }
            )
        }
    }
}

@Composable
fun RecipeIngredientRow(
    masterIngredient: Ingredient,
    amount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
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
                .size(40.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = Color.Black)
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .background(Color(0xFFFF5252), shape = RoundedCornerShape(8.dp))
                .size(40.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Delete Item", tint = Color.White)
        }
    }
}
