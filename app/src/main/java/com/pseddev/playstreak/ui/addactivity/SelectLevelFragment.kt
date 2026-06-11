package com.pseddev.playstreak.ui.addactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.FragmentSelectLevelBinding

class SelectLevelFragment : Fragment() {
    
    private var _binding: FragmentSelectLevelBinding? = null
    private val binding get() = _binding!!
    
    private val args: SelectLevelFragmentArgs by navArgs()
    
    private val viewModel: AddActivityViewModel by activityViewModels {
        AddActivityViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectLevelBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.textPieceName.text = args.pieceName
        
        setupLevelOptions()
        
        // Check if we're in edit mode and set up the ViewModel
        val editActivity = com.pseddev.playstreak.ui.progress.EditActivityStorage.getEditActivity()
        if (editActivity != null) {
            // Set edit mode in ViewModel
            viewModel.setEditMode(editActivity)
        }

        // Pre-populate fields in edit mode
        viewModel.editActivity.observe(viewLifecycleOwner) { editActivity ->
            if (editActivity != null) {
                // Quick Save is hidden in edit mode - it would discard existing time/notes data
                binding.buttonQuickSave.visibility = View.GONE
                // Pre-select the level
                when (editActivity.level) {
                    1 -> binding.radioLevel1.isChecked = true
                    2 -> binding.radioLevel2.isChecked = true
                    3 -> binding.radioLevel3.isChecked = true
                    4 -> binding.radioLevel4.isChecked = true
                }
                
                // Pre-select performance type if applicable
                if (args.activityType == ActivityType.PERFORMANCE) {
                    when (editActivity.performanceType) {
                        "online" -> binding.radioPerformanceOnline.isChecked = true
                        "live" -> binding.radioPerformanceLive.isChecked = true
                    }
                }
            }
        }
        
        binding.buttonContinue.setOnClickListener {
            val selectedLevel = getSelectedLevel() ?: return@setOnClickListener
            val performanceType = getSelectedPerformanceType()

            // Determine next navigation
            when {
                // Time input for Practice Level 2 or any Technique
                args.activityType == ActivityType.PRACTICE && 
                (selectedLevel == 2 || args.itemType == ItemType.TECHNIQUE) -> {
                    val action = SelectLevelFragmentDirections
                        .actionSelectLevelFragmentToTimeInputFragment(
                            activityType = args.activityType,
                            pieceId = args.pieceId,
                            pieceName = args.pieceName,
                            level = selectedLevel,
                            performanceType = performanceType
                        )
                    findNavController().navigate(action)
                }
                // Notes input for everything else (Performance, and Practice without time input)
                else -> {
                    val action = SelectLevelFragmentDirections
                        .actionSelectLevelFragmentToNotesInputFragment(
                            activityType = args.activityType,
                            pieceId = args.pieceId,
                            pieceName = args.pieceName,
                            level = selectedLevel,
                            performanceType = performanceType
                        )
                    findNavController().navigate(action)
                }
            }
        }

        binding.buttonQuickSave.setOnClickListener {
            val selectedLevel = getSelectedLevel() ?: return@setOnClickListener
            val performanceType = getSelectedPerformanceType()

            // Save immediately with defaults, skipping the remaining screens
            viewModel.saveActivity(
                pieceId = args.pieceId,
                activityType = args.activityType,
                level = selectedLevel,
                performanceType = performanceType,
                minutes = -1,
                notes = ""
            )
        }

        viewModel.navigateToMain.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                // Pop back to the screen that started the add activity flow; this fragment
                // can also be reached directly from the progress screen
                if (!findNavController().popBackStack(com.pseddev.playstreak.R.id.addActivityFragment, true)) {
                    findNavController().popBackStack(com.pseddev.playstreak.R.id.progressFragment, false)
                }
                viewModel.doneNavigating()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun getSelectedLevel(): Int? = when {
        binding.radioLevel1.isChecked -> 1
        binding.radioLevel2.isChecked -> 2
        binding.radioLevel3.isChecked -> 3
        binding.radioLevel4.isChecked -> 4
        else -> null
    }

    private fun getSelectedPerformanceType(): String {
        return if (args.activityType == ActivityType.PERFORMANCE) {
            when {
                binding.radioPerformanceOnline.isChecked -> "online"
                binding.radioPerformanceLive.isChecked -> "live"
                else -> "practice"
            }
        } else {
            "practice"
        }
    }

    private fun setupLevelOptions() {
        if (args.activityType == ActivityType.PRACTICE) {
            binding.textLevelLabel.text = "Practice Level:"
            binding.radioLevel1.text = "Level 1 - Essentials"
            binding.radioLevel2.text = "Level 2 - Incomplete"
            binding.radioLevel3.text = "Level 3 - Complete with Issues"
            binding.radioLevel4.apply {
                text = "Level 4 - Complete and Satisfactory"
                visibility = View.VISIBLE
            }
            binding.performanceTypeGroup.visibility = View.GONE
        } else {
            binding.textLevelLabel.text = "Performance Level:"
            binding.radioLevel1.text = "Level 1 - Failed"
            binding.radioLevel2.text = "Level 2 - Unsatisfactory"
            binding.radioLevel3.text = "Level 3 - Satisfactory"
            binding.radioLevel4.visibility = View.GONE
            binding.performanceTypeGroup.visibility = View.VISIBLE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}