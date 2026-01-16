package com.example.mealplanning.weeklyMealPlan.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.key
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mealplanning.weeklyMealPlan.data.MealPlan
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDetail
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MealPlanGrid(
    modifier: Modifier = Modifier,
    weekDates: List<LocalDate>,
    mealPlans: Map<MealPlan, List<MealPlanDetail>>,
    onCellClick: (UIMeal) -> Unit
) {
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner")
    val dayFormatter = DateTimeFormatter.ofPattern("EEE MMM d")

    LazyColumn(modifier = modifier) {
        item {
            Row(Modifier.fillMaxWidth()) {
                // standardized weights: 0.25 for each of the 4 columns
                TextCell(text = "", weight = 0.3f, isHeader = true)
                mealTypes.forEach { TextCell(text = it, weight = 0.25f, isHeader = true) }
            }
        }
        items(weekDates) { date ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextCell(text = date.format(dayFormatter), weight = 0.3f)
                mealTypes.forEachIndexed { index, _ ->
                    val mealType = index
                    val entry = mealPlans.entries.find {
                        it.key.Date == date && it.key.MealType == mealType
                    }
                    val uiMeal = UIMeal(
                        mealPlan = entry?.key,
                        details = entry?.value ?: emptyList(),
                        date = date,
                        mealType = mealType
                    )

                    MealCell(
                        mealPlan = entry?.key,
                        weight = 0.25f, // Match header weight
                        onClick = { onCellClick(uiMeal) }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.MealCell(mealPlan: MealPlan?, weight: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(60.dp)
            .border(BorderStroke(0.5.dp, Color.White))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // MODIFICATION: This logic now correctly determines what to display.
        if (mealPlan == null) {
            // If there's no meal plan, the cell is empty. Show nothing.
        } else if (mealPlan.IsEatOut) {
            // If it's an "Eat Out" meal, show an 'X'.
            Text(
                text = "X",
                color = Color.Red,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            // If it's not "Eat Out" (i.e., a cooked meal), show a checkmark.
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Cook",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun RowScope.TextCell(text: String, weight: Float, isHeader: Boolean = false) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(if (isHeader) 40.dp else 60.dp)
            .border(BorderStroke(0.5.dp, Color.White)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
