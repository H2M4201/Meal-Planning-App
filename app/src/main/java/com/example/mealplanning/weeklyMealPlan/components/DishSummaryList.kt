package com.example.mealplanning.weeklyMealPlan.components

import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mealplanning.weeklyMealPlan.data.MealPlan
import java.time.format.DateTimeFormatter

@Composable
fun DishSummaryDialog(
    dishes: Map<java.time.LocalDate, List<MealPlan>>,
    onDismiss: () -> Unit // Accept the Map directly    onDismiss: () -> Unit
) {
    val dateSeparatorFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    val stableGroupedDishes by remember(dishes) {
        derivedStateOf { dishes }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dishes to Cook this Week",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (stableGroupedDishes.isEmpty()) {
                        item {
                            Text(
                                "No dishes to cook.",
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Use stableGroupedDishes instead of the raw dishes parameter
                        stableGroupedDishes.forEach { (date, dailyMeals) ->
                            // Use a stable key for the header
                            item(key = "header_${date}") {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${date.format(dateSeparatorFormatter)}:",
                                        color = Color(0xFFF0703C),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    // Sorting here is fine, but stable keys in items() would be better
                                    // if this were a very long list.
                                    dailyMeals.sortedBy { it.MealType }.forEach { meal ->
                                        Row(
                                            modifier = Modifier
                                                .padding(start = 12.dp, bottom = 2.dp)
                                                .fillMaxWidth()
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
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}