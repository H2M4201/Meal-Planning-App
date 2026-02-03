package com.example.mealplanning.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mealplanning.ingredientList.data.Ingredient
import com.example.mealplanning.ingredientList.data.IngredientDao
import com.example.mealplanning.recipe.data.Recipe
import com.example.mealplanning.recipe.data.RecipeDao
import com.example.mealplanning.recipe.data.RecipeDetail
import com.example.mealplanning.shoppingList.data.ShoppingCart
import com.example.mealplanning.shoppingList.data.ShoppingCartDao
import com.example.mealplanning.stock.data.StockDao
import com.example.mealplanning.weeklyMealPlan.data.MealPlan
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDao
import com.example.mealplanning.weeklyMealPlan.data.MealPlanDetail
import com.example.mealplanning.stock.data.Stock

// MODIFICATION: Add all new entities to this array
@Database(
    entities = [
        Ingredient::class,
        Stock::class,
        ShoppingCart::class,
        MealPlan::class,
        MealPlanDetail::class,
        Recipe::class,
        RecipeDetail::class
    ],
    version = 6, // Start with version 1 for the new schema
    exportSchema = true
)
@TypeConverters(Converters::class) // This handles the LocalDate conversion
abstract class AppDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao
    abstract fun stockDao(): StockDao
    abstract fun shoppingCartDao(): ShoppingCartDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun recipeDao(): RecipeDao

    companion object {

        val MIGRATION = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    ALTER TABLE `Recipe` ADD COLUMN `isActive` INTEGER NOT NULL DEFAULT 1
                """)

                // Create RecipeDetail Table with Foreign Keys
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `RecipeDetail` (
                        `RecipeID` INTEGER NOT NULL, 
                        `IngredientID` INTEGER NOT NULL, 
                        `Amount` INTEGER NOT NULL, 
                        PRIMARY KEY(`RecipeID`, `IngredientID`), 
                        FOREIGN KEY(`RecipeID`) REFERENCES `Recipe`(`ID`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`IngredientID`) REFERENCES `ingredients`(`ID`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meal_planning_database.db"
                )
                    // Use .fallbackToDestructiveMigration() for now during development
                    // This will wipe the database on schema changes.
                    // Replace with proper migrations for production.
                    .fallbackToDestructiveMigration(false)
                    .addMigrations(MIGRATION)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
