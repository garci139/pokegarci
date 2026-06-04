package com.garci.pokegarci.ui.language

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.garci.pokegarci.R
import com.garci.pokegarci.util.NavAnimations.navigateWithSlide
import com.garci.pokegarci.databinding.ActivityLanguageBinding
import com.garci.pokegarci.util.AppConstants
import com.garci.pokegarci.util.playClickEmeraldSound
import com.garci.pokegarci.utils.LocaleManager
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageFragment : Fragment() {

    private var _binding: ActivityLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ActivityLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSpanish.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            changeLanguage("es")
        }

        binding.btnEnglish.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            changeLanguage("en")
        }
    }

    private fun changeLanguage(language: String) {
        val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val oldLanguage = LocaleManager.getLanguage(requireContext())
        prefs.edit().putString(AppConstants.OLD_LANGUAGE_KEY, oldLanguage).apply()
        LocaleManager.saveLanguage(requireContext(), language)

        findNavController().navigateWithSlide(R.id.firstMenuFragment) {
            setPopUpTo(R.id.nav_graph, true)
        }
        requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
