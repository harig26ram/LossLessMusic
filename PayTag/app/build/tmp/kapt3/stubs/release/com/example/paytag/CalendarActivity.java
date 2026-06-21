package com.example.paytag;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\'\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0002\u00a2\u0006\u0002\u0010\u0016J\u0010\u0010\u0017\u001a\u00020\u00112\u0006\u0010\u0018\u001a\u00020\u0011H\u0002J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\u0012\u0010\u001b\u001a\u00020\u001a2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0005\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\b\u001a\u00020\t8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\f\u0010\r\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u001e"}, d2 = {"Lcom/example/paytag/CalendarActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/example/paytag/databinding/ActivityCalendarBinding;", "currentCal", "Ljava/util/Calendar;", "kotlin.jvm.PlatformType", "db", "Lcom/example/paytag/data/AppDatabase;", "getDb", "()Lcom/example/paytag/data/AppDatabase;", "db$delegate", "Lkotlin/Lazy;", "createDayView", "Landroid/view/View;", "day", "", "amount", "", "isToday", "", "(ILjava/lang/Double;Z)Landroid/view/View;", "dpToPx", "dp", "loadCalendar", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "app_release"})
public final class CalendarActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.example.paytag.databinding.ActivityCalendarBinding binding;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy db$delegate = null;
    private java.util.Calendar currentCal;
    
    public CalendarActivity() {
        super();
    }
    
    private final com.example.paytag.data.AppDatabase getDb() {
        return null;
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void loadCalendar() {
    }
    
    private final android.view.View createDayView(int day, java.lang.Double amount, boolean isToday) {
        return null;
    }
    
    private final int dpToPx(int dp) {
        return 0;
    }
}