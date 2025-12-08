package com.pseddev.playstreak.ui.progress

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.DialogEditPieceBinding

class EditPieceDialogFragment : DialogFragment() {
    
    private var _binding: DialogEditPieceBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PiecesViewModel by viewModels({requireParentFragment()}) {
        PiecesViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    private var pieceId: Long = -1
    private var currentName: String = ""
    private var currentType: ItemType = ItemType.PIECE
    private var currentKey: String? = null
    private var currentArtist: String? = null
    private var currentNotes: String? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditPieceBinding.inflate(layoutInflater)

        // Get arguments
        arguments?.let {
            pieceId = it.getLong(ARG_PIECE_ID, -1)
            currentName = it.getString(ARG_PIECE_NAME, "")
            currentType = ItemType.valueOf(it.getString(ARG_PIECE_TYPE, ItemType.PIECE.name))
            currentKey = it.getString(ARG_PIECE_KEY)
            currentArtist = it.getString(ARG_PIECE_ARTIST)
            currentNotes = it.getString(ARG_PIECE_NOTES)
        }

        // Set up key dropdown
        setupKeyDropdown()

        // Pre-populate fields
        binding.pieceNameEditText.setText(currentName)
        when (currentType) {
            ItemType.PIECE -> binding.radioPiece.isChecked = true
            ItemType.TECHNIQUE -> binding.radioTechnique.isChecked = true
        }
        binding.artistEditText.setText(currentArtist ?: "")
        binding.keyAutoCompleteTextView.setText(currentKey ?: "None", false)
        binding.notesEditText.setText(currentNotes ?: "")

        // Set up button listeners
        binding.saveButton.setOnClickListener {
            saveChanges()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupKeyDropdown() {
        val keys = listOf(
            "None",
            "C Major", "C Minor",
            "G Major", "G Minor",
            "D Major", "D Minor",
            "A Major", "A Minor",
            "E Major", "E Minor",
            "B Major", "B Minor",
            "F# Major", "F# Minor",
            "Db Major", "Db Minor",
            "Ab Major", "Ab Minor",
            "Eb Major", "Eb Minor",
            "Bb Major", "Bb Minor",
            "F Major", "F Minor"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, keys)
        binding.keyAutoCompleteTextView.setAdapter(adapter)
    }
    
    private fun saveChanges() {
        val newName = binding.pieceNameEditText.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a piece name", Toast.LENGTH_SHORT).show()
            return
        }

        val newType = if (binding.radioPiece.isChecked) ItemType.PIECE else ItemType.TECHNIQUE

        // Capture new metadata fields
        val newArtist = binding.artistEditText.text?.toString()?.trim()
        val selectedKey = binding.keyAutoCompleteTextView.text?.toString()?.trim()
        val newKey = if (selectedKey == "None" || selectedKey.isNullOrBlank()) null else selectedKey
        val newNotes = binding.notesEditText.text?.toString()?.trim()

        // Only update if something changed
        if (newName != currentName || newType != currentType ||
            newKey != currentKey || newArtist != currentArtist || newNotes != currentNotes) {
            viewModel.updatePiece(pieceId, newName, newType, newKey, newArtist, newNotes)
            Toast.makeText(requireContext(), "Piece updated", Toast.LENGTH_SHORT).show()
        }

        dismiss()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val ARG_PIECE_ID = "piece_id"
        private const val ARG_PIECE_NAME = "piece_name"
        private const val ARG_PIECE_TYPE = "piece_type"
        private const val ARG_PIECE_KEY = "piece_key"
        private const val ARG_PIECE_ARTIST = "piece_artist"
        private const val ARG_PIECE_NOTES = "piece_notes"

        fun newInstance(
            pieceId: Long,
            pieceName: String,
            pieceType: ItemType,
            key: String? = null,
            artist: String? = null,
            notes: String? = null
        ): EditPieceDialogFragment {
            return EditPieceDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_PIECE_ID, pieceId)
                    putString(ARG_PIECE_NAME, pieceName)
                    putString(ARG_PIECE_TYPE, pieceType.name)
                    putString(ARG_PIECE_KEY, key)
                    putString(ARG_PIECE_ARTIST, artist)
                    putString(ARG_PIECE_NOTES, notes)
                }
            }
        }
    }
}