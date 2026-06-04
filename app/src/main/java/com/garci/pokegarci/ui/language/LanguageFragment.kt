package com.garci.pokegarci.ui.language

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.ActivityLanguageBinding
import com.garci.pokegarci.databinding.IncludeLanguageOptionCardBinding
import com.garci.pokegarci.util.AppConstants
import com.garci.pokegarci.util.NavAnimations.navigateWithSlide
import com.garci.pokegarci.util.playClickEmeraldSound
import com.garci.pokegarci.util.setupAppTopBar
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

        bindLanguageOption(
            option = binding.btnSpanish,
            flagRes = R.drawable.flag_spain,
            labelRes = R.string.spanishLanguageButton,
            languageCode = "es",
        )
        bindLanguageOption(
            option = binding.btnEnglish,
            flagRes = R.drawable.flag_united_kingdom,
            labelRes = R.string.englishLanguageButton,
            languageCode = "en",
        )
        bindLanguageOption(
            option = binding.btnGerman,
            flagRes = R.drawable.flag_germany,
            labelRes = R.string.germanLanguageButton,
            languageCode = "de",
        )
        bindLanguageOption(
            option = binding.btnFrench,
            flagRes = R.drawable.flag_france,
            labelRes = R.string.frenchLanguageButton,
            languageCode = "fr",
        )

        setupAppTopBar(
            topBar = binding.appTopBarInclude,
            title = getString(R.string.settingsMainMenuTitle),
            insetHost = binding.languageLayout,
        )
    }

    private fun bindLanguageOption(
        option: IncludeLanguageOptionCardBinding,
        @DrawableRes flagRes: Int,
        @StringRes labelRes: Int,
        languageCode: String,
    ) {
        option.languageOptionFlag.setImageResource(flagRes)
        option.languageOptionLabel.setText(labelRes)
        option.root.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            changeLanguage(languageCode)
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
