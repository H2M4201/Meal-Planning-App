package com.example.mealplanning.stock.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealplanning.stock.data.Stock
import com.example.mealplanning.stock.data.StockDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockViewModel(private val stockDao: StockDao) : ViewModel() {

    val stockItems: StateFlow<List<Stock>> = stockDao.getAllStock()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addStockItems(newStockItems: List<Stock>) {
        viewModelScope.launch {
            // A more robust implementation would consolidate amounts.
            // For now, we insert all. The OnConflictStrategy will handle replacement.
            newStockItems.forEach { stockItem ->
                stockDao.insert(stockItem)
            }
        }
    }

    fun removeStockItem(stock: Stock) {
        viewModelScope.launch {
            stockDao.delete(stock)
        }
    }

    fun updateStockItem(stock: Stock) {
        viewModelScope.launch {
            stockDao.insert(stock)
        }
    }
}

class StockViewModelFactory(private val stockDao: StockDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockViewModel(stockDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
