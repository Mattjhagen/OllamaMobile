package com.ollamamobile.app.manager

import android.content.Context

class FirstLaunchManager(context: Context) {
    private val prefs = context.getSharedPreferences("first_launch_prefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean("is_first_launch", false).apply()
    }
}
