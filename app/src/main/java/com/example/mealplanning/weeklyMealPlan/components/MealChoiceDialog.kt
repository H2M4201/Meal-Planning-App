package com.example.mealplanning.weeklyMealPlan.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.format.DateTimeFormatter
import kotlin.text.format

enum class MealChoice {
    COOK,
    EAT_OUT
}
@Composable
fun MealChoiceDialog(
    meal: UIMeal, // MODIFICATION: It now accepts the UIMeal object
    onDismiss: () -> Unit,
    onResult: (MealChoice) -> Unit // MODIFICATION: Returns a simple MealChoice enum
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${getMealTypeName(meal.mealType)} - ${meal.date.format((dateFormatter))}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { onResult(MealChoice.COOK) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Cook", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onResult(MealChoice.EAT_OUT) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Eat Out", color = Color.Black)
                }
            }
        }
    }
}
