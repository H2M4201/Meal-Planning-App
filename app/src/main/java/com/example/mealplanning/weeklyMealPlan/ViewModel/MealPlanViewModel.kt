package com.example.mealplanning.weeklyMealPlan.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.ingredientList.data.IngredientDao
import com.example.mealplanning.weeklyMealPlan.data.MealPlan
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDao
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDetail
import com.example.mealplanning.shoppingList.ViewModel.ShoppingListViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class MealPlanViewModel(
    private val mealPlanDao: MealPlanDao,
    private val ingredientDao: IngredientDao // Add the IngredientDao dependency
) : ViewModel() {
//    fun getMealPlansForWeek(startOfWeek: LocalDate): StateFlow<List<MealPlan>> {
//        val endOfWeek = startOfWeek.plusDays(6)
//        return mealPlanDao.getMealPlansForWeek(startOfWeek, endOfWeek)
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000L),
//                initialValue = emptyList()
//            )
//    }
//
//    fun getMealPlanDetails(mealPlanId: Int): StateFlow<List<MealPlanDetail>> {
//        return mealPlanDao.getMealPlanDetails(mealPlanId)
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000L),
//                initialValue = emptyList()
//            )
//    }

    fun getMealPlanWithDetailsByWeek(startOfWeek: LocalDate): StateFlow<Map<MealPlan, List<MealPlanDetail>>> {
        val endOfWeek = startOfWeek.plusDays(6)
        return mealPlanDao.getMealPlanWithDetailsByWeek(startOfWeek, endOfWeek)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap()
            )
    }

    fun saveMealPlan(mealPlan: MealPlan, details: List<MealPlanDetail>) {
        viewModelScope.launch {
            // This returns the new/updated MealPlan's ID
            val mealPlanId = mealPlanDao.insertMealPlan(mealPlan)
            // Associate all details with the correct MealPlan ID
            details.forEach { detail ->
                mealPlanDao.insertMealPlanDetail(detail.copy(MealPlanID = mealPlanId.toInt()))
            }
        }
    }

    fun removeMealPlan(mealPlan: MealPlan) {
        // This will also remove the details due to the CASCADE onDelete
        viewModelScope.launch {
            mealPlanDao.deleteMealPlan(mealPlan)
        }
    }

    fun updateShoppingListForWeek(
        startOfWeek: LocalDate,
        shoppingListVm: ShoppingListViewModel
    ) {
        viewModelScope.launch {
            val endOfWeek = startOfWeek.plusDays(6)
            // 1. Get all raw meal details for the week
            val mealDetails = mealPlanDao.getIngredientsForWeek(startOfWeek, endOfWeek)

            // 2. Group by IngredientID and sum the Amounts so we don't have duplicates
            val itemsToCart = mealDetails
                .groupBy { it.IngredientID }
                .map { (ingredientId, details) ->
                    com.example.mealplanning.shoppingList.data.ShoppingCart(
                        IngredientID = ingredientId,
                        Amount = details.sumOf { it.Amount },
                        week = startOfWeek
                    )
                }

            // 3. Send the aggregated ShoppingCart items to the ShoppingListViewModel
            if (itemsToCart.isNotEmpty()) {
                shoppingListVm.addItemsToCart(itemsToCart)
            }
        }
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
