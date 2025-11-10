package com.example.apiconnectapp.data.recent

import android.content.Context
import org.json.JSONArray

object RecentSearchRepository {
    private const val PREFS_NAME = "recent_searches_prefs"
    private const val KEY_RECENTS = "recents"
    private const val MAX_ITEMS = 10

    fun getRecentQueries(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RECENTS, "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { idx ->
                arr.optString(idx)?.takeIf { it.isNotBlank() }
            }
        }.getOrDefault(emptyList())
    }

    fun addRecentQuery(context: Context, query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val current = getRecentQueries(context).toMutableList()
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)
        while (current.size > MAX_ITEMS) current.removeLast()
        val arr = JSONArray()
        current.forEach { arr.put(it) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_RECENTS, arr.toString()).apply()
    }

    fun clearAll(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_RECENTS).apply()
    }
}

