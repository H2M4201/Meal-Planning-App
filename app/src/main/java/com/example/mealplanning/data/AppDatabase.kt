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
    version = 4, // Start with version 1 for the new schema
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

//        val MIGRATION = object : Migration(2, 3) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//                // 1. Add isUpdateStock to ShoppingCart (Default 0/false)
//                db.execSQL("ALTER TABLE ShoppingCart ADD COLUMN isUpdateStock INTEGER NOT NULL DEFAULT 0")
//
//                // 2. Drop lastUpdatedAmount from MealPlanDetail
//                // This requires SQLite 3.35.0+.
//                db.execSQL("ALTER TABLE MealPlanDetail DROP COLUMN lastUpdatedAmount")
//            }
//        }

        val MIGRATION = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")

                // 1. Rename IsEatOut TO Status")

                // 1. Rename IsEatOut to Status in MealPlan
                db.execSQL("ALTER TABLE MealPlan RENAME COLUMN IsEatOut TO Status")

                // 2. Add LastStockUpdated and IsConsumed to MealPlanDetail
                db.execSQL("ALTER TABLE MealPlanDetail ADD COLUMN LastCartUpdated INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE MealPlanDetail ADD COLUMN IsConsumed INTEGER NOT NULL DEFAULT 0")

                // 3. Handle ShoppingCart: remove isUpdateStock, add LastUpdatedStock
                // Note: SQLite does not support DROP COLUMN in older versions (pre-3.35.0).
                // If your app supports Android 12+, you can use DROP COLUMN.
                // Otherwise, the safest direct way without a temp table is renaming
                // if they are semantically similar, or just adding the new one.

                // Assuming you want to drop 'isUpdateStock' and add 'LastUpdatedStock':
                try {
                    db.execSQL("ALTER TABLE ShoppingCart DROP COLUMN isUpdateStock")
                } catch (e: Exception) {
                    // Fallback for older Android versions where DROP COLUMN isn't supported
                    // If this fails, the column stays but won't be used by the Room Entity
                }
                db.execSQL("ALTER TABLE ShoppingCart ADD COLUMN LastUpdatedStock INTEGER NOT NULL DEFAULT 0")
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
