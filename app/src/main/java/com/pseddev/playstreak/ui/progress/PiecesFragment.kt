package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentPiecesBinding
import com.pseddev.playstreak.ui.progress.QuickAddActivityDialogFragment
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.DateFormatter

class PiecesFragment : Fragment() {
    
    private var _binding: FragmentPiecesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PiecesViewModel by viewModels {
        PiecesViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    private lateinit var adapter: PiecesAdapter
    private var shouldScrollToTop = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPiecesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        setupSortingControls()
        
        val proUserManager = ProUserManager.getInstance(requireContext())
        
        adapter = PiecesAdapter(
            onPieceClick = { pieceWithStats ->
                viewModel.selectPiece(pieceWithStats.piece.id)
            },
            onFavoriteToggle = { pieceWithStats ->
                val success = viewModel.toggleFavorite(pieceWithStats)
                if (!success) {
                    showFavoriteLimitPrompt()
                }
            },
            onAddActivityClick = { pieceWithStats ->
                // Show quick add activity dialog with piece pre-filled
                val dialog = QuickAddActivityDialogFragment.newInstance(
                    pieceWithStats.piece.id,
                    pieceWithStats.piece.name,
                    "dashboard_quick"
                )
                dialog.show(parentFragmentManager, "QuickAddActivityDialog")
            },
            onEditClick = { pieceWithStats ->
                val dialog = EditPieceDialogFragment.newInstance(
                    pieceWithStats.piece.id,
                    pieceWithStats.piece.name,
                    pieceWithStats.piece.type,
                    pieceWithStats.piece.key,
                    pieceWithStats.piece.artist,
                    pieceWithStats.piece.notes
                )
                dialog.show(parentFragmentManager, "EditPieceDialog")
            },
            onDeleteClick = { pieceWithStats ->
                showDeleteConfirmationDialog(pieceWithStats)
            },
            proUserManager
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        
        viewModel.piecesWithStats.observe(viewLifecycleOwner) { pieces ->
            if (pieces.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(pieces) {
                    // Scroll to top after the new data is submitted if a sort change happened
                    if (shouldScrollToTop) {
                        binding.recyclerView.scrollToPosition(0)
                        shouldScrollToTop = false
                    }
                }
            }
        }
        
        viewModel.selectedPieceDetails.observe(viewLifecycleOwner) { details ->
            details?.let {
                showPieceDetails(it)
            }
        }
    }
    
    private fun showPieceDetails(details: PieceDetails) {
        binding.pieceDetailsCard.visibility = View.VISIBLE
        val piece = details.piece
        
        // Header
        binding.pieceNameText.text = piece.name
        
        // Basic Information Section
        // Artist (conditional visibility)
        if (!piece.artist.isNullOrBlank()) {
            binding.artistText.text = "Artist: ${piece.artist}"
            binding.artistText.visibility = View.VISIBLE
        } else {
            binding.artistText.visibility = View.GONE
        }

        // Key (conditional visibility)
        if (!piece.key.isNullOrBlank()) {
            binding.keyText.text = "Key: ${piece.key}"
            binding.keyText.visibility = View.VISIBLE
        } else {
            binding.keyText.visibility = View.GONE
        }

        // Type (always visible)
        binding.pieceTypeText.text = "Type: ${piece.type.name.lowercase().replaceFirstChar { it.uppercase() }}"

        // Notes (conditional visibility)
        if (!piece.notes.isNullOrBlank()) {
            binding.notesText.text = "Notes: ${piece.notes}"
            binding.notesText.visibility = View.VISIBLE
        } else {
            binding.notesText.visibility = View.GONE
        }

        // Favorite (always visible)
        binding.isFavoriteText.text = "Favorite: ${if (piece.isFavorite) "Yes" else "No"}"

        // Created (always visible)
        binding.dateCreatedText.text = "Created: ${DateFormatter.formatDateOnly(piece.dateCreated)}"
        
        // Practice Statistics Section (from Phase 1 statistics)
        binding.practiceCountText.text = "Total Practices: ${piece.practiceCount}"
        binding.lastPracticeText.text = "Last Practice: ${DateFormatter.formatDate(piece.lastPracticeDate)}"
        binding.secondLastPracticeText.text = "2nd Last Practice: ${DateFormatter.formatDateWithFallback(piece.secondLastPracticeDate)}"
        binding.thirdLastPracticeText.text = "3rd Last Practice: ${DateFormatter.formatDateWithFallback(piece.thirdLastPracticeDate)}"
        binding.lastSatisfactoryPracticeText.text = "Last Satisfactory Practice: ${DateFormatter.formatDate(piece.lastSatisfactoryPractice)}"
        
        // Performance Statistics Section (from Phase 1 statistics)
        binding.performanceCountText.text = "Total Performances: ${piece.performanceCount}"
        binding.lastPerformanceText.text = "Last Performance: ${DateFormatter.formatDate(piece.lastPerformanceDate)}"
        binding.secondLastPerformanceText.text = "2nd Last Performance: ${DateFormatter.formatDateWithFallback(piece.secondLastPerformanceDate)}"
        binding.thirdLastPerformanceText.text = "3rd Last Performance: ${DateFormatter.formatDateWithFallback(piece.thirdLastPerformanceDate)}"
        binding.lastSatisfactoryPerformanceText.text = "Last Satisfactory Performance: ${DateFormatter.formatDate(piece.lastSatisfactoryPerformance)}"
        
        // Legacy Activity Data Section (calculated from activities for comparison)
        val practiceCountLegacy = details.activities.count { it.activityType == com.pseddev.playstreak.data.entities.ActivityType.PRACTICE }
        val performanceCountLegacy = details.activities.count { it.activityType == com.pseddev.playstreak.data.entities.ActivityType.PERFORMANCE }
        binding.totalActivitiesText.text = "Total Activities: ${details.activities.size} (P:$practiceCountLegacy, F:$performanceCountLegacy)"
        
        val totalMinutes = details.activities.filter { it.minutes > 0 }.sumOf { it.minutes }
        if (totalMinutes > 0) {
            binding.totalTimeText.text = "Total Tracked Time: $totalMinutes minutes"
            binding.totalTimeText.visibility = View.VISIBLE
        } else {
            binding.totalTimeText.text = "Total Tracked Time: No time data"
            binding.totalTimeText.visibility = View.VISIBLE
        }
        
        if (details.lastActivity != null) {
            binding.lastActivityText.text = "Last Activity (Legacy): ${DateFormatter.formatDate(details.lastActivity.timestamp)}"
        } else {
            binding.lastActivityText.text = "Last Activity (Legacy): No activities recorded"
        }
        
        // System Information Section
        binding.lastUpdatedText.text = "Last Updated: ${DateFormatter.formatDateTime(piece.lastUpdated)}"
        
        binding.closeDetailsButton.setOnClickListener {
            binding.pieceDetailsCard.visibility = View.GONE
            viewModel.clearSelection()
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonAddPiece.setOnClickListener {
            findNavController().navigate(R.id.action_viewProgressFragment_to_addPieceFragment)
        }
    }
    
    private fun setupSortingControls() {
        // Set up chip selection listener
        binding.sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val sortType = when (checkedIds[0]) {
                    R.id.chipAlphabetical -> SortType.ALPHABETICAL
                    R.id.chipLastDate -> SortType.LAST_DATE
                    R.id.chipActivityCount -> SortType.ACTIVITY_COUNT
                    else -> SortType.ALPHABETICAL
                }
                viewModel.setSortType(sortType)
                shouldScrollToTop = true
            }
        }
        
        // Set up sort direction button
        binding.buttonSortDirection.setOnClickListener {
            viewModel.toggleSortDirection()
            updateSortDirectionButton()
            shouldScrollToTop = true
        }
        
        // Initialize sort direction button
        updateSortDirectionButton()
    }
    
    private fun updateSortDirectionButton() {
        val direction = viewModel.getCurrentSortDirection()
        binding.buttonSortDirection.text = if (direction == SortDirection.ASCENDING) "↑" else "↓"
    }
    
    private fun showFavoriteLimitPrompt() {
        AlertDialog.Builder(requireContext())
            .setTitle("Favorite Limit")
            .setMessage("You can have up to ${ProUserManager.FREE_USER_FAVORITE_LIMIT} favorite pieces.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showDeleteConfirmationDialog(pieceWithStats: PieceWithStats) {
        val activityText = if (pieceWithStats.activityCount == 1) {
            "1 activity"
        } else {
            "${pieceWithStats.activityCount} activities"
        }
        
        val message = "Delete \"${pieceWithStats.piece.name}\" and all its $activityText?\n\n" +
                "This action cannot be undone."
        
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Piece")
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePiece(pieceWithStats)
                Toast.makeText(requireContext(), "Piece deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}