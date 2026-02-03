package com.example.mealplanning.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.recipe.components.RecipeDetailDialog
import com.example.mealplanning.recipe.data.Recipe
import com.example.mealplanning.recipe.data.RecipeDetail
import com.example.mealplanning.recipe.ViewModel.RecipeViewModel
import com.example.mealplanning.shareUI.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    onNavigateUp: () -> Unit,
    recipeVm: RecipeViewModel,
    ingredientListVm: IngredientListViewModel
) {
    val recipes by recipeVm.allRecipes.collectAsState()
    var showDetailDialog by remember { mutableStateOf<Recipe?>(null) }
    var isNewRecipe by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "Recipes", onNavigateUp = onNavigateUp) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showDetailDialog = Recipe(Name = "")
                    isNewRecipe = true
                },
                containerColor = Color.Green,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, "Add Recipe")
            }
        },
        containerColor = Color(0xFF2C2C2C)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(recipes, key = { it.ID }) { recipe ->
                RecipeRow(
                    recipe = recipe,
                    onEdit = {
                        showDetailDialog = recipe
                        isNewRecipe = false
                    },
                    onDelete = { recipeVm.deleteRecipe(recipe) }
                )
            }
        }
    }

    showDetailDialog?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            isNew = isNewRecipe,
            recipeVm = recipeVm,
            ingredientListVm = ingredientListVm,
            onDismiss = { showDetailDialog = null }
        )
    }
}

@Composable
fun RecipeRow(recipe: Recipe, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF0703C).copy(alpha = 0.9f)
        ) {
            Text(
                text = recipe.Name,
                modifier = Modifier.padding(16.dp),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(
            onClick = onEdit,
            modifier = Modifier.background(Color.White, shape = RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Default.Edit, "Edit", tint = Color.Black)
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.background(Color(0xFFFF5252), shape = RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Default.Close, "Delete", tint = Color.White)
        }
    }
}
