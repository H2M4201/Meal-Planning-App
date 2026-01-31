package com.example.mealplanning.shareUI.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IngredientRowTemplate(
    name: String,
    valueDisplay: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    nameWidth: androidx.compose.ui.unit.Dp = 145.dp,
    valueWidth: androidx.compose.ui.unit.Dp = 75.dp,
    isReadOnly: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.width(nameWidth),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0703C)
        ) {
            Text(
                text = name,
                modifier = Modifier.padding(8.dp),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }

        Surface(
            modifier = Modifier.width(valueWidth),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0703C)
        ) {
            Text(
                text = valueDisplay,
                modifier = Modifier.padding(8.dp),
                color = Color.Black,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isReadOnly) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp).background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Edit, "Edit", tint = Color.Black, modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).background(Color(0xFFFF5252), shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Close, "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}