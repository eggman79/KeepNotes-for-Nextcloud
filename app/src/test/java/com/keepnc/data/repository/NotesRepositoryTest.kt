package com.keepnc.data.repository

import com.keepnc.data.auth.TokenStorage
import com.keepnc.data.local.NoteDao
import com.keepnc.data.local.NoteEntity
import com.keepnc.data.local.SyncStatus
import com.keepnc.data.remote.AttachmentUploadItem
import com.keepnc.data.remote.NotesApi
import com.keepnc.data.remote.dto.AttachmentDto
import com.keepnc.data.remote.dto.NoteCreateRequest
import com.keepnc.data.remote.dto.NoteDto
import com.keepnc.data.remote.dto.NoteUpdateRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class NotesRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var noteDao: FakeNoteDao
    private lateinit var notesApi: FakeNotesApi
    private lateinit var tokenStorage: TokenStorage
    private lateinit var repository: NotesRepository

    @Before
    fun setUp() {
        noteDao = FakeNoteDao()
        notesApi = FakeNotesApi()
        tokenStorage = object : TokenStorage() {
            override fun isLoggedIn(): Boolean = true
        }
        repository = NotesRepository(noteDao, notesApi, tokenStorage).apply {
            ioDispatcher = testDispatcher
        }
    }

    @Test
    fun `ensureServerId returns existing serverId when already present`() = runTest(testDispatcher) {
        val note = NoteEntity(id = 1L, serverId = 42L, title = "Title", content = "Content")
        noteDao.notes[1L] = note

        val serverId = repository.ensureServerId(1L)
        assertEquals(42L, serverId)
        assertEquals(0, notesApi.remoteNotes.size)
    }

    @Test
    fun `ensureServerId pushes note to server when serverId is null`() = runTest(testDispatcher) {
        val note = NoteEntity(id = 1L, serverId = null, title = "Local Note", content = "Content")
        noteDao.notes[1L] = note

        val serverId = repository.ensureServerId(1L)
        assertEquals(999L, serverId)
        val updatedNote = noteDao.notes[1L]
        assertNotNull(updatedNote)
        assertEquals(999L, updatedNote?.serverId)
        assertEquals(SyncStatus.SYNCED, updatedNote?.syncStatus)
    }

    @Test
    fun `uploadAttachment uploads file and returns filename`() = runTest(testDispatcher) {
        val item = AttachmentUploadItem("photo.jpg", "image/jpeg", byteArrayOf(1, 2, 3))
        val result = repository.uploadAttachment(42L, item)

        assertTrue(result.isSuccess)
        assertEquals(".attachments.42/test.jpg", result.getOrNull())
        assertEquals(1, notesApi.uploadedAttachments.size)
        assertEquals(42L, notesApi.uploadedAttachments[0].first)
    }

    @Test
    fun `deleteAttachment calls API with noteId and path`() = runTest(testDispatcher) {
        val result = repository.deleteAttachment(42L, ".attachments.42/photo.jpg")

        assertTrue(result.isSuccess)
        assertEquals(1, notesApi.deletedAttachments.size)
        assertEquals(42L to ".attachments.42/photo.jpg", notesApi.deletedAttachments[0])
    }

    private class FakeNoteDao : NoteDao {
        val notes = mutableMapOf<Long, NoteEntity>()

        override fun getAllNotes(): Flow<List<NoteEntity>> = flowOf(notes.values.toList())
        override fun getNotesByCategory(category: String): Flow<List<NoteEntity>> = flowOf(notes.values.filter { it.category == category })
        override fun getFavorites(): Flow<List<NoteEntity>> = flowOf(notes.values.filter { it.favorite })
        override fun searchNotes(query: String): Flow<List<NoteEntity>> = flowOf(notes.values.filter { it.title.contains(query) })
        override fun getCategories(): Flow<List<String>> = flowOf(notes.values.map { it.category }.distinct())
        override suspend fun getNotesByServerId(serverId: Long): List<NoteEntity> = notes.values.filter { it.serverId == serverId }
        override suspend fun getNoteByServerId(serverId: Long): NoteEntity? = notes.values.firstOrNull { it.serverId == serverId }
        override suspend fun getNoteById(id: Long): NoteEntity? = notes[id]
        override suspend fun getDirtyNotes(): List<NoteEntity> = notes.values.filter { it.syncStatus == SyncStatus.DIRTY }
        override suspend fun getPendingDeleteNotes(): List<NoteEntity> = notes.values.filter { it.syncStatus == SyncStatus.PENDING_DELETE }
        override suspend fun insert(note: NoteEntity): Long {
            val id = if (note.id == 0L) (notes.keys.maxOrNull() ?: 0L) + 1L else note.id
            notes[id] = note.copy(id = id)
            return id
        }
        override suspend fun update(note: NoteEntity) { notes[note.id] = note }
        override suspend fun delete(note: NoteEntity) { notes.remove(note.id) }
        override suspend fun deleteById(id: Long) { notes.remove(id) }
        override suspend fun deletePureLocalPendingDeletes() {}
        override suspend fun deleteSyncedNotesNotIn(serverIds: List<Long>) {}
        override suspend fun deleteAllSynced() {
            val toRemove = notes.values.filter { it.syncStatus == SyncStatus.SYNCED }
            toRemove.forEach { notes.remove(it.id) }
        }
        override suspend fun clearAll() { notes.clear() }
    }

    private class FakeNotesApi : NotesApi {
        val remoteNotes = mutableListOf<NoteDto>()
        val uploadedAttachments = mutableListOf<Pair<Long, MultipartBody.Part>>()
        val deletedAttachments = mutableListOf<Pair<Long, String>>()

        override suspend fun getNotes(): List<NoteDto> = remoteNotes
        override suspend fun getNote(id: Long): NoteDto = remoteNotes.first { it.id == id }
        override suspend fun createNote(note: NoteCreateRequest): NoteDto = NoteDto(id = 999L, title = note.title, content = note.content)
        override suspend fun updateNote(id: Long, note: NoteUpdateRequest): NoteDto = NoteDto(id = id, title = note.title, content = note.content)
        override suspend fun deleteNote(id: Long) {
            remoteNotes.removeAll { it.id == id }
        }

        override suspend fun uploadAttachment(noteId: Long, file: MultipartBody.Part): AttachmentDto {
            uploadedAttachments.add(noteId to file)
            return AttachmentDto(filename = ".attachments.$noteId/test.jpg")
        }

        override suspend fun deleteAttachment(noteId: Long, path: String): Response<Unit> {
            deletedAttachments.add(noteId to path)
            return Response.success(Unit)
        }
    }
}
