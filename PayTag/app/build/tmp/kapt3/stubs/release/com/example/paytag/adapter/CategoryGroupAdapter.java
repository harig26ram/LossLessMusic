package com.example.paytag.adapter;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0002\u001a\u001bB-\u0012\u0012\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004\u0012\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004\u00a2\u0006\u0002\u0010\bJ\b\u0010\u0010\u001a\u00020\fH\u0016J\u001c\u0010\u0011\u001a\u00020\u00062\n\u0010\u0012\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0013\u001a\u00020\fH\u0016J\u001c\u0010\u0014\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\fH\u0016J\u0014\u0010\u0018\u001a\u00020\u00062\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00050\u000eR\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\f0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/example/paytag/adapter/CategoryGroupAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/example/paytag/adapter/CategoryGroupAdapter$CategoryGroupViewHolder;", "onEdit", "Lkotlin/Function1;", "Lcom/example/paytag/data/Expense;", "", "onDelete", "(Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "categoryColors", "", "", "", "groups", "", "Lcom/example/paytag/adapter/CategoryGroupAdapter$CategoryGroup;", "getItemCount", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "submitList", "expenses", "CategoryGroup", "CategoryGroupViewHolder", "app_release"})
public final class CategoryGroupAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.example.paytag.adapter.CategoryGroupAdapter.CategoryGroupViewHolder> {
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<com.example.paytag.data.Expense, kotlin.Unit> onEdit = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<com.example.paytag.data.Expense, kotlin.Unit> onDelete = null;
    @org.jetbrains.annotations.NotNull
    private java.util.List<com.example.paytag.adapter.CategoryGroupAdapter.CategoryGroup> groups;
    @org.jetbrains.annotations.NotNull
    private final java.util.Map<java.lang.String, java.lang.Integer> categoryColors = null;
    
    public CategoryGroupAdapter(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.paytag.data.Expense, kotlin.Unit> onEdit, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.paytag.data.Expense, kotlin.Unit> onDelete) {
        super();
    }
    
    public final void submitList(@org.jetbrains.annotations.NotNull
    java.util.List<com.example.paytag.data.Expense> expenses) {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public com.example.paytag.adapter.CategoryGroupAdapter.CategoryGroupViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull
    com.example.paytag.adapter.CategoryGroupAdapter.CategoryGroupViewHolder holder, int position) {
    }
    
    @java.lang.Override
    public int getItemCount() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0005H\u00c6\u0003J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00c6\u0003J-\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001J\t\u0010\u0019\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001a"}, d2 = {"Lcom/example/paytag/adapter/CategoryGroupAdapter$CategoryGroup;", "", "category", "", "total", "", "expenses", "", "Lcom/example/paytag/data/Expense;", "(Ljava/lang/String;DLjava/util/List;)V", "getCategory", "()Ljava/lang/String;", "getExpenses", "()Ljava/util/List;", "getTotal", "()D", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_release"})
    public static final class CategoryGroup {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String category = null;
        private final double total = 0.0;
        @org.jetbrains.annotations.NotNull
        private final java.util.List<com.example.paytag.data.Expense> expenses = null;
        
        public CategoryGroup(@org.jetbrains.annotations.NotNull
        java.lang.String category, double total, @org.jetbrains.annotations.NotNull
        java.util.List<com.example.paytag.data.Expense> expenses) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getCategory() {
            return null;
        }
        
        public final double getTotal() {
            return 0.0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.example.paytag.data.Expense> getExpenses() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        public final double component2() {
            return 0.0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.example.paytag.data.Expense> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.example.paytag.adapter.CategoryGroupAdapter.CategoryGroup copy(@org.jetbrains.annotations.NotNull
        java.lang.String category, double total, @org.jetbrains.annotations.NotNull
        java.util.List<com.example.paytag.data.Expense> expenses) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0005\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/example/paytag/adapter/CategoryGroupAdapter$CategoryGroupViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Lcom/example/paytag/adapter/CategoryGroupAdapter;Landroid/view/View;)V", "categoryDot", "categoryName", "Landroid/widget/TextView;", "categoryTotal", "expensesRecycler", "Landroidx/recyclerview/widget/RecyclerView;", "bind", "", "group", "Lcom/example/paytag/adapter/CategoryGroupAdapter$CategoryGroup;", "app_release"})
    public final class CategoryGroupViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull
        private final android.view.View categoryDot = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView categoryName = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView categoryTotal = null;
        @org.jetbrains.annotations.NotNull
        private final androidx.recyclerview.widget.RecyclerView expensesRecycler = null;
        
        public CategoryGroupViewHolder(@org.jetbrains.annotations.NotNull
        android.view.View itemView) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull
        com.example.paytag.adapter.CategoryGroupAdapter.CategoryGroup group) {
        }
    }
}