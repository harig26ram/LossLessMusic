package com.example.paytag.data

class GroupRepository {

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        prefs = context.getSharedPreferences("paytag_prefs", android.content.Context.MODE_PRIVATE)
    }

    fun isLoggedIn(): Boolean = prefs?.getBoolean("logged_in", false) ?: false

    fun isSetupComplete(): Boolean = prefs?.getBoolean("setup_complete", false) ?: false

    fun getUserName(): String = prefs?.getString("user_name", "User") ?: "User"

    fun login(name: String) {
        prefs?.edit()?.putBoolean("logged_in", true)?.putString("user_name", name)?.apply()
    }

    fun completeSetup() {
        prefs?.edit()?.putBoolean("setup_complete", true)?.apply()
    }

    fun logout() {
        prefs?.edit()?.putBoolean("logged_in", false)?.remove("user_name")?.remove("group_id")?.remove("group_code")?.remove("group_member")?.apply()
    }

    fun resetSetup() {
        prefs?.edit()?.remove("setup_complete")?.remove("group_id")?.remove("group_code")?.remove("group_member")?.apply()
    }

    fun createGroup(): Group {
        val code = (100000..999999).random().toString()
        val userName = getUserName()
        val groupId = "group_${System.currentTimeMillis()}"

        prefs?.edit()
            ?.putString("group_id", groupId)
            ?.putString("group_code", code)
            ?.putString("group_member", userName)
            ?.putBoolean("setup_complete", true)
            ?.apply()

        return Group(groupId = groupId, code = code, memberNames = listOf(userName))
    }

    fun joinGroup(code: String): Group {
        val existingCode = prefs?.getString("group_code", "") ?: ""
        if (existingCode == code) throw Exception("You're already in this group")

        val groupId = "group_${System.currentTimeMillis()}"
        val userName = getUserName()

        prefs?.edit()
            ?.putString("group_id", groupId)
            ?.putString("group_code", code)
            ?.putString("group_member", userName)
            ?.putBoolean("setup_complete", true)
            ?.apply()

        return Group(groupId = groupId, code = code, memberNames = listOf(userName))
    }

    fun getGroup(): Group? {
        val groupId = prefs?.getString("group_id", "") ?: ""
        val code = prefs?.getString("group_code", "") ?: ""
        val member = prefs?.getString("group_member", "") ?: ""

        if (groupId.isEmpty()) return null
        return Group(groupId = groupId, code = code, memberNames = listOf(member))
    }

    fun leaveGroup() {
        prefs?.edit()?.remove("group_id")?.remove("group_code")?.remove("group_member")?.apply()
    }

    fun getThemeMode(): String = prefs?.getString("theme_mode", "dark") ?: "dark"

    fun setThemeMode(mode: String) {
        prefs?.edit()?.putString("theme_mode", mode)?.apply()
    }
}
