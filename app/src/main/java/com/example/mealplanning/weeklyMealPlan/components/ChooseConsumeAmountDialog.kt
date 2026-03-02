package com.example.mealplanning.weeklyMealPlan.components

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.shareUI.components.IngredientDialog
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDetail

@Composable
fun ChooseConsumeAmountDialog(
    recipeIngredients: List<MealPlanDetail>,
    masterIngredients: List<Ingredient>,
    ingredientListVm: IngredientListViewModel,
    onDismiss: () -> Unit,
    onConfirm: (List<MealPlanDetail>) -> Unit
) {
    val consumeDetails = remember { mutableStateListOf(*recipeIngredients.toTypedArray()) }
    var editingDetail by remember { mutableStateOf<MealPlanDetail?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Amount to Consume",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(consumeDetails) { detail ->
                        val ingredient = masterIngredients.find { it.ID == detail.IngredientID }
                        if (ingredient != null) {
                            // MODIFICATION: Layout matches IngredientRow but only shows Edit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0703C)
                                ) {
                                    Text(
                                        text = ingredient.Name,
                                        modifier = Modifier.padding(12.dp),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Surface(
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0703C)
                                ) {
                                    Text(
                                        text = "${detail.Amount}${ingredient.Unit}",
                                        modifier = Modifier.padding(12.dp),
                                        color = Color.Black,
                                        fontSize = 14.sp
                                    )
                                }

                                IconButton(
                                    onClick = { editingDetail = detail },
                                    modifier = Modifier
                                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                                        .size(40.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Amount", tint = Color.Black)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onConfirm(consumeDetails.toList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0703C))
                    ) {
                        Text("Confirm", color = Color.Black)
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                }
            }
        }
    }

    // Logic to handle editing the specific amount to consume
    editingDetail?.let { detailToEdit ->
        val masterIngredient = masterIngredients.find { it.ID == detailToEdit.IngredientID }
        if (masterIngredient != null) {
            IngredientDialog(
                ingredient = masterIngredient,
                initialAmount = detailToEdit.Amount.toString(),
                ingredientListVm = ingredientListVm, // This would ideally be passed in
                onDismiss = { editingDetail = null },
                onSave = { _, newAmount, _ ->
                    val parsedAmount = newAmount.toIntOrNull()
                    if (parsedAmount != null) {
                        val index = consumeDetails.indexOf(detailToEdit)
                        if (index != -1) {
                            consumeDetails[index] = detailToEdit.copy(Amount = parsedAmount)
                        }
                    }
                    editingDetail = null
                }
            )
        }
    }
}