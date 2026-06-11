package com.pseddev.playstreak.ui.progress

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.DialogActivityDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shows the full details of a single activity, styled after the Piece detail view.
 * View-only: launched by tapping an activity on the Dashboard or Calendar tabs.
 */
class ActivityDetailDialogFragment : DialogFragment() {

    private var _binding: DialogActivityDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogActivityDetailBinding.inflate(layoutInflater)

        arguments?.let { populateDetails(it) }

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun populateDetails(args: Bundle) {
        val pieceName = args.getString(ARG_PIECE_NAME, "")
        val pieceType = ItemType.valueOf(args.getString(ARG_PIECE_TYPE, ItemType.PIECE.name))
        val activityType = ActivityType.valueOf(args.getString(ARG_ACTIVITY_TYPE, ActivityType.PRACTICE.name))
        val level = args.getInt(ARG_LEVEL)
        val performanceType = args.getString(ARG_PERFORMANCE_TYPE, "")
        val minutes = args.getInt(ARG_MINUTES, -1)
        val notes = args.getString(ARG_NOTES, "")
        val timestamp = args.getLong(ARG_TIMESTAMP)
        val isFavorite = args.getBoolean(ARG_IS_FAVORITE)
        val artist = args.getString(ARG_ARTIST)
        val key = args.getString(ARG_KEY)

        // Header (technique emoji matches Timeline/Calendar display)
        binding.pieceNameText.text = if (pieceType == ItemType.TECHNIQUE) {
            "🎼 $pieceName"
        } else {
            pieceName
        }

        // Activity Information
        binding.activityTypeText.text = when (activityType) {
            ActivityType.PRACTICE -> "Type: Practice"
            ActivityType.PERFORMANCE -> {
                val typeText = if (performanceType == "online") "Online" else "Live"
                "Type: Performance ($typeText)"
            }
        }
        binding.levelText.text = "Level: ${getLevelText(activityType, level)}"

        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        binding.dateText.text = "Date: ${dateFormat.format(Date(timestamp))}"

        if (minutes > 0) {
            binding.durationText.text = "Duration: $minutes minutes"
            binding.durationText.visibility = View.VISIBLE
        } else {
            binding.durationText.visibility = View.GONE
        }

        if (notes.isNotEmpty()) {
            binding.notesText.text = "Notes: $notes"
            binding.notesText.visibility = View.VISIBLE
        } else {
            binding.notesText.visibility = View.GONE
        }

        // Piece Information
        binding.favoriteText.text = "Favorite: ${if (isFavorite) "Yes" else "No"}"

        if (!artist.isNullOrBlank()) {
            binding.artistText.text = "Artist: $artist"
            binding.artistText.visibility = View.VISIBLE
        } else {
            binding.artistText.visibility = View.GONE
        }

        if (!key.isNullOrBlank()) {
            binding.keyText.text = "Key: $key"
            binding.keyText.visibility = View.VISIBLE
        } else {
            binding.keyText.visibility = View.GONE
        }
    }

    private fun getLevelText(activityType: ActivityType, level: Int): String {
        return when (activityType) {
            ActivityType.PRACTICE -> when (level) {
                1 -> "Level 1 - Essentials"
                2 -> "Level 2 - Incomplete"
                3 -> "Level 3 - Complete with Issues"
                4 -> "Level 4 - Complete and Satisfactory"
                else -> "Level $level"
            }
            ActivityType.PERFORMANCE -> when (level) {
                1 -> "Level 1 - Failed"
                2 -> "Level 2 - Unsatisfactory"
                3 -> "Level 3 - Satisfactory"
                else -> "Level $level"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PIECE_NAME = "piece_name"
        private const val ARG_PIECE_TYPE = "piece_type"
        private const val ARG_ACTIVITY_TYPE = "activity_type"
        private const val ARG_LEVEL = "level"
        private const val ARG_PERFORMANCE_TYPE = "performance_type"
        private const val ARG_MINUTES = "minutes"
        private const val ARG_NOTES = "notes"
        private const val ARG_TIMESTAMP = "timestamp"
        private const val ARG_IS_FAVORITE = "is_favorite"
        private const val ARG_ARTIST = "artist"
        private const val ARG_KEY = "key"

        fun newInstance(activityWithPiece: ActivityWithPiece): ActivityDetailDialogFragment {
            val activity = activityWithPiece.activity
            val piece = activityWithPiece.pieceOrTechnique
            return ActivityDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PIECE_NAME, piece.name)
                    putString(ARG_PIECE_TYPE, piece.type.name)
                    putString(ARG_ACTIVITY_TYPE, activity.activityType.name)
                    putInt(ARG_LEVEL, activity.level)
                    putString(ARG_PERFORMANCE_TYPE, activity.performanceType)
                    putInt(ARG_MINUTES, activity.minutes)
                    putString(ARG_NOTES, activity.notes)
                    putLong(ARG_TIMESTAMP, activity.timestamp)
                    putBoolean(ARG_IS_FAVORITE, piece.isFavorite)
                    putString(ARG_ARTIST, piece.artist)
                    putString(ARG_KEY, piece.key)
                }
            }
        }
    }
}