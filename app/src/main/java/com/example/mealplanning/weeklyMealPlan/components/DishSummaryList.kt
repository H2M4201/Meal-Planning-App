package com.example.mealplanning.weeklyMealPlan.components

import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mealplanning.weeklyMealPlan.data.MealPlan
import java.time.format.DateTimeFormatter

@Composable
fun DishSummaryList(
    modifier: Modifier = Modifier,
    dishes: List<MealPlan>
) {
    val groupedDishes = remember(dishes) {
        dishes.groupBy { it.Date }
    }

    val dateHeaderFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    Column(modifier = modifier) {
        Text(
            text = "Dishes to Cook this Week",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Iterate through the Map entries (Date -> List of Meals)
            groupedDishes.forEach { (date, dailyMeals) ->

                // 1. Add the Date Header (e.g., Sat, Jan 13:)
                item(key = date.toString()) {
                    Text(
                        text = "${date.format(dateHeaderFormatter)}:",
                        color = Color(0xFFF0703C), // Accent color for the date
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // 2. Add each meal for that day as an item
                items(dailyMeals, key = { it.ID }) { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = "- ${getMealTypeName(meal.MealType)}: ",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = meal.MealName,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}