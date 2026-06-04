package com.garci.pokegarci.ui.pokedex

import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.DialogPokedexFilterPopupBinding
import com.garci.pokegarci.util.playClickEmeraldSound

class PokedexFilterDialogHost(
    private val fragment: Fragment,
) {

    fun show(
        @StringRes titleRes: Int,
        @StringRes hintRes: Int,
        onSetup: (DialogPokedexFilterPopupBinding, setApplyEnabled: (Boolean) -> Unit) -> Unit,
        onApply: () -> Unit,
    ) {
        val binding = DialogPokedexFilterPopupBinding.inflate(
            LayoutInflater.from(fragment.requireContext()),
        )
        binding.pokedexFilterDialogTitle.setText(titleRes)
        binding.pokedexFilterDialogHint.setText(hintRes)

        val dialog = Dialog(fragment.requireContext(), R.style.Theme_PokeGarci_FilterDialog)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        val dismiss = { dialog.dismiss() }
        onSetup(binding) { enabled ->
            binding.pokedexFilterApplyButton.isEnabled = enabled
        }
        binding.pokedexFilterApplyButton.setOnClickListener {
            onApply()
            dismiss()
        }
        binding.pokedexFilterCloseButton.setOnClickListener {
            fragment.requireContext().playClickEmeraldSound()
            dismiss()
        }
        dialog.show()
    }
}
