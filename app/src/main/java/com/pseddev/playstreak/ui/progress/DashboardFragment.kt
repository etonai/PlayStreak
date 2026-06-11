package com.pseddev.playstreak.ui.progress

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import android.util.Log
import com.pseddev.playstreak.BuildConfig
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        
        viewModel.todayActivities.observe(viewLifecycleOwner) { activities ->
            Log.d("DashboardUI", "Observer called with ${activities.size} activities")
            binding.todayCountText.text = "${activities.size} activities"
            populateActivityRows(binding.todayActivitiesList, activities)
        }

        viewModel.yesterdayActivities.observe(viewLifecycleOwner) { activities ->
            binding.yesterdayCountText.text = "${activities.size} activities"
            populateActivityRows(binding.yesterdayActivitiesList, activities)
        }
        
        viewModel.currentStreak.observe(viewLifecycleOwner) { streak ->
            val emojiSuffix = when {
                streak >= 100 -> " 🚀🚀🚀"
                streak >= 61 -> " 💎💎💎"
                streak >= 30 -> " ⭐⭐⭐"
                streak >= 14 -> " 🔥🔥🔥"
                streak >= 8 -> " 🔥" 
                streak >= 5 -> " 🎶"
                streak >= 3 -> " 🎵"
                else -> ""
            }
            binding.currentStreakText.text = "$streak day${if (streak != 1) "s" else ""}$emojiSuffix"
        }
        
        viewModel.weekSummary.observe(viewLifecycleOwner) { summary ->
            binding.weekSummaryText.text = summary
        }
        
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            Log.d("Phase4Debug01", "Dashboard received ${suggestions.size} total suggestions")
            val practiceCount = suggestions.count { it.suggestionType == SuggestionType.PRACTICE }
            val performanceCount = suggestions.count { it.suggestionType == SuggestionType.PERFORMANCE }
            val favoritePractice = suggestions.count { it.suggestionType == SuggestionType.PRACTICE && it.piece.isFavorite }
            val nonFavoritePractice = suggestions.count { it.suggestionType == SuggestionType.PRACTICE && !it.piece.isFavorite }
            
            Log.d("Phase4Debug02", "Dashboard breakdown: $practiceCount practice, $performanceCount performance")
            Log.d("Phase4Debug03", "Dashboard practice: $favoritePractice favorite, $nonFavoritePractice non-favorite")
            
            // Log first few suggestions for comparison
            suggestions.take(5).forEachIndexed { index, suggestion ->
                Log.d("Phase4Debug04", "Dashboard[$index]: ${suggestion.piece.name} (${suggestion.suggestionType}, favorite=${suggestion.piece.isFavorite})")
            }
            
            if (suggestions.isNotEmpty()) {
                binding.suggestionsCard.visibility = View.VISIBLE
                val suggestionText = suggestions.joinToString("\n") { suggestion ->
                    val favoriteIndicator = if (suggestion.piece.isFavorite) "⭐ " else ""
                    "• $favoriteIndicator${suggestion.piece.name} (${suggestion.suggestionReason})"
                }
                binding.suggestionsList.text = suggestionText
            } else {
                binding.suggestionsCard.visibility = View.GONE
            }
        }
        
        viewModel.performanceSuggestions.observe(viewLifecycleOwner) { suggestions ->
            // Conditional debug logging for development builds only
            if (BuildConfig.DEBUG) {
                android.util.Log.d("DashboardFragment", "Performance suggestions: ${suggestions.size}")
                if (suggestions.size <= 5) { // Limit detailed logging to avoid log spam
                    suggestions.forEach { suggestion ->
                        android.util.Log.d("DashboardFragment", "Performance Suggestion: ${suggestion.piece.name} - Type: ${suggestion.suggestionType} - Reason: ${suggestion.suggestionReason}")
                    }
                } else {
                    android.util.Log.d("DashboardFragment", "Too many suggestions (${suggestions.size}) - detailed logging skipped to avoid spam")
                }
            }
            
            if (suggestions.isNotEmpty()) {
                binding.performanceSuggestionsCard.visibility = View.VISIBLE
                val suggestionText = suggestions.joinToString("\n") { suggestion ->
                    val favoriteIndicator = if (suggestion.piece.isFavorite) "⭐ " else ""
                    "• $favoriteIndicator${suggestion.piece.name} (${suggestion.suggestionReason})"
                }
                binding.performanceSuggestionsList.text = suggestionText
            } else {
                binding.performanceSuggestionsCard.visibility = View.GONE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonAddActivity.setOnClickListener {
            findNavController().navigate(R.id.action_viewProgressFragment_to_addActivityFragment)
        }
    }

    /**
     * Fills a container with one tappable row per activity; tapping opens the activity detail.
     */
    private fun populateActivityRows(container: LinearLayout, activities: List<ActivityWithPiece>) {
        container.removeAllViews()

        if (activities.isEmpty()) {
            container.visibility = View.GONE
            return
        }
        container.visibility = View.VISIBLE

        for (activityWithPiece in activities) {
            val activity = activityWithPiece.activity
            val piece = activityWithPiece.pieceOrTechnique
            val time = android.text.format.DateFormat.format("h:mm a", activity.timestamp)
            val level = "(${activity.level})"
            val minutes = if (activity.minutes > 0) " (${activity.minutes} min)" else ""
            val type = activity.activityType.name.lowercase().replaceFirstChar { it.uppercase() }

            val row = TextView(requireContext()).apply {
                text = "• $time - ${piece.name} - $type $level$minutes"
                setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2)
                val secondaryColor = TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.textColorSecondary, secondaryColor, true)
                setTextColor(resources.getColorStateList(secondaryColor.resourceId, requireContext().theme))
                val rippleBackground = TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, rippleBackground, true)
                setBackgroundResource(rippleBackground.resourceId)
                setPadding(0, 8, 0, 8)
                setOnClickListener {
                    ActivityDetailDialogFragment.newInstance(activityWithPiece)
                        .show(parentFragmentManager, "ActivityDetailDialog")
                }
            }
            container.addView(row)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}