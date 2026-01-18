package com.example.mealplanning.weeklyMealPlan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.shareUI.components.AppTopBar
import com.example.mealplanning.shoppingList.ViewModel.ShoppingListViewModel
import com.example.mealplanning.weeklyMealPlan.data.MealPlan
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDetail
import com.example.mealplanning.weeklyMealPlan.ViewModel.MealPlanViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

// This is the new data class that will represent a cell in our UI.
// It holds the MealPlan from the DB and its associated details.
data class UIMeal(
    val mealPlan: MealPlan? = null,
    val details: List<MealPlanDetail> = emptyList(),
    // We still need the date and type for empty cells
    val date: LocalDate,
    val mealType: Int // 0=Breakfast, 1=Lunch, 2=Dinner
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyMealPlanScreen(
    onNavigateUp: () -> Unit,
    vm: MealPlanViewModel,
    shoppingListVm: ShoppingListViewModel,
    ingredientListVm: IngredientListViewModel
) {
    // --- STATE MANAGEMENT ---
    var startOfWeek by remember { mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))) }
    val weekDates = remember(startOfWeek) { List(7) { i -> startOfWeek.plusDays(i.toLong()) } }

    // This is the core logic change: Read directly from the ViewModel's Flow
    val weeklyMealPlans by vm.getMealPlanWithDetailsByWeek(startOfWeek).collectAsState()
    var showMealChoiceDialog by remember { mutableStateOf<UIMeal?>(null) }
    var showCookDialog by remember { mutableStateOf<UIMeal?>(null) }



    Scaffold(
        topBar = {
            AppTopBar(title = "Weekly Meal Plan", onNavigateUp = onNavigateUp)
        },
        containerColor = Color(0xFF2C2C2C)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopControls(
                onLastWeek = { startOfWeek = startOfWeek.minusWeeks(1) },
                onNextWeek = { startOfWeek = startOfWeek.plusWeeks(1) },
                onUpdateShoppingList = {
                    // Flatten all MealPlanDetail objects from the current week's map
                    val allDetails = weeklyMealPlans.values.flatten()

                    if (allDetails.isNotEmpty()) {
                        shoppingListVm.updateShoppingListFromMealPlan(allDetails, startOfWeek)
                    }
                }
            )
            MealPlanGrid(
                modifier = Modifier.weight(1f),
                weekDates = weekDates,
                mealPlans = weeklyMealPlans,
                onCellClick = { uiMeal ->
                    // MODIFICATION: Check IsEatOut status or if it's a new meal
                    // If it's already marked as EatOut, or if it's empty, show Choice dialog
                    if (uiMeal.mealPlan == null || uiMeal.mealPlan.IsEatOut) {
                        showMealChoiceDialog = uiMeal
                    } else {
                        // If it's an existing Cooked meal, show Choice dialog to allow changing to EatOut
                        showCookDialog = uiMeal
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
//                    vm.saveMealPlan(localMealPlan)
                    onNavigateUp()
                }) { Text("Save") }
                Button(onClick = onNavigateUp) { Text("Cancel") }
            }
        }
    }

    showMealChoiceDialog?.let { meal ->
        MealChoiceDialog(
            meal = meal,
            onDismiss = { showMealChoiceDialog = null },
            onResult = { choice ->
                showMealChoiceDialog = null // Close the simple dialog
                when (choice) {
                    MealChoice.COOK -> {
                        // If user chooses "Cook", open the detailed CookDialog
                        showCookDialog = meal
                    }
                    MealChoice.EAT_OUT -> {
                        // If user chooses "Eat Out", save it directly
                        val eatOutMealPlan = MealPlan(
                            ID = 0, // New entry
                            Date = meal.date,
                            MealType = meal.mealType,
                            IsEatOut = true,
                            MealName = "Eat Out"
                        )
                        vm.saveMealPlan(eatOutMealPlan, emptyList())
                    }
                }
            }
        )
    }

    showCookDialog?.let { uiMeal ->
        CookDialog(
            meal = uiMeal,
            ingredientListVm = ingredientListVm,
            onDismiss = { showCookDialog = null },
            onSave = { mealPlan, details ->
                vm.saveMealPlan(mealPlan, details)
                showCookDialog = null
            },
            onRemove = {
                if (uiMeal.mealPlan != null) {
                    vm.removeMealPlan(uiMeal.mealPlan)
                }
                showCookDialog = null
            },
            onSetEatOut = {
                val eatOutMealPlan = MealPlan(
                    ID = uiMeal.mealPlan?.ID ?: 0,
                    Date = uiMeal.date,
                    MealType = uiMeal.mealType,
                    IsEatOut = true,
                    MealName = "Eat Out"
                )
                vm.saveMealPlan(eatOutMealPlan, emptyList())
                showCookDialog = null
            }
        )
    }
}

@Composable
fun TopControls(
    onLastWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onUpdateShoppingList: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onLastWeek) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Last Week",
                    tint = Color.White
                )
            }
            Text(
                text = "Last Week",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Next Week",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onNextWeek) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Week",
                    tint = Color.White
                )
            }
        }

        TextButton(onClick = onUpdateShoppingList) {
            Text(
                text = "Update\nShopping List",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }


}
