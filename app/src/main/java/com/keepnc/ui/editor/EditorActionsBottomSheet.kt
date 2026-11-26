package com.keepnc.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.keepnc.R
import com.keepnc.databinding.DialogEditorActionsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * BottomSheet dialog for editor overflow actions:
 * 1. "Show checkboxes" / "Hide checkboxes" (Convert note to checklist and back)
 * 2. "Category" (Open category picker)
 * 3. "Delete note" (Confirm and delete note)
 */
@AndroidEntryPoint
class EditorActionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogEditorActionsBinding? = null
    private val binding get() = _binding!!

    var onAddImagesClickListener: (() -> Unit)? = null
    var onManageAttachmentsClickListener: (() -> Unit)? = null
    var onToggleCheckboxesListener: (() -> Unit)? = null
    var onCategoryClickListener: (() -> Unit)? = null
    var onDeleteClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditorActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hasCheckboxes = arguments?.getBoolean(ARG_HAS_CHECKBOXES) ?: false
        val attachmentCount = arguments?.getInt(ARG_ATTACHMENT_COUNT) ?: 0

        binding.tvToggleCheckboxes.text = getString(
            if (hasCheckboxes) R.string.editor_action_hide_checkboxes
            else R.string.editor_action_show_checkboxes
        )

        binding.actionAddImages.setOnClickListener {
            dismiss()
            onAddImagesClickListener?.invoke()
        }

        if (attachmentCount > 0) {
            binding.actionManageAttachments.visibility = View.VISIBLE
            binding.tvManageAttachments.text = getString(R.string.action_manage_attachments, attachmentCount)
            binding.actionManageAttachments.setOnClickListener {
                dismiss()
                onManageAttachmentsClickListener?.invoke()
            }
        } else {
            binding.actionManageAttachments.visibility = View.GONE
        }

        binding.actionToggleCheckboxes.setOnClickListener {
            dismiss()
            onToggleCheckboxesListener?.invoke()
        }

        binding.actionCategory.setOnClickListener {
            dismiss()
            onCategoryClickListener?.invoke()
        }

        binding.actionDelete.setOnClickListener {
            dismiss()
            onDeleteClickListener?.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditorActionsBottomSheet"
        private const val ARG_HAS_CHECKBOXES = "has_checkboxes"
        private const val ARG_ATTACHMENT_COUNT = "attachment_count"

        fun newInstance(hasCheckboxes: Boolean, attachmentCount: Int = 0): EditorActionsBottomSheet {
            return EditorActionsBottomSheet().apply {
                arguments = bundleOf(
                    ARG_HAS_CHECKBOXES to hasCheckboxes,
                    ARG_ATTACHMENT_COUNT to attachmentCount
                )
            }
        }
    }
}
