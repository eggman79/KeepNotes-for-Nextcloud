package com.keepnc.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data model for a locally persisted note draft.
 */
data class NoteDraft(
    val title: String,
    val content: String,
    val category: String,
    val isFavorite: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Persists unsaved note drafts to prevent data loss when the app process is terminated
 * by OS battery optimizers or low-memory killers while the user is editing or previewing a note.
 */
@Singleton
open class DraftStorage {
    private val context: Context?
    private val injectedPrefs: SharedPreferences?

    @Inject
    constructor(@ApplicationContext context: Context) {
        this.context = context
        this.injectedPrefs = null
    }

    /** Constructor for unit testing without Android Context */
    constructor(sharedPreferences: SharedPreferences? = null) {
        this.context = null
        this.injectedPrefs = sharedPreferences
    }

    private val prefs: SharedPreferences by lazy {
        injectedPrefs ?: run {
            val ctx = checkNotNull(context) { "Context is required for SharedPreferences access" }
            ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun draftKey(noteId: Long?): String =
        if (noteId == null || noteId == -1L) KEY_NEW_NOTE else "draft_$noteId"

    open fun saveDraft(noteId: Long?, draft: NoteDraft) {
        val key = draftKey(noteId)
        prefs.edit()
            .putString("${key}_title", draft.title)
            .putString("${key}_content", draft.content)
            .putString("${key}_category", draft.category)
            .putBoolean("${key}_favorite", draft.isFavorite)
            .putLong("${key}_timestamp", draft.timestamp)
            .apply()
    }

    open fun getDraft(noteId: Long?): NoteDraft? {
        val key = draftKey(noteId)
        val content = prefs.getString("${key}_content", null) ?: return null
        val title = prefs.getString("${key}_title", "") ?: ""
        val category = prefs.getString("${key}_category", "") ?: ""
        val favorite = prefs.getBoolean("${key}_favorite", false)
        val timestamp = prefs.getLong("${key}_timestamp", 0L)
        return NoteDraft(
            title = title,
            content = content,
            category = category,
            isFavorite = favorite,
            timestamp = timestamp
        )
    }

    open fun clearDraft(noteId: Long?) {
        val key = draftKey(noteId)
        prefs.edit()
            .remove("${key}_title")
            .remove("${key}_content")
            .remove("${key}_category")
            .remove("${key}_favorite")
            .remove("${key}_timestamp")
            .apply()
    }

    open fun hasDraft(noteId: Long?): Boolean {
        val key = draftKey(noteId)
        return prefs.contains("${key}_content")
    }

    companion object {
        private const val PREFS_NAME = "keepnc_editor_drafts"
        private const val KEY_NEW_NOTE = "draft_new_note"
    }
}
