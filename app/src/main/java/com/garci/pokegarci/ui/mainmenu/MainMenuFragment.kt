package com.garci.pokegarci.ui.mainmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.ActivityMainMenuBinding
import com.garci.pokegarci.util.playClickEmeraldSound
import com.garci.pokegarci.util.setupAppTopBar
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainMenuFragment : Fragment() {

    private var _binding: ActivityMainMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ActivityMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppTopBar(
            topBar = binding.appTopBarInclude,
            title = getString(R.string.main_menu_top_bar_title),
            insetHost = binding.mainMenu,
            showBackButton = false
        )

        binding.function1.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            findNavController().navigate(R.id.action_mainMenu_to_pokedex)
        }

        binding.function2.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            findNavController().navigate(R.id.action_mainMenu_to_size)
        }

        binding.function3.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            findNavController().navigate(R.id.action_mainMenu_to_guess)
        }

        binding.function4.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            findNavController().navigate(R.id.action_mainMenu_to_language)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}