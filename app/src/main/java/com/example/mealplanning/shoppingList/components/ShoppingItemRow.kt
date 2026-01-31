package com.example.mealplanning.shoppingList.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.shoppingList.data.ShoppingCart

@Composable
fun ShoppingItemRow(ingredient: Ingredient, shoppingCart: ShoppingCart, onEdit: () -> Unit, onDelete: () -> Unit, isBought: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1.5f),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFFF0703C).copy(alpha = 0.9f)
        ) {
            Text(
                text = ingredient.Name,
                modifier = Modifier.padding(16.dp),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            modifier = Modifier.weight(0.5f),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFFF0703C).copy(alpha = 0.9f)
        ) {
            Text(
                text = shoppingCart.Amount.toString(),
                modifier = Modifier.padding(16.dp),
                color = Color.Black
            )
        }

        IconButton(
            onClick = onEdit,
            enabled = !isBought,
            modifier = Modifier.background(
                if (isBought) Color.Gray else Color.White,
                MaterialTheme.shapes.small
            )
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = Color.Black)
        }
        IconButton(
            onClick = onDelete,
            enabled = !isBought,
            modifier = Modifier.background(
                if (isBought) Color.DarkGray else Color(0xFFFF5252),
                MaterialTheme.shapes.small
            )
        ) {
            Icon(Icons.Default.Close, contentDescription = "Delete Item", tint = Color.White)
        }
    }
}