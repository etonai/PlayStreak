package com.pseddev.playstreak.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pseddev.playstreak.databinding.FragmentConfigurationBinding
import com.pseddev.playstreak.utils.PreferencesManager
import com.pseddev.playstreak.utils.ConfigurationManager
import com.pseddev.playstreak.utils.ProUserManager

class ConfigurationFragment : Fragment() {
    
    private var _binding: FragmentConfigurationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ConfigurationViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var proUserManager: ProUserManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager.getInstance(requireContext())
        configurationManager = ConfigurationManager.getInstance(requireContext())
        proUserManager = ProUserManager.getInstance(requireContext())

        // Create ViewModel with custom factory
        viewModel = ViewModelProvider(this, ConfigurationViewModelFactory(preferencesManager))
            .get(ConfigurationViewModel::class.java)

        setupObservers()
        setupClickListeners()
        setupPruningToggle()
        setupAchievementCelebrationsToggle()
        setupProModeToggle()
    }
    
    private fun setupObservers() {
        viewModel.calendarDetailModeEnabled.observe(viewLifecycleOwner) { enabled ->
            // Update switch state without triggering listener
            binding.switchCalendarDetailMode.setOnCheckedChangeListener(null)
            binding.switchCalendarDetailMode.isChecked = enabled
            binding.switchCalendarDetailMode.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setCalendarDetailMode(isChecked)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.switchCalendarDetailMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setCalendarDetailMode(isChecked)
        }
    }
    
    private fun setupPruningToggle() {
        // Set initial state from ConfigurationManager
        binding.switchAllowPruning.isChecked = configurationManager.isPruningEnabled()
        
        // Set up toggle listener
        binding.switchAllowPruning.setOnCheckedChangeListener { _, isChecked ->
            configurationManager.setPruningEnabled(isChecked)
        }
    }
    
    private fun setupAchievementCelebrationsToggle() {
        // Set initial state from ConfigurationManager
        binding.switchAchievementCelebrations.isChecked = configurationManager.isAchievementCelebrationEnabled()

        // Set up toggle listener
        binding.switchAchievementCelebrations.setOnCheckedChangeListener { _, isChecked ->
            configurationManager.setAchievementCelebrationEnabled(isChecked)
        }
    }

    private fun setupProModeToggle() {
        // Set initial state from ProUserManager
        binding.switchProMode.isChecked = proUserManager.isProUser()

        // Set up toggle listener
        binding.switchProMode.setOnCheckedChangeListener { _, isChecked ->
            proUserManager.setProUser(isChecked)
            Toast.makeText(
                requireContext(),
                if (isChecked) "Pro Mode enabled" else "Pro Mode disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ConfigurationViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigurationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfigurationViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}