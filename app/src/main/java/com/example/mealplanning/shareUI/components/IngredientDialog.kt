package com.example.mealplanning.shareUI.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.ingredientList.ViewModel.IngredientListViewModel

@Composable
fun IngredientDialog(
    ingredient: Ingredient? = null,
    initialAmount: String = "",
    ingredientListVm: IngredientListViewModel,
    isFromCookDialog: Boolean = false,
    isMasterIngredient: Boolean = false, // TASK 1: New parameter to identify caller
    onDismiss: () -> Unit,
    onSave: (name: String, amount: String, unit: String) -> Unit
) {
    var itemName by remember { mutableStateOf(ingredient?.Name ?: "") }
    var itemAmount by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf(ingredient?.Unit ?: "") }
    var showSearchDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TASK 1: Only show search if called from CookDialog
                if (isFromCookDialog) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Search saved ingredient") },
                        shape = RoundedCornerShape(50),
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSearchDialog = true },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // TASK 1: Only show manual Name entry if NOT from CookDialog
                if (!isFromCookDialog) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Enter Item name") },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else if (itemName.isNotBlank()) {
                    // Show selection feedback in CookDialog
                    Text(
                        text = "Selected: $itemName",
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (!isMasterIngredient) {
                    OutlinedTextField(
                        value = itemAmount,
                        onValueChange = {
                            if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                itemAmount = it
                            }
                        },
                        label = { Text("Enter amount") },
                        shape = RoundedCornerShape(50),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = itemUnit,
                    onValueChange = { itemUnit = it },
                    label = { Text("Enter unit") },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (itemName.isNotBlank()) {
                                if (!isMasterIngredient) {
                                    onSave(itemName, itemAmount, itemUnit)
                                }
                                onSave(itemName, "", itemUnit)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Save", color = Color.Black)
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }

        if (showSearchDialog) {
            IngredientSearchDialog(
                ingredientListVm = ingredientListVm,
                onDismiss = { showSearchDialog = false },
                onIngredientSelected = { selectedIngredient ->
                    itemName = selectedIngredient.Name
                    itemUnit = selectedIngredient.Unit
                    showSearchDialog = false
                }
            )
        }
    }
}

@Composable
fun IngredientSearchDialog(
    ingredientListVm: IngredientListViewModel,
    onDismiss: () -> Unit,
    onIngredientSelected: (Ingredient) -> Unit
) {
    val masterIngredients by ingredientListVm.masterIngredients.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.heightIn(max = 500.dp)) {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(masterIngredients) { ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIngredientSelected(ingredient) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = ingredient.Name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(text = ingredient.Unit, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
