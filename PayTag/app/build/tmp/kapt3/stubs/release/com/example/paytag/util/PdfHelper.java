package com.example.paytag.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J:\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f\u00a8\u0006\u0010"}, d2 = {"Lcom/example/paytag/util/PdfHelper;", "", "()V", "generateReport", "Ljava/io/File;", "context", "Landroid/content/Context;", "expenses", "", "Lcom/example/paytag/data/Expense;", "categoryTotals", "Lcom/example/paytag/data/CategoryTotal;", "monthTotal", "", "monthLabel", "", "app_release"})
public final class PdfHelper {
    @org.jetbrains.annotations.NotNull
    public static final com.example.paytag.util.PdfHelper INSTANCE = null;
    
    private PdfHelper() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.io.File generateReport(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.util.List<com.example.paytag.data.Expense> expenses, @org.jetbrains.annotations.NotNull
    java.util.List<com.example.paytag.data.CategoryTotal> categoryTotals, double monthTotal, @org.jetbrains.annotations.NotNull
    java.lang.String monthLabel) {
        return null;
    }
}