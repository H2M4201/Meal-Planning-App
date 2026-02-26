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
                // Logic for a brand new MealPlan entry
                val mealPlanId = mealPlanDao.insertMealPlan(mealPlan)
                details.forEach { detail ->
                    mealPlanDao.insertMealPlanDetail(detail.copy(MealPlanID = mealPlanId.toInt()))
                }
            } else {
                // Logic for updating an existing MealPlan (or converting Eat Out to Cook)
                mealPlanDao.updateMealPlan(mealPlan)

                // 1. DELETE all existing details for this meal first.
                // This ensures rows removed in the UI are actually gone from the DB,
                // and clears the way for a fresh set of details.
                mealPlanDao.deleteDetailsForMealPlan(mealPlan.ID)

                // 2. INSERT the current list of details as fresh rows.
                // Since they are new rows for this MealPlanID, we use insert instead of update.
                details.forEach { detail ->
                    // Ensure the detail is linked to the correct MealPlan ID
                    mealPlanDao.insertMealPlanDetail(detail.copy(MealPlanID = mealPlan.ID))
                }
            }
        }
    }

    suspend fun removeMealPlanSync(mealPlan: MealPlan) {
        mealPlanDao.deleteMealPlan(mealPlan)
    }
     fun removeMealPlan(mealPlan: MealPlan) {
        // This will also remove the details due to the CASCADE onDelete
        viewModelScope.launch {
            mealPlanDao.deleteMealPlan(mealPlan)
        }
    }

    suspend fun getTotalIngredientsByWeek(startOfWeek: LocalDate): List<IngredientSummary> {
        val endOfWeek = startOfWeek.plusDays(6)
        // .first() will wait for the database to emit the first result and then close the connection
        return mealPlanDao.getIngredientsForWeek(startOfWeek, endOfWeek).first()
    }



    fun getDishesToCook(startOfWeek: LocalDate): StateFlow<Map<LocalDate, List<MealPlan>>> {
        val endOfWeek = startOfWeek.plusDays(6)
        return mealPlanDao.getMealPlanWithDetailsByWeek(startOfWeek, endOfWeek)
            .map { map ->
                map.keys
                    .filter { it.Status != 0 } // Filter out "Eat Out"
                    .sortedWith(compareBy({ it.Date }, { it.MealType }))
                    .groupBy { it.Date } // Grouping happens HERE
            }
            .distinctUntilChanged() // Crucial: prevents blinking if data is identical
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap()
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
