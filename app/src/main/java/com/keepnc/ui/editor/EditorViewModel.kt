package com.keepnc.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepnc.data.local.DraftStorage
import com.keepnc.data.local.NoteDraft
import com.keepnc.data.remote.AttachmentUploadItem
import com.keepnc.data.remote.AttachmentUrlUtils
import com.keepnc.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadProgress(val current: Int, val total: Int)

/**
 * ViewModel for the note editor.
 *
 * Holds the current note data as mutable state so it survives rotation.
 * The Fragment reads from these fields when setting up the UI.
 */
@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: NotesRepository,
    private val draftStorage: DraftStorage = DraftStorage()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Loading)
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // Holds the local DB id of the note being edited; null means new note
    var noteId: Long? = null
        private set

    /** Server ID of the note if synced with Nextcloud (used for attachment URL resolution). */
    var serverId: Long? = null
        private set

    /** Tracks attachments uploaded to Nextcloud during this editing session. */
    val stagedAttachments = mutableSetOf<String>()

    /** True if a local/remote note was temporarily created solely to hold uploaded attachments. */
    var isNewNoteCreatedForAttachments: Boolean = false
        private set

    private val _uploadProgress = MutableStateFlow<UploadProgress?>(null)
    val uploadProgress: StateFlow<UploadProgress?> = _uploadProgress.asStateFlow()

    private val _uploadError = MutableSharedFlow<String>()
    val uploadError: SharedFlow<String> = _uploadError.asSharedFlow()

    // Initial snapshot of the note when loaded — used to detect unsaved changes
    private var initialTitle: String = ""
    private var initialContent: String = ""
    private var initialCategory: String = ""
    private var initialFavorite: Boolean = false

    // Mutable note fields — the Fragment reads these to populate its views
    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val category = MutableStateFlow("")
    val isFavorite = MutableStateFlow(false)

    /** Emits true when a previously unsaved draft was restored on editor open. */
    val hasRestoredDraft = MutableStateFlow(false)

    /** Live list of all distinct categories in the database. */
    val allCategories: StateFlow<List<String>> = repository.getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Call once from [EditorFragment.onViewCreated].
     * If [id] is null or -1, sets up a blank new note (or restores new note draft).
     * Otherwise, loads the existing note from Room (or restores unsaved draft for this note).
     */
    fun loadNote(id: Long?) {
        _uiState.value = EditorUiState.Loading
        stagedAttachments.clear()
        isNewNoteCreatedForAttachments = false

        if (id == null || id == -1L) {
            // New note
            noteId = null
            serverId = null
            initialTitle = ""
            initialContent = ""
            initialCategory = ""
            initialFavorite = false

            val draft = draftStorage.getDraft(null)
            if (draft != null && (draft.title.isNotBlank() || draft.content.isNotBlank())) {
                title.value = draft.title
                content.value = draft.content
                category.value = draft.category
                isFavorite.value = draft.isFavorite
                hasRestoredDraft.value = true
            } else {
                title.value = ""
                content.value = ""
                category.value = ""
                isFavorite.value = false
                hasRestoredDraft.value = false
            }
            _uiState.value = EditorUiState.Editing
            return
        }

        viewModelScope.launch {
            try {
                val note = repository.getNoteById(id)
                if (note != null) {
                    noteId = note.id
                    serverId = note.serverId

                    val titleTrimmed = note.title.trim()
                    val firstLine = note.content.substringBefore('\n').trim()
                    val shouldStripTitle = titleTrimmed.isNotBlank() && firstLine == titleTrimmed

                    val finalContent = if (shouldStripTitle) {
                        note.content.substringAfter('\n', missingDelimiterValue = "").trimStart('\r', '\n')
                    } else {
                        note.content
                    }

                    initialTitle = note.title
                    initialContent = finalContent
                    initialCategory = note.category
                    initialFavorite = note.favorite

                    val draft = draftStorage.getDraft(note.id)
                    if (draft != null && (draft.title != note.title || draft.content != finalContent ||
                                draft.category != note.category || draft.isFavorite != note.favorite)) {
                        title.value = draft.title
                        content.value = draft.content
                        category.value = draft.category
                        isFavorite.value = draft.isFavorite
                        hasRestoredDraft.value = true
                    } else {
                        title.value = note.title
                        content.value = finalContent
                        category.value = note.category
                        isFavorite.value = note.favorite
                        hasRestoredDraft.value = false
                    }

                    if (shouldStripTitle) {
                        repository.updateNote(note.id, note.title, finalContent, note.category, note.favorite)
                        repository.syncWithServer()
                    }

                    _uiState.value = EditorUiState.Editing
                } else {
                    _uiState.value = EditorUiState.Error(messageRes = com.keepnc.R.string.editor_note_not_found)
                }
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(message = e.message)
            }
        }
    }

    /** Updates the selected category for this note. */
    fun setCategory(newCategory: String) {
        category.value = newCategory.trim()
    }

    /**
     * Checks if current field values differ from the initially loaded note state.
     */
    fun hasChanges(
        currentTitle: String,
        currentContent: String,
        currentCategory: String,
        currentFavorite: Boolean
    ): Boolean {
        // If it's a new blank note and nothing was entered, no changes
        if (noteId == null &&
            currentTitle.isBlank() &&
            currentContent.isBlank() &&
            currentCategory.isBlank() &&
            !currentFavorite
        ) {
            return false
        }
        return currentTitle != initialTitle ||
                currentContent != initialContent ||
                currentCategory != initialCategory ||
                currentFavorite != initialFavorite
    }

    /**
     * Uploads multiple image attachments to Nextcloud sequentially and inserts Markdown
     * tags into the note content (at cursor position or at the end).
     */
    fun uploadAndInsertAttachments(
        items: List<AttachmentUploadItem>,
        cursorPosition: Int,
        onComplete: ((newContent: String, newCursorPosition: Int) -> Unit)? = null
    ) {
        if (items.isEmpty()) return

        viewModelScope.launch {
            try {
                // Ensure note has serverId on Nextcloud
                var currentServerId = serverId
                if (currentServerId == null) {
                    var currentId = noteId
                    if (currentId == null) {
                        currentId = repository.createNote(
                            title.value,
                            content.value,
                            category.value,
                            isFavorite.value
                        )
                        noteId = currentId
                        isNewNoteCreatedForAttachments = true
                    }
                    currentServerId = repository.ensureServerId(currentId)
                    serverId = currentServerId
                }

                val uploaded = mutableListOf<Pair<String, String>>() // (fileName, serverFilename)

                items.forEachIndexed { index, item ->
                    _uploadProgress.value = UploadProgress(current = index + 1, total = items.size)
                    val result = repository.uploadAttachment(currentServerId, item)
                    result.fold(
                        onSuccess = { serverFilename ->
                            stagedAttachments.add(serverFilename)
                            uploaded.add(item.fileName to serverFilename)
                        },
                        onFailure = { error ->
                            _uploadError.emit(error.message ?: "Upload failed")
                        }
                    )
                }

                if (uploaded.isNotEmpty()) {
                    val currentText = content.value
                    val markdownSnippet = uploaded.joinToString("\n") { (name, path) -> "![$name]($path)" }
                    val (newContent, newCursor) = AttachmentUrlUtils.insertMarkdownIntoContent(
                        currentText,
                        markdownSnippet,
                        cursorPosition
                    )
                    content.value = newContent
                    onComplete?.invoke(newContent, newCursor)
                }
            } catch (e: Exception) {
                _uploadError.emit(e.message ?: "Upload failed")
            } finally {
                _uploadProgress.value = null
            }
        }
    }

    /**
     * Cleans up attachments that were uploaded during this session but subsequently
     * deleted by the user from [contentText] before saving.
     */
    private fun cleanupRemovedStagedAttachments(contentText: String) {
        val currentImages = AttachmentUrlUtils.extractAllImageUrls(contentText)
        val removed = stagedAttachments.filter { it !in currentImages }
        val sId = serverId
        if (removed.isNotEmpty() && sId != null) {
            viewModelScope.launch {
                removed.forEach { path ->
                    repository.deleteAttachment(sId, path)
                }
            }
        }
        stagedAttachments.clear()
        isNewNoteCreatedForAttachments = false
    }

    /**
     * Deletes one or more image attachments from the note:
     * 1. Removes image markdown tags from note content.
     * 2. Calls Nextcloud DELETE endpoint if the note has a serverId.
     * 3. Removes from stagedAttachments if present.
     * 4. Updates content and persists changes to Room.
     */
    fun deleteAttachments(
        paths: List<String>,
        onComplete: ((newContent: String) -> Unit)? = null
    ) {
        if (paths.isEmpty()) return

        val currentContent = content.value
        val newContent = AttachmentUrlUtils.removeImagesFromContent(currentContent, paths.toSet())
        content.value = newContent

        val sId = serverId
        stagedAttachments.removeAll(paths.toSet())

        viewModelScope.launch {
            if (sId != null) {
                paths.forEach { path ->
                    try {
                        repository.deleteAttachment(sId, path)
                    } catch (e: Exception) {
                        // Ignore individual deletion errors
                    }
                }
            }
            val currentId = noteId
            if (currentId != null) {
                repository.updateNote(
                    currentId,
                    title.value,
                    newContent,
                    category.value,
                    isFavorite.value
                )
                initialContent = newContent
            }
            onComplete?.invoke(newContent)
        }
    }

    /**
     * Automatically saves current changes to local Room database and triggers server sync,
     * without navigating away from the editor screen.
     */
    fun autoSave(
        titleText: String,
        contentText: String,
        categoryText: String,
        favorite: Boolean
    ) {
        if (titleText.isBlank() && contentText.isBlank()) {
            return
        }

        viewModelScope.launch {
            try {
                val currentId = noteId
                val savedId = if (currentId == null) {
                    val newId = repository.createNote(titleText, contentText, categoryText, favorite)
                    noteId = newId
                    newId
                } else {
                    repository.updateNote(currentId, titleText, contentText, categoryText, favorite)
                    currentId
                }
                initialTitle = titleText
                initialContent = contentText
                initialCategory = categoryText
                initialFavorite = favorite
                hasRestoredDraft.value = false

                cleanupRemovedStagedAttachments(contentText)

                draftStorage.clearDraft(currentId)
                draftStorage.clearDraft(savedId)
                draftStorage.clearDraft(null)

                repository.syncWithServer()
            } catch (e: Exception) {
                // Room write succeeded; ignore transient network sync errors during auto-save
            }
        }
    }

    /**
     * Persists an unsaved draft to [DraftStorage] without touching Room.
     */
    fun saveDraft(
        titleText: String,
        contentText: String,
        categoryText: String,
        favorite: Boolean
    ) {
        if (titleText.isBlank() && contentText.isBlank()) {
            draftStorage.clearDraft(noteId)
            return
        }
        draftStorage.saveDraft(
            noteId,
            NoteDraft(
                title = titleText,
                content = contentText,
                category = categoryText,
                isFavorite = favorite
            )
        )
    }

    /**
     * Discards changes made in this session:
     * Deletes any attachments uploaded to Nextcloud that were not saved,
     * and deletes temporary note if created purely for attachments.
     */
    fun discardChanges() {
        val sId = serverId
        val nId = noteId
        if (stagedAttachments.isNotEmpty() && sId != null) {
            val toDelete = stagedAttachments.toSet()
            stagedAttachments.clear()
            viewModelScope.launch {
                toDelete.forEach { path ->
                    repository.deleteAttachment(sId, path)
                }
            }
        }
        if (isNewNoteCreatedForAttachments && nId != null) {
            isNewNoteCreatedForAttachments = false
            viewModelScope.launch {
                repository.deleteNote(nId)
            }
        }
        draftStorage.clearDraft(noteId)
        draftStorage.clearDraft(null)
    }

    /**
     * Discards the restored draft and reverts fields back to the initial Room note state.
     */
    fun discardDraft() {
        discardChanges()
        hasRestoredDraft.value = false
        title.value = initialTitle
        content.value = initialContent
        category.value = initialCategory
        isFavorite.value = initialFavorite
    }

    /**
     * Saves the note (create or update), immediately launches server sync,
     * and transitions to [EditorUiState.Saved].
     */
    fun saveNote(
        titleText: String,
        contentText: String,
        categoryText: String,
        favorite: Boolean
    ) {
        // Don't save completely empty new notes
        if (titleText.isBlank() && contentText.isBlank()) {
            discardChanges()
            _uiState.value = EditorUiState.Saved
            return
        }

        viewModelScope.launch {
            _uiState.value = EditorUiState.Saving
            try {
                val currentId = noteId
                if (currentId == null) {
                    repository.createNote(titleText, contentText, categoryText, favorite)
                } else {
                    repository.updateNote(currentId, titleText, contentText, categoryText, favorite)
                }
                cleanupRemovedStagedAttachments(contentText)
                draftStorage.clearDraft(currentId)
                draftStorage.clearDraft(null)
                // Immediately sync with the server after saving
                repository.syncWithServer()
                _uiState.value = EditorUiState.Saved
            } catch (e: Exception) {
                cleanupRemovedStagedAttachments(contentText)
                draftStorage.clearDraft(noteId)
                draftStorage.clearDraft(null)
                _uiState.value = EditorUiState.Saved
            }
        }
    }

    /**
     * Deletes the note (if already saved), launches server sync,
     * and transitions to [EditorUiState.Saved] to close the screen.
     */
    fun deleteNote() {
        val currentId = noteId
        stagedAttachments.clear()
        isNewNoteCreatedForAttachments = false
        draftStorage.clearDraft(currentId)
        draftStorage.clearDraft(null)

        if (currentId == null) {
            // Unsaved new note — simply exit
            _uiState.value = EditorUiState.Saved
            return
        }

        viewModelScope.launch {
            _uiState.value = EditorUiState.Saving
            try {
                repository.deleteNote(currentId)
                repository.syncWithServer()
                _uiState.value = EditorUiState.Saved
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Saved
            }
        }
    }

    fun toggleFavorite() {
        isFavorite.value = !isFavorite.value
    }
}
