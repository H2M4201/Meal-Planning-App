package com.example.mealplanning.weeklyMealPlan

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.shoppingList.ViewModel.ShoppingListViewModel
import com.example.mealplanning.shareUI.theme.MealPlanningTheme
import com.example.mealplanning.weeklyMealPlan.ViewModel.MealPlanViewModel
import com.example.mealplanning.weeklyMealPlan.components.WeeklyMealPlanScreen

// Data classes and enums remain in this central file as they define the core data structure

// The main entry point composable, now much cleaner.
@Composable
fun WeeklyMealPlanEntry(
    onNavigateUp: () -> Unit,
    vm: MealPlanViewModel,
    shoppingListVm: ShoppingListViewModel,
    ingredientListVm: IngredientListViewModel
) {
    WeeklyMealPlanScreen(
        onNavigateUp = onNavigateUp,
        vm = vm,
        shoppingListVm = shoppingListVm,
        ingredientListVm = ingredientListVm
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF2C2C2C)
@Composable
fun WeeklyMealPlanScreenPreview() {
    MealPlanningTheme {
        // This preview will show an empty screen, which is acceptable
        // as the logic is now too complex for a simple preview.
        WeeklyMealPlanEntry(
            onNavigateUp = {},
            vm = viewModel(), // This will be an empty VM for preview
            shoppingListVm = viewModel(),
            ingredientListVm = viewModel()
        )
    }
}
