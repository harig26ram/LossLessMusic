package com.example.paytag.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime GROUP BY category")
    fun getCategoryTotals(startTime: Long, endTime: Long): Flow<List<CategoryTotal>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalSpent(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime AND note LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchExpenses(startTime: Long, endTime: Long, query: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getExpensesBetweenSync(startTime: Long, endTime: Long): List<Expense>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime GROUP BY category")
    suspend fun getCategoryTotalsSync(startTime: Long, endTime: Long): List<CategoryTotal>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
