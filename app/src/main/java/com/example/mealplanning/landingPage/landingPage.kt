package com.example.mealplanning.landingPage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mealplanning.R
// ADD THIS IMPORT STATEMENT
import com.example.mealplanning.shareUI.theme.MealPlanningTheme

@Composable
// Add the new navigation lambda as a parameter
fun MainScreen(
    onNavigateToWeeklyPlan: () -> Unit,
    onNavigateToUpdateStock: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToIngredientList: () -> Unit,
    onNavigateToRecipe: () -> Unit // Add new parameter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo (code is unchanged)
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 48.dp)
        )

        // Navigation Buttons
        AppButton(text = "Weekly Meal Plan", onClick = onNavigateToWeeklyPlan)
        Spacer(modifier = Modifier.height(16.dp))
        // Use the new lambda for the "Update Stock" button
        AppButton(text = "Stock", onClick = onNavigateToUpdateStock)
        Spacer(modifier = Modifier.height(16.dp))
        // Connect the new action to the button
        AppButton(text = "Shopping List", onClick = onNavigateToShoppingList)
        Spacer(modifier = Modifier.height(16.dp))

        AppButton(text = "Ingredient List", onClick = onNavigateToIngredientList)
        Spacer(modifier = Modifier.height(16.dp))

        AppButton(text = "Recipe", onClick = onNavigateToRecipe)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// I am adding the AppButton code here since it was missing from your provided file content
@Composable
fun AppButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF0703C) // Orange color for the button
        )
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MealPlanningTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF2C2C2C)
        ) {
            // Update the preview to provide an empty lambda for the new parameter
            MainScreen(
                onNavigateToWeeklyPlan = {},
                onNavigateToUpdateStock = {},
                onNavigateToShoppingList = {}, // Update preview
                onNavigateToIngredientList = {},
                onNavigateToRecipe = {}
            )
        }
    }
}
