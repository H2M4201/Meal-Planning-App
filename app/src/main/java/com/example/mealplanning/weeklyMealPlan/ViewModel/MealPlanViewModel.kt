package com.example.mealplanning.weeklyMealPlan.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.ingredientList.data.IngredientDao
import com.example.mealplanning.shoppingList.ViewModel.ShoppingListViewModel
import com.example.mealplanning.weeklyMealPlan.data.IngredientSummary
import com.example.mealplanning.weeklyMealPlan.data.MealPlan
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDao
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MealPlanViewModel(
    private val mealPlanDao: MealPlanDao,
    private val ingredientDao: IngredientDao // Add the IngredientDao dependency
) : ViewModel() {


    fun getMealPlanWithDetailsByWeek(startOfWeek: LocalDate): StateFlow<Map<MealPlan, List<MealPlanDetail>>> {
        val endOfWeek = startOfWeek.plusDays(6)
        return mealPlanDao.getMealPlanWithDetailsByWeek(startOfWeek, endOfWeek)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap()
            )
    }

    fun saveMealPlan(mealPlan: MealPlan, details: List<MealPlanDetail>, isNew: Boolean) {
        viewModelScope.launch {
            if (isNew) {
                // New Logic: Insert and get new ID
                val mealPlanId = mealPlanDao.insertMealPlan(mealPlan)
                details.forEach { detail ->
                    mealPlanDao.insertMealPlanDetail(detail.copy(MealPlanID = mealPlanId.toInt()))
                }
            } else {
                // Update Logic: Explicit Update for the main record
                mealPlanDao.updateMealPlan(mealPlan)

                // 2. Insert the current list of details
                details.forEach { detail ->
                    mealPlanDao.updateMealPlanDetail(detail)
                }
            }
        }
    }


    fun removeMealPlan(mealPlan: MealPlan) {
        // This will also remove the details due to the CASCADE onDelete
        viewModelScope.launch {
            mealPlanDao.deleteMealPlan(mealPlan)
        }
    }

    fun getTotalIngredientsByWeek(startOfWeek: LocalDate): StateFlow<List<IngredientSummary>> {
        val endOfWeek = startOfWeek.plusDays(6)
        return mealPlanDao.getIngredientsForWeek(startOfWeek, endOfWeek)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    fun syncMealPlanIngredientsToCart(
        details: List<MealPlanDetail>, // Passed from Screen
        startOfWeek: LocalDate,
        shoppingListVm: ShoppingListViewModel
    ) {
        viewModelScope.launch {
            // Step A: Mark the current state as "synced" by updating LastCartUpdated = Amount
            // This must be done BEFORE we calculate the next delta
            mealPlanDao.updateLastCartSyncAmount(details)

            // Step B: Collect the latest totals (which now include the updated LastCartUpdated)
            // We fetch one snapshot to pass to the shopping list
            val ingredientSummaries = getTotalIngredientsByWeek(startOfWeek).first()

            // Step C: Hand over the summaries to ShoppingList to calculate the delta
            shoppingListVm.updateShoppingListFromMealPlan(ingredientSummaries, startOfWeek)
        }
    }

    fun getDishesToCook(startOfWeek: LocalDate): StateFlow<List<MealPlan>> {
        val endOfWeek = startOfWeek.plusDays(6)
        return mealPlanDao.getMealPlanWithDetailsByWeek(startOfWeek, endOfWeek)
            .map { map ->
                map.keys
                    .filter { it.Status != 0 }
                    .sortedWith(compareBy({ it.Date }, { it.MealType }))
            }
            .distinctUntilChanged() // Prevents emissions if the list content is the same
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }


}

class MealPlanViewModelFactory(
    private val mealPlanDao: MealPlanDao,
    private val ingredientDao: IngredientDao // Add IngredientDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealPlanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealPlanViewModel(mealPlanDao, ingredientDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
