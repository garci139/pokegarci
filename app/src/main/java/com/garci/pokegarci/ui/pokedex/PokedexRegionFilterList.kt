package com.garci.pokegarci.ui.pokedex

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.garci.pokegarci.databinding.ItemPokedexFilterRegionOptionBinding
import com.garci.pokegarci.domain.guess.PokemonGeneration
import com.garci.pokegarci.util.RegionGenerationColors

class PokedexRegionFilterList(
    private val context: Context,
    private val container: LinearLayout,
    val selectedIds: MutableSet<String>,
    private val labelProvider: (PokemonGeneration) -> String,
    private val onSelectionChanged: (Set<String>) -> Unit,
) {

    private val rowBindings = mutableListOf<ItemPokedexFilterRegionOptionBinding>()

    fun bind() {
        container.removeAllViews()
        rowBindings.clear()
        PokemonGeneration.entries.forEach { generation ->
            val binding = ItemPokedexFilterRegionOptionBinding.inflate(
                LayoutInflater.from(context),
                container,
                true,
            )
            rowBindings += binding
            binding.pokedexFilterOptionLabel.text = labelProvider(generation)
            binding.root.setOnClickListener { toggle(generation) }
            refreshRow(generation, binding)
        }
    }

    private fun toggle(generation: PokemonGeneration) {
        if (generation.name in selectedIds) {
            selectedIds.remove(generation.name)
        } else {
            selectedIds.add(generation.name)
        }
        onSelectionChanged(selectedIds)
        rowBindings.forEachIndexed { index, binding ->
            refreshRow(PokemonGeneration.entries[index], binding)
        }
    }

    private fun refreshRow(
        generation: PokemonGeneration,
        binding: ItemPokedexFilterRegionOptionBinding,
    ) {
        val selected = generation.name in selectedIds
        binding.regionFilterIndicator.regionColors = RegionGenerationColors.colorsFor(generation)
        binding.regionFilterIndicator.isRegionSelected = selected
    }
}
