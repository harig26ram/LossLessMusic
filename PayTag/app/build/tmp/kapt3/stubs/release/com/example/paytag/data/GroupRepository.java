package com.example.paytag.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000b\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006J\u0006\u0010\u0007\u001a\u00020\bJ\b\u0010\t\u001a\u0004\u0018\u00010\bJ\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u000bJ\u000e\u0010\r\u001a\u00020\u00062\u0006\u0010\u000e\u001a\u00020\u000fJ\u0006\u0010\u0010\u001a\u00020\u0011J\u0006\u0010\u0012\u001a\u00020\u0011J\u000e\u0010\u0013\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u000bJ\u0006\u0010\u0015\u001a\u00020\u0006J\u000e\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u000bJ\u0006\u0010\u0018\u001a\u00020\u0006J\u0006\u0010\u0019\u001a\u00020\u0006J\u000e\u0010\u001a\u001a\u00020\u00062\u0006\u0010\u001b\u001a\u00020\u000bR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/example/paytag/data/GroupRepository;", "", "()V", "prefs", "Landroid/content/SharedPreferences;", "completeSetup", "", "createGroup", "Lcom/example/paytag/data/Group;", "getGroup", "getThemeMode", "", "getUserName", "init", "context", "Landroid/content/Context;", "isLoggedIn", "", "isSetupComplete", "joinGroup", "code", "leaveGroup", "login", "name", "logout", "resetSetup", "setThemeMode", "mode", "app_release"})
public final class GroupRepository {
    @org.jetbrains.annotations.Nullable
    private android.content.SharedPreferences prefs;
    
    public GroupRepository() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    public final boolean isLoggedIn() {
        return false;
    }
    
    public final boolean isSetupComplete() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUserName() {
        return null;
    }
    
    public final void login(@org.jetbrains.annotations.NotNull
    java.lang.String name) {
    }
    
    public final void completeSetup() {
    }
    
    public final void logout() {
    }
    
    public final void resetSetup() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.paytag.data.Group createGroup() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.paytag.data.Group joinGroup(@org.jetbrains.annotations.NotNull
    java.lang.String code) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.example.paytag.data.Group getGroup() {
        return null;
    }
    
    public final void leaveGroup() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getThemeMode() {
        return null;
    }
    
    public final void setThemeMode(@org.jetbrains.annotations.NotNull
    java.lang.String mode) {
    }
}