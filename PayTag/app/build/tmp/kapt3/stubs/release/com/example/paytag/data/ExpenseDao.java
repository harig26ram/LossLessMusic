package com.example.paytag.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0019\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH\'J\u001b\u0010\n\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ$\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\t0\b2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\fH\'J\'\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000f0\t2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0013J$\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\b2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\fH\'J\'\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00050\t2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0013J \u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00170\b2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\fH\'J\u0019\u0010\u0018\u001a\u00020\f2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J,\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\b2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\'J\u0019\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001d"}, d2 = {"Lcom/example/paytag/data/ExpenseDao;", "", "delete", "", "expense", "Lcom/example/paytag/data/Expense;", "(Lcom/example/paytag/data/Expense;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllExpenses", "Lkotlinx/coroutines/flow/Flow;", "", "getById", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCategoryTotals", "Lcom/example/paytag/data/CategoryTotal;", "startTime", "endTime", "getCategoryTotalsSync", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getExpensesBetween", "getExpensesBetweenSync", "getTotalSpent", "", "insert", "searchExpenses", "query", "", "update", "app_release"})
@androidx.room.Dao
public abstract interface ExpenseDao {
    
    @androidx.room.Insert
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull
    com.example.paytag.data.Expense expense, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull
    com.example.paytag.data.Expense expense, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull
    com.example.paytag.data.Expense expense, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM expenses ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.paytag.data.Expense>> getAllExpenses();
    
    @androidx.room.Query(value = "SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.paytag.data.Expense>> getExpensesBetween(long startTime, long endTime);
    
    @androidx.room.Query(value = "SELECT category, SUM(amount) as total FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime GROUP BY category")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.paytag.data.CategoryTotal>> getCategoryTotals(long startTime, long endTime);
    
    @androidx.room.Query(value = "SELECT SUM(amount) FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Double> getTotalSpent(long startTime, long endTime);
    
    @androidx.room.Query(value = "SELECT * FROM expenses WHERE id = :id")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getById(long id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.example.paytag.data.Expense> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime AND note LIKE \'%\' || :query || \'%\' ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.paytag.data.Expense>> searchExpenses(long startTime, long endTime, @org.jetbrains.annotations.NotNull
    java.lang.String query);
    
    @androidx.room.Query(value = "SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getExpensesBetweenSync(long startTime, long endTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.paytag.data.Expense>> $completion);
    
    @androidx.room.Query(value = "SELECT category, SUM(amount) as total FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime GROUP BY category")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getCategoryTotalsSync(long startTime, long endTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.paytag.data.CategoryTotal>> $completion);
}