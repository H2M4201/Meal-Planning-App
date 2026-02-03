package com.example.mealplanning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mealplanning.data.AppDatabase
import com.example.mealplanning.ingredientList.IngredientListScreen
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModelFactory
import com.example.mealplanning.landingPage.MainScreen
import com.example.mealplanning.recipe.RecipeScreen
import com.example.mealplanning.recipe.ViewModel.RecipeViewModel
import com.example.mealplanning.recipe.ViewModel.RecipeViewModelFactory
import com.example.mealplanning.shareUI.theme.MealPlanningTheme
import com.example.mealplanning.shoppingList.ShoppingListScreen
import com.example.mealplanning.shoppingList.ViewModel.ShoppingListViewModel
import com.example.mealplanning.shoppingList.ViewModel.ShoppingListViewModelFactory
import com.example.mealplanning.stock.StockScreen
import com.example.mealplanning.stock.ViewModel.StockViewModel
import com.example.mealplanning.stock.ViewModel.StockViewModelFactory
import com.example.mealplanning.weeklyMealPlan.ViewModel.MealPlanViewModel
import com.example.mealplanning.weeklyMealPlan.WeeklyMealPlanEntry
import com.example.mealplanning.weeklyMealPlan.ViewModel.MealPlanViewModelFactory



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MealPlanningTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF2C2C2C)
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext
    val db = AppDatabase.getDatabase(context)

    val ingredientListViewModel: IngredientListViewModel = viewModel(factory = IngredientListViewModelFactory(db.ingredientDao()))
    val stockViewModel: StockViewModel = viewModel(factory = StockViewModelFactory(db.stockDao()))
    val shoppingListViewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModelFactory(db.shoppingCartDao()))
    val mealPlanViewModel: MealPlanViewModel = viewModel(
        factory = MealPlanViewModelFactory(
            mealPlanDao = db.mealPlanDao(),
            ingredientDao = db.ingredientDao()
        )
    )
    val recipeViewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(
            recipeDao = db.recipeDao()
        )
    )

    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") {
            MainScreen(
                onNavigateToWeeklyPlan = { navController.navigate("weeklyPlan") },
                onNavigateToUpdateStock = { navController.navigate("updateStock") },
                onNavigateToShoppingList = { navController.navigate("shoppingList") },
                onNavigateToIngredientList = { navController.navigate("ingredientList") },
                onNavigateToRecipe = { navController.navigate("recipe") }
            )
        }
        composable("weeklyPlan") {
            WeeklyMealPlanEntry(
                onNavigateUp = { navController.navigateUp() },
                vm = mealPlanViewModel,
                shoppingListVm = shoppingListViewModel,
                ingredientListVm = ingredientListViewModel
            )
        }
        composable("updateStock") {
            StockScreen(
                onNavigateUp = { navController.navigateUp() },
                vm = stockViewModel,
                ingredientListVm = ingredientListViewModel
            )
        }
        composable("shoppingList") {
            ShoppingListScreen(
                onNavigateUp = { navController.navigateUp() },
                shoppingListVm = shoppingListViewModel,
                stockVm = stockViewModel,
                ingredientListVm = ingredientListViewModel
            )
        }
        composable("ingredientList") {
            IngredientListScreen(
                onNavigateUp = { navController.navigateUp() },
                IngredientVm = ingredientListViewModel,
                StockVm = stockViewModel
            )
        }
        composable("recipe") {
            RecipeScreen(
                onNavigateUp = { navController.navigateUp() },
                recipeVm = recipeViewModel,
                ingredientListVm = ingredientListViewModel
            )
        }
    }
}
