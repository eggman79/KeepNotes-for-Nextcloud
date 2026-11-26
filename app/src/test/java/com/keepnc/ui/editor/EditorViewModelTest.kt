package com.keepnc.ui.editor

import com.keepnc.data.auth.TokenStorage
import com.keepnc.data.local.DraftStorage
import com.keepnc.data.local.NoteDao
import com.keepnc.data.local.NoteDraft
import com.keepnc.data.local.NoteEntity
import com.keepnc.data.local.SyncStatus
import com.keepnc.data.remote.AttachmentUploadItem
import com.keepnc.data.remote.NotesApi
import com.keepnc.data.remote.dto.AttachmentDto
import com.keepnc.data.remote.dto.NoteCreateRequest
import com.keepnc.data.remote.dto.NoteDto
import com.keepnc.data.remote.dto.NoteUpdateRequest
import com.keepnc.data.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MultipartBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeNoteDao
    private lateinit var fakeApi: FakeNotesApi
    private lateinit var fakeDraftStorage: FakeDraftStorage
    private lateinit var repository: NotesRepository
    private lateinit var viewModel: EditorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeNoteDao()
        fakeApi = FakeNotesApi()
        fakeDraftStorage = FakeDraftStorage()

        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null)
        val allocateMethod = unsafeClass.getMethod("allocateInstance", Class::class.java)
        val dummyTokenStorage = allocateMethod.invoke(unsafe, TokenStorage::class.java) as TokenStorage

        repository = NotesRepository(fakeDao, fakeApi, dummyTokenStorage).apply {
            ioDispatcher = testDispatcher
        }
        viewModel = EditorViewModel(repository, fakeDraftStorage)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNote with null loads empty new note`() = runTest {
        viewModel.loadNote(null)
        advanceUntilIdle()

        assertEquals("", viewModel.title.value)
        assertEquals("", viewModel.content.value)
        assertEquals("", viewModel.category.value)
        assertFalse(viewModel.isFavorite.value)
        assertEquals(EditorUiState.Editing, viewModel.uiState.value)
    }

    @Test
    fun `loadNote loads note from repository`() = runTest {
        val note = NoteEntity(id = 1L, serverId = 100L, title = "Title", content = "Content", category = "Work", favorite = true)
        fakeDao.notes[1L] = note

        viewModel.loadNote(1L)
        advanceUntilIdle()

        assertEquals("Title", viewModel.title.value)
        assertEquals("Content", viewModel.content.value)
        assertEquals("Work", viewModel.category.value)
        assertTrue(viewModel.isFavorite.value)
        assertEquals(EditorUiState.Editing, viewModel.uiState.value)
    }

    @Test
    fun `loadNote strips duplicate first line title`() = runTest {
        val note = NoteEntity(id = 2L, title = "My Title", content = "My Title\nActual body", category = "", favorite = false)
        fakeDao.notes[2L] = note

        viewModel.loadNote(2L)
        advanceUntilIdle()

        assertEquals("My Title", viewModel.title.value)
        assertEquals("Actual body", viewModel.content.value)
    }

    @Test
    fun `loadNote with draft restores draft if different from note`() = runTest {
        val note = NoteEntity(id = 3L, title = "Old Title", content = "Old Content")
        fakeDao.notes[3L] = note
        fakeDraftStorage.saveDraft(
            3L,
            NoteDraft(title = "Draft Title", content = "Draft Content", category = "Draft Cat", isFavorite = true)
        )

        viewModel.loadNote(3L)
        advanceUntilIdle()

        assertEquals("Draft Title", viewModel.title.value)
        assertEquals("Draft Content", viewModel.content.value)
        assertEquals("Draft Cat", viewModel.category.value)
        assertTrue(viewModel.isFavorite.value)
        assertTrue(viewModel.hasRestoredDraft.value)
    }

    @Test
    fun `discardDraft reverts to initial note values`() = runTest {
        val note = NoteEntity(id = 3L, title = "Original Title", content = "Original Content", category = "Cat", favorite = false)
        fakeDao.notes[3L] = note
        fakeDraftStorage.saveDraft(
            3L,
            NoteDraft(title = "Draft Title", content = "Draft Content", category = "Draft Cat", isFavorite = true)
        )

        viewModel.loadNote(3L)
        advanceUntilIdle()

        viewModel.discardDraft()

        assertEquals("Original Title", viewModel.title.value)
        assertEquals("Original Content", viewModel.content.value)
        assertEquals("Cat", viewModel.category.value)
        assertFalse(viewModel.isFavorite.value)
        assertFalse(viewModel.hasRestoredDraft.value)
        assertFalse(fakeDraftStorage.hasDraft(3L))
    }

    @Test
    fun `autoSave updates existing note and clears drafts`() = runTest {
        val note = NoteEntity(id = 4L, title = "Title", content = "Content")
        fakeDao.notes[4L] = note

        viewModel.loadNote(4L)
        advanceUntilIdle()

        viewModel.autoSave("New Title", "New Content", "New Cat", true)
        advanceUntilIdle()

        val updated = fakeDao.notes[4L]
        assertEquals("New Title", updated?.title)
        assertEquals("New Content", updated?.content)
        assertEquals("New Cat", updated?.category)
        assertTrue(updated?.favorite == true)
        assertFalse(fakeDraftStorage.hasDraft(4L))
    }

    @Test
    fun `saveNote for new note creates note in repository`() = runTest {
        viewModel.loadNote(null)
        advanceUntilIdle()

        viewModel.saveNote("Created Title", "Created Content", "", false)
        advanceUntilIdle()

        assertEquals(1, fakeDao.notes.size)
        val created = fakeDao.notes.values.first()
        assertEquals("Created Title", created.title)
        assertEquals("Created Content", created.content)
        assertEquals(EditorUiState.Saved, viewModel.uiState.value)
    }

    @Test
    fun `saveNote with empty fields does not create note`() = runTest {
        viewModel.loadNote(null)
        advanceUntilIdle()

        viewModel.saveNote("", "", "", false)
        advanceUntilIdle()

        assertEquals(0, fakeDao.notes.size)
        assertEquals(EditorUiState.Saved, viewModel.uiState.value)
    }

    @Test
    fun `saveDraft persists draft in DraftStorage`() = runTest {
        val note = NoteEntity(id = 5L, title = "Title", content = "Content")
        fakeDao.notes[5L] = note

        viewModel.loadNote(5L)
        advanceUntilIdle()

        viewModel.saveDraft("Draft Title", "Draft Content", "Cat", true)

        val draft = fakeDraftStorage.getDraft(5L)
        assertEquals("Draft Title", draft?.title)
        assertEquals("Draft Content", draft?.content)
        assertEquals("Cat", draft?.category)
        assertTrue(draft?.isFavorite == true)
    }

    @Test
    fun `saveNote clears draft on success`() = runTest {
        val note = NoteEntity(id = 5L, title = "Title", content = "Content")
        fakeDao.notes[5L] = note
        fakeDraftStorage.saveDraft(
            5L,
            NoteDraft(title = "Draft", content = "Draft", category = "", isFavorite = false)
        )

        viewModel.loadNote(5L)
        advanceUntilIdle()

        viewModel.saveNote("Title", "Updated", "", false)
        advanceUntilIdle()

        assertFalse(fakeDraftStorage.hasDraft(5L))
        assertEquals(EditorUiState.Saved, viewModel.uiState.value)
    }

    @Test
    fun `deleteNote clears draft and transitions to Saved state`() = runTest {
        val note = NoteEntity(id = 5L, title = "Title", content = "Content")
        fakeDao.notes[5L] = note
        fakeDraftStorage.saveDraft(
            5L,
            NoteDraft(title = "Draft", content = "Draft", category = "", isFavorite = false)
        )

        viewModel.loadNote(5L)
        advanceUntilIdle()

        viewModel.deleteNote()
        advanceUntilIdle()

        assertFalse(fakeDraftStorage.hasDraft(5L))
        assertEquals(EditorUiState.Saved, viewModel.uiState.value)
    }

    @Test
    fun `uploadAndInsertAttachments uploads multiple items and inserts markdown at cursor position`() = runTest {
        val note = NoteEntity(id = 10L, serverId = 100L, title = "Test", content = "Hello world")
        fakeDao.notes[10L] = note

        viewModel.loadNote(10L)
        advanceUntilIdle()

        val items = listOf(
            AttachmentUploadItem("photo1.jpg", "image/jpeg", byteArrayOf(1, 2)),
            AttachmentUploadItem("photo2.png", "image/png", byteArrayOf(3, 4))
        )

        // Insert at cursor 5 (between "Hello" and " world")
        var callbackInvoked = false
        viewModel.uploadAndInsertAttachments(items, cursorPosition = 5) { newContent, newCursor ->
            callbackInvoked = true
            assertTrue(newContent.contains("![photo1.jpg](.attachments.100/photo1.jpg)"))
            assertTrue(newContent.contains("![photo2.png](.attachments.100/photo2.png)"))
        }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
        assertEquals(2, fakeApi.uploadedAttachments.size)
        assertEquals(2, viewModel.stagedAttachments.size)
        assertTrue(viewModel.content.value.startsWith("Hello\n\n![photo1.jpg]"))
    }

    @Test
    fun `uploadAndInsertAttachments appends at end when cursor position is negative`() = runTest {
        val note = NoteEntity(id = 11L, serverId = 101L, title = "Test", content = "Initial line")
        fakeDao.notes[11L] = note

        viewModel.loadNote(11L)
        advanceUntilIdle()

        val items = listOf(
            AttachmentUploadItem("img.jpg", "image/jpeg", byteArrayOf(1))
        )

        viewModel.uploadAndInsertAttachments(items, cursorPosition = -1)
        advanceUntilIdle()

        assertEquals("Initial line\n\n![img.jpg](.attachments.101/img.jpg)", viewModel.content.value)
    }

    @Test
    fun `autoSave deletes staged attachments that user removed from content`() = runTest {
        val note = NoteEntity(id = 12L, serverId = 102L, title = "Test", content = "Text")
        fakeDao.notes[12L] = note

        viewModel.loadNote(12L)
        advanceUntilIdle()

        val items = listOf(
            AttachmentUploadItem("kept.jpg", "image/jpeg", byteArrayOf(1)),
            AttachmentUploadItem("deleted.jpg", "image/jpeg", byteArrayOf(2))
        )

        viewModel.uploadAndInsertAttachments(items, cursorPosition = -1)
        advanceUntilIdle()

        assertEquals(2, viewModel.stagedAttachments.size)

        // User edits content and removes "deleted.jpg"
        val modifiedContent = "Text\n\n![kept.jpg](.attachments.102/kept.jpg)"
        viewModel.autoSave("Test", modifiedContent, "", false)
        advanceUntilIdle()

        // "deleted.jpg" should be deleted from server
        assertEquals(1, fakeApi.deletedAttachments.size)
        assertEquals(102L to ".attachments.102/deleted.jpg", fakeApi.deletedAttachments[0])
        assertEquals(0, viewModel.stagedAttachments.size)
    }

    @Test
    fun `discardChanges deletes all staged attachments`() = runTest {
        val note = NoteEntity(id = 13L, serverId = 103L, title = "Test", content = "Text")
        fakeDao.notes[13L] = note

        viewModel.loadNote(13L)
        advanceUntilIdle()

        val items = listOf(
            AttachmentUploadItem("pic.jpg", "image/jpeg", byteArrayOf(1))
        )

        viewModel.uploadAndInsertAttachments(items, cursorPosition = -1)
        advanceUntilIdle()

        assertEquals(1, viewModel.stagedAttachments.size)

        viewModel.discardChanges()
        advanceUntilIdle()

        assertEquals(1, fakeApi.deletedAttachments.size)
        assertEquals(103L to ".attachments.103/pic.jpg", fakeApi.deletedAttachments[0])
        assertEquals(0, viewModel.stagedAttachments.size)
    }

    private class FakeDraftStorage : DraftStorage(null) {
        val drafts = mutableMapOf<String, NoteDraft>()

        override fun saveDraft(noteId: Long?, draft: NoteDraft) {
            val key = if (noteId == null || noteId == -1L) "draft_new_note" else "draft_$noteId"
            drafts[key] = draft
        }

        override fun getDraft(noteId: Long?): NoteDraft? {
            val key = if (noteId == null || noteId == -1L) "draft_new_note" else "draft_$noteId"
            return drafts[key]
        }

        override fun clearDraft(noteId: Long?) {
            val key = if (noteId == null || noteId == -1L) "draft_new_note" else "draft_$noteId"
            drafts.remove(key)
        }

        override fun hasDraft(noteId: Long?): Boolean {
            val key = if (noteId == null || noteId == -1L) "draft_new_note" else "draft_$noteId"
            return drafts.containsKey(key)
        }
    }

    private class FakeNoteDao : NoteDao {
        val notes = mutableMapOf<Long, NoteEntity>()
        var onUpdate: (() -> Unit)? = null

        override fun getAllNotes(): Flow<List<NoteEntity>> = flowOf(notes.values.toList())
        override fun getNotesByCategory(category: String): Flow<List<NoteEntity>> = flowOf(notes.values.filter { it.category == category })
        override fun getFavorites(): Flow<List<NoteEntity>> = flowOf(notes.values.filter { it.favorite })
        override fun searchNotes(query: String): Flow<List<NoteEntity>> = flowOf(notes.values.filter { it.title.contains(query) || it.content.contains(query) })
        override fun getCategories(): Flow<List<String>> = flowOf(notes.values.map { it.category }.distinct())
        override suspend fun getNotesByServerId(serverId: Long): List<NoteEntity> = notes.values.filter { it.serverId == serverId }
        override suspend fun getNoteByServerId(serverId: Long): NoteEntity? = notes.values.find { it.serverId == serverId }
        override suspend fun getNoteById(id: Long): NoteEntity? = notes[id]
        override suspend fun getDirtyNotes(): List<NoteEntity> = notes.values.filter { it.syncStatus == SyncStatus.DIRTY }
        override suspend fun getPendingDeleteNotes(): List<NoteEntity> = notes.values.filter { it.syncStatus == SyncStatus.PENDING_DELETE }
        override suspend fun insert(note: NoteEntity): Long {
            val newId = (notes.keys.maxOrNull() ?: 0L) + 1L
            val created = note.copy(id = newId)
            notes[newId] = created
            return newId
        }
        override suspend fun update(note: NoteEntity) {
            notes[note.id] = note
            onUpdate?.invoke()
        }
        override suspend fun delete(note: NoteEntity) { notes.remove(note.id) }
        override suspend fun deleteById(id: Long) { notes.remove(id) }
        override suspend fun deletePureLocalPendingDeletes() {}
        override suspend fun deleteSyncedNotesNotIn(serverIds: List<Long>) {}
        override suspend fun deleteAllSynced() {}
        override suspend fun clearAll() { notes.clear() }
    }

    private class FakeNotesApi : NotesApi {
        val uploadedAttachments = mutableListOf<Pair<Long, MultipartBody.Part>>()
        val deletedAttachments = mutableListOf<Pair<Long, String>>()

        override suspend fun getNotes(): List<NoteDto> = emptyList()
        override suspend fun getNote(id: Long): NoteDto = NoteDto(id = id)
        override suspend fun createNote(note: NoteCreateRequest): NoteDto = NoteDto(id = 999L)
        override suspend fun updateNote(id: Long, note: NoteUpdateRequest): NoteDto = NoteDto(id = id)
        override suspend fun deleteNote(id: Long) {}

        override suspend fun uploadAttachment(noteId: Long, file: MultipartBody.Part): AttachmentDto {
            uploadedAttachments.add(noteId to file)
            // Extract filename from part headers
            val filename = file.headers?.get("Content-Disposition")?.let {
                Regex("""filename="([^"]+)"""").find(it)?.groupValues?.get(1)
            } ?: "upload.jpg"
            return AttachmentDto(filename = ".attachments.$noteId/$filename")
        }

        override suspend fun deleteAttachment(noteId: Long, path: String): Response<Unit> {
            deletedAttachments.add(noteId to path)
            return Response.success(Unit)
        }
    }
}
