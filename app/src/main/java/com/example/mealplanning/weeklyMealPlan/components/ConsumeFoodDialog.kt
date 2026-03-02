package com.example.mealplanning.weeklyMealPlan.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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

enum class ConsumeChoice {
    PARTLY,
    ALL
}

@Composable
fun ConsumeFoodDialog(
    meal: UIMeal,
    onDismiss: () -> Unit,
    onConfirm: (ConsumeChoice) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Consume Food",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${getMealTypeName(meal.mealType)} - ${meal.date.format(dateFormatter)}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = { onConfirm(ConsumeChoice.PARTLY) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0703C))
                ) {
                    Text("Partly Consume", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onConfirm(ConsumeChoice.ALL) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0703C))
                ) {
                    Text("Consume All", color = Color.Black)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        }
    }
}