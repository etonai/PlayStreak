package com.pseddev.playstreak.ui.pieces

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.databinding.FragmentAddPieceBinding

class AddPieceFragment : Fragment() {
    
    private var _binding: FragmentAddPieceBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddPieceViewModel by viewModels {
        AddPieceViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository,
            requireContext()
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPieceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyDropdown()
        setupClickListeners()
        observeViewModel()
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
    
    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            val name = binding.pieceNameEditText.text?.toString()?.trim()

            if (name.isNullOrBlank()) {
                binding.pieceNameInputLayout.error = "Please enter a piece name"
                return@setOnClickListener
            }

            binding.pieceNameInputLayout.error = null

            val type = if (binding.radioPiece.isChecked) ItemType.PIECE else ItemType.TECHNIQUE
            // Only mark as favorite if the switch is visible and checked
            val isFavorite = binding.favoriteSwitch.visibility == View.VISIBLE && binding.favoriteSwitch.isChecked

            // Capture new metadata fields
            val artist = binding.artistEditText.text?.toString()?.trim()
            val selectedKey = binding.keyAutoCompleteTextView.text?.toString()?.trim()
            val key = if (selectedKey == "None" || selectedKey.isNullOrBlank()) null else selectedKey
            val notes = binding.notesEditText.text?.toString()?.trim()

            viewModel.savePiece(name, type, isFavorite, key, artist, notes)
        }
        
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddPieceResult.Success -> {
                    Toast.makeText(requireContext(), "Piece added successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is AddPieceResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is AddPieceResult.PieceLimitReached -> {
                    showPieceLimitDialog(result.currentCount, result.limit, result.isProUser)
                }
                is AddPieceResult.DuplicateName -> {
                    binding.pieceNameInputLayout.error = "This piece already exists"
                }
            }
        }
        
        viewModel.canAddFavorites.observe(viewLifecycleOwner) { canAdd ->
            binding.favoriteSwitch.visibility = if (canAdd) View.VISIBLE else View.GONE
        }
        
        viewModel.showCelebration.observe(viewLifecycleOwner) { achievementType ->
            if (achievementType != null) {
                val achievement = com.pseddev.playstreak.utils.AchievementDefinitions.getAllAchievementDefinitions()
                    .find { it.type == achievementType }
                if (achievement != null) {
                    viewModel.getCelebrationManager().showCelebration(binding.root, achievement)
                }
                viewModel.onCelebrationHandled()
            }
        }
    }
    
    private fun showPieceLimitDialog(currentCount: Int, limit: Int, isProUser: Boolean) {
        val message = "Piece Limit Reached\n\n" +
                "You currently have $currentCount pieces and techniques. " +
                "This app supports up to $limit pieces total to ensure good performance.\n\n" +
                "You can keep all your existing pieces, but cannot add new ones until you remove some pieces."

        AlertDialog.Builder(requireContext())
            .setTitle("Cannot Add Piece")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}