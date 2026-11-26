package com.keepnc.ui.notes

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import com.keepnc.R
import com.keepnc.data.local.NoteEntity
import com.keepnc.data.local.SyncStatus
import com.keepnc.data.remote.AttachmentUrlUtils
import com.keepnc.data.settings.FontSizePreset
import com.keepnc.databinding.ItemNoteCardBinding
import com.keepnc.ui.MarkwonFactory

/**
 * RecyclerView adapter for the note card grid.
 *
 * Uses [ListAdapter] + [DiffUtil.ItemCallback] so only changed cards are redrawn,
 * preventing the whole list from flickering on every Room update.
 *
 * Supports:
 * - Google Keep-style top image thumbnail for notes containing image attachments.
 * - Markwon markdown text with task-list checkboxes.
 *
 * @param onNoteClick     Invoked when the user taps a card
 * @param onNoteLongClick Invoked on long-press; return true to consume the event
 * @param cardFontSize    Font size preset for card title and content
 * @param serverUrl       Nextcloud base server URL for resolving attachment paths
 */
class NoteCardAdapter(
    private val onNoteClick: (NoteEntity) -> Unit,
    private val onNoteLongClick: (NoteEntity) -> Boolean,
    var cardFontSize: FontSizePreset = FontSizePreset.DEFAULT,
    var serverUrl: String? = null
) : ListAdapter<NoteEntity, NoteCardAdapter.NoteViewHolder>(DiffCallback) {

    // -----------------------------------------------------------------------
    // ViewHolder
    // -----------------------------------------------------------------------

    inner class NoteViewHolder(private val binding: ItemNoteCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // One Markwon instance per ViewHolder — created lazily on first bind
        private val markwon by lazy { MarkwonFactory.createForCard(binding.root.context) }

        fun bind(note: NoteEntity) {
            // Apply card font size
            binding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, cardFontSize.cardTitleSp)
            binding.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, cardFontSize.cardContentSp)

            // Image thumbnail (Google Keep style)
            val firstImageUrl = AttachmentUrlUtils.extractFirstImageUrl(note.content)
            if (firstImageUrl != null) {
                val resolvedUrl = AttachmentUrlUtils.resolveAttachmentUrl(serverUrl, note.serverId, firstImageUrl)
                binding.ivThumbnail.visibility = View.VISIBLE
                binding.ivThumbnail.load(resolvedUrl) {
                    crossfade(true)
                }
            } else {
                binding.ivThumbnail.visibility = View.GONE
                binding.ivThumbnail.dispose()
            }

            // Title — hide when blank so the card looks cleaner
            if (note.title.isBlank()) {
                binding.tvTitle.visibility = View.GONE
            } else {
                binding.tvTitle.visibility = View.VISIBLE
                binding.tvTitle.text = note.title
            }

            // Content excerpt rendered as Markdown (includes task-list checkboxes).
            // Nextcloud Notes stores the title as the first line of content, so we
            // strip it here to avoid showing it twice (the card header already has it).
            val rawExcerpt = note.excerpt
            val excerptWithoutTitle = if (note.title.isNotBlank()) {
                val firstLine = rawExcerpt.substringBefore('\n').trim()
                if (firstLine == note.title.trim()) {
                    rawExcerpt.substringAfter('\n', missingDelimiterValue = "").trimStart('\r', '\n')
                } else rawExcerpt
            } else rawExcerpt

            // Strip raw Markdown image syntax `![alt](url)` so card excerpt isn't cluttered
            val cleanExcerpt = AttachmentUrlUtils.stripImageMarkdown(excerptWithoutTitle)
            val formattedExcerpt = MarkwonFactory.formatChecklistStrikethrough(cleanExcerpt)

            if (formattedExcerpt.isBlank()) {
                binding.tvContent.visibility = View.GONE
            } else {
                binding.tvContent.visibility = View.VISIBLE
                markwon.setMarkdown(binding.tvContent, formattedExcerpt)
                // Markwon sets LinkMovementMethod which swallows all touch events —
                // remove it so clicks bubble up to the MaterialCardView instead.
                binding.tvContent.movementMethod = null
            }

            // Category chip
            if (note.category.isBlank()) {
                binding.tvCategory.visibility = View.GONE
            } else {
                binding.tvCategory.visibility = View.VISIBLE
                binding.tvCategory.text = note.category
            }

            // Favourite / pin icon
            binding.ivFavorite.setImageResource(
                if (note.favorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            // Sync indicator dot — visible when the note has local unsaved changes
            binding.ivSyncIndicator.visibility =
                if (note.syncStatus == SyncStatus.DIRTY ||
                    note.syncStatus == SyncStatus.PENDING_DELETE
                ) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onNoteClick(note) }
            binding.root.setOnLongClickListener { onNoteLongClick(note) }
        }
    }

    // -----------------------------------------------------------------------
    // Adapter overrides
    // -----------------------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // -----------------------------------------------------------------------
    // DiffUtil
    // -----------------------------------------------------------------------

    companion object DiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem == newItem
    }
}
