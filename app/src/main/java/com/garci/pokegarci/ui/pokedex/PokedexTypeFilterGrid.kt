package com.garci.pokegarci.ui.pokedex

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.ItemPokedexFilterOptionBinding
import com.garci.pokegarci.domain.pokedex.PokemonTypes
import com.garci.pokegarci.util.getTypeDisplayName
import com.garci.pokegarci.util.typeIconMap

class PokedexTypeFilterGrid(
    private val context: Context,
    private val container: LinearLayout,
    private val selection: PokedexTypeFilterSelection,
    private val onSelectionChanged: () -> Unit,
) {

    private val typeKeys = PokemonTypes.ALL.sorted()
    private val leftColumnTypes = typeKeys.take(9)
    private val rightColumnTypes = typeKeys.drop(9)

    private var monotypeBinding: ItemPokedexFilterOptionBinding? = null
    private var allTypesBinding: ItemPokedexFilterOptionBinding? = null
    private val typeBindings = mutableMapOf<String, ItemPokedexFilterOptionBinding>()

    fun bind() {
        container.removeAllViews()
        bindHeaderRow()
        bindTypeRows()
        refreshChecks()
    }

    private fun bindHeaderRow() {
        val row = horizontalRow()
        allTypesBinding = inflateOption(row, weight = 1f, marginEndDp = 4)
        bindOption(
            binding = allTypesBinding!!,
            label = context.getString(R.string.pokedex_filter_all_types),
            iconRes = null,
            checked = selection.allTypesToggleSelected,
        ) {
            selection.onAllTypesToggleClicked()
            refreshChecks()
            refreshMonotypeCheck()
            onSelectionChanged()
        }

        monotypeBinding = inflateOption(row, weight = 1f, marginStartDp = 4)
        bindOption(
            binding = monotypeBinding!!,
            label = context.getString(R.string.pokedex_filter_monotype),
            iconRes = null,
            checked = selection.monotypeSelected,
        ) {
            selection.monotypeSelected = !selection.monotypeSelected
            refreshMonotypeCheck()
            onSelectionChanged()
        }
        container.addView(row)
    }

    private fun bindTypeRows() {
        for (index in 0 until 9) {
            val row = horizontalRow()
            val leftKey = leftColumnTypes[index]
            val rightKey = rightColumnTypes[index]

            val leftBinding = inflateOption(row, weight = 1f, marginEndDp = 4)
            typeBindings[leftKey] = leftBinding
            bindTypeOption(leftBinding, leftKey)

            val rightBinding = inflateOption(row, weight = 1f, marginStartDp = 4)
            typeBindings[rightKey] = rightBinding
            bindTypeOption(rightBinding, rightKey)

            container.addView(row)
        }
    }

    private fun bindTypeOption(binding: ItemPokedexFilterOptionBinding, typeKey: String) {
        bindOption(
            binding = binding,
            label = context.getTypeDisplayName(typeKey),
            iconRes = typeIconMap[typeKey],
            checked = typeKey in selection.selectedTypes,
        ) {
            selection.onTypeToggleClicked(typeKey)
            refreshChecks()
            onSelectionChanged()
        }
    }

    private fun bindOption(
        binding: ItemPokedexFilterOptionBinding,
        label: String,
        iconRes: Int?,
        checked: Boolean,
        onClick: () -> Unit,
    ) {
        binding.pokedexFilterOptionLabel.text = label
        binding.pokedexFilterOptionCheck.isChecked = checked
        if (iconRes != null) {
            binding.pokedexFilterOptionIcon.visibility = View.VISIBLE
            binding.pokedexFilterOptionIcon.setImageResource(iconRes)
        } else {
            binding.pokedexFilterOptionIcon.visibility = View.GONE
        }
        binding.pokedexFilterOptionCheck.isClickable = false
        binding.pokedexFilterOptionCheck.isFocusable = false
        binding.root.setOnClickListener {
            onClick()
        }
    }

    private fun refreshChecks() {
        allTypesBinding?.pokedexFilterOptionCheck?.isChecked = selection.allTypesToggleSelected
        typeBindings.forEach { (typeKey, binding) ->
            binding.pokedexFilterOptionCheck.isChecked = typeKey in selection.selectedTypes
        }
        refreshMonotypeCheck()
    }

    private fun refreshMonotypeCheck() {
        monotypeBinding?.pokedexFilterOptionCheck?.isChecked = selection.monotypeSelected
    }

    private fun horizontalRow(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun inflateOption(
        parent: LinearLayout,
        weight: Float? = null,
        width: Int = 0,
        marginStartDp: Int = 0,
        marginEndDp: Int = 0,
    ): ItemPokedexFilterOptionBinding {
        val binding = ItemPokedexFilterOptionBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false,
        )
        val density = context.resources.displayMetrics.density
        val params = if (weight != null) {
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
        } else {
            LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        params.marginStart = (marginStartDp * density).toInt()
        params.marginEnd = (marginEndDp * density).toInt()
        binding.root.layoutParams = params
        parent.addView(binding.root)
        return binding
    }
}
