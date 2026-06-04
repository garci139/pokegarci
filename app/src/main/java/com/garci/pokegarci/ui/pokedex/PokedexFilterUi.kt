package com.garci.pokegarci.ui.pokedex

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.ActivityPokedexBinding
import com.garci.pokegarci.databinding.DialogPokedexFilterPopupBinding
import com.garci.pokegarci.databinding.ItemPokedexStatFilterRowBinding
import com.garci.pokegarci.domain.guess.PokemonGeneration
import com.garci.pokegarci.domain.pokedex.PokedexStatFilter
import com.garci.pokegarci.domain.pokedex.StatThreshold
import com.garci.pokegarci.presentation.pokedex.PokedexViewModel
import com.garci.pokegarci.util.getRegionLabel
import com.garci.pokegarci.util.playClickEmeraldSound
import com.garci.pokegarci.utils.vibrate
import kotlinx.coroutines.launch

class PokedexFilterUi(
    private val fragment: Fragment,
    private val binding: ActivityPokedexBinding,
    private val viewModel: PokedexViewModel,
    private val onFiltersCleared: () -> Unit,
) {

    private val dialogHost = PokedexFilterDialogHost(fragment)

    private val regionChip: View = binding.pokedexFilters.pokedexFilterRegion.root
    private val typeChip: View = binding.pokedexFilters.pokedexFilterType.root
    private val statsChip: View = binding.pokedexFilters.pokedexFilterStats.root
    private val clearChip: View = binding.pokedexFilters.pokedexFilterClear.root

    private val regionChipLabel: TextView =
        binding.pokedexFilters.pokedexFilterRegion.pokedexFilterChipLabel
    private val typeChipLabel: TextView =
        binding.pokedexFilters.pokedexFilterType.pokedexFilterChipLabel
    private val statsChipLabel: TextView =
        binding.pokedexFilters.pokedexFilterStats.pokedexFilterChipLabel
    private val clearChipLabel: TextView =
        binding.pokedexFilters.pokedexFilterClear.pokedexFilterChipLabel

    fun setup() {
        regionChipLabel.setText(R.string.pokedex_filter_region)
        typeChipLabel.setText(R.string.pokedex_filter_type)
        statsChipLabel.setText(R.string.pokedex_filter_stats)
        clearChipLabel.setText(R.string.pokedex_filter_clear)

        regionChip.setOnClickListener {
            fragment.requireContext().vibrate()
            fragment.requireContext().playClickEmeraldSound()
            showRegionDialog()
        }
        typeChip.setOnClickListener {
            fragment.requireContext().vibrate()
            fragment.requireContext().playClickEmeraldSound()
            showTypeDialog()
        }
        statsChip.setOnClickListener {
            fragment.requireContext().vibrate()
            fragment.requireContext().playClickEmeraldSound()
            showStatsDialog()
        }

        regionChip.setOnLongClickListener { clearRegionFilter() }
        typeChip.setOnLongClickListener { clearTypeFilter() }
        statsChip.setOnLongClickListener { clearStatsFilter() }

        clearChip.setOnClickListener {
            if (!hasAnyActiveFilter()) return@setOnClickListener
            fragment.requireContext().vibrate()
            viewModel.clearAllFilters()
            onFiltersCleared()
            fragment.requireContext().playClickEmeraldSound()
        }

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeRegionFilter.collect { syncChipStates() }
            }
        }
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeTypeFilter.collect { syncChipStates() }
            }
        }
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeStatFilter.collect { syncChipStates() }
            }
        }
    }

    private fun syncChipStates() {
        regionChip.isSelected = viewModel.activeRegionFilter.value != null
        typeChip.isSelected = viewModel.activeTypeFilter.value != null
        statsChip.isSelected = viewModel.activeStatFilter.value != null
        clearChip.isSelected = false
    }

    private fun hasAnyActiveFilter(): Boolean =
        viewModel.activeRegionFilter.value != null ||
            viewModel.activeTypeFilter.value != null ||
            viewModel.activeStatFilter.value != null

    private fun clearRegionFilter(): Boolean {
        if (viewModel.activeRegionFilter.value == null) return true
        viewModel.clearRegionFilter()
        fragment.requireContext().playClickEmeraldSound()
        showClearedToast()
        return true
    }

    private fun clearTypeFilter(): Boolean {
        if (viewModel.activeTypeFilter.value == null) return true
        viewModel.clearTypeFilter()
        fragment.requireContext().playClickEmeraldSound()
        showClearedToast()
        return true
    }

    private fun clearStatsFilter(): Boolean {
        if (viewModel.activeStatFilter.value == null) return true
        viewModel.clearStatFilter()
        fragment.requireContext().playClickEmeraldSound()
        showClearedToast()
        return true
    }

    private fun showClearedToast() {
        Toast.makeText(fragment.requireContext(), R.string.pokedex_filter_cleared, Toast.LENGTH_SHORT).show()
    }

    private fun showRegionDialog() {
        val context = fragment.requireContext()
        val initialSelection = viewModel.activeRegionFilter.value
            ?.map { it.name }
            ?.toMutableSet()
            ?: mutableSetOf()
        var regionList: PokedexRegionFilterList? = null

        dialogHost.show(
            titleRes = R.string.pokedex_filter_region_title,
            hintRes = R.string.pokedex_filter_region_hint,
            onSetup = { popup, setApplyEnabled ->
                popup.pokedexFilterRegionList.visibility = View.VISIBLE
                regionList = PokedexRegionFilterList(
                    context = context,
                    container = popup.pokedexFilterRegionList,
                    selectedIds = initialSelection,
                    labelProvider = { generation -> context.getRegionLabel(generation) },
                    onSelectionChanged = { },
                ).also { it.bind() }
                setApplyEnabled(true)
            },
            onApply = {
                val selected = regionList?.selectedIds?.mapNotNull { id ->
                    PokemonGeneration.entries.find { it.name == id }
                }?.toSet() ?: return@show
                viewModel.applyRegionFilter(selected)
                fragment.requireContext().playClickEmeraldSound()
            },
        )
    }

    private fun showTypeDialog() {
        val context = fragment.requireContext()
        val selection = PokedexTypeFilterSelection.fromActiveFilter(viewModel.activeTypeFilter.value)

        dialogHost.show(
            titleRes = R.string.pokedex_filter_type_title,
            hintRes = R.string.pokedex_filter_type_hint,
            onSetup = { popup, setApplyEnabled ->
                popup.pokedexFilterTypeGrid.visibility = View.VISIBLE
                val typeGrid = PokedexTypeFilterGrid(
                    context = context,
                    container = popup.pokedexFilterTypeGrid,
                    selection = selection,
                    onSelectionChanged = {
                        setApplyEnabled(selection.isApplyEnabled())
                    },
                )
                typeGrid.bind()
                setApplyEnabled(selection.isApplyEnabled())
            },
            onApply = {
                viewModel.applyTypeFilter(selection.toFilter())
                fragment.requireContext().playClickEmeraldSound()
            },
        )
    }

    private fun showStatsDialog() {
        val context = fragment.requireContext()
        val active = viewModel.activeStatFilter.value ?: PokedexStatFilter()
        var statFields: List<ItemPokedexStatFilterRowBinding>? = null

        dialogHost.show(
            titleRes = R.string.pokedex_filter_stats_title,
            hintRes = R.string.pokedex_filter_stats_hint,
            onSetup = { popup, setApplyEnabled ->
                popup.pokedexFilterStatsScroll.visibility = View.VISIBLE
                val thresholds = listOf(
                    active.hp,
                    active.attack,
                    active.defense,
                    active.specialAttack,
                    active.specialDefense,
                    active.speed,
                )
                val statLabels = listOf(
                    R.string.HP,
                    R.string.Attack,
                    R.string.Defense,
                    R.string.SpAttack,
                    R.string.SpDefense,
                    R.string.Speed,
                )
                val rows = statLabels.mapIndexed { index, labelRes ->
                    val rowBinding = ItemPokedexStatFilterRowBinding.inflate(
                        LayoutInflater.from(context),
                        popup.pokedexFilterStatsContainer,
                        true,
                    )
                    rowBinding.pokedexStatFilterLabel.setText(labelRes)
                    val threshold = thresholds[index]
                    rowBinding.pokedexStatFilterMin.setText(threshold.min?.toString().orEmpty())
                    rowBinding.pokedexStatFilterMax.setText(threshold.max?.toString().orEmpty())
                    rowBinding
                }
                statFields = rows

                fun readFilter(): PokedexStatFilter {
                    fun readThreshold(row: ItemPokedexStatFilterRowBinding): StatThreshold {
                        val min = row.pokedexStatFilterMin.text?.toString()?.trim()?.toIntOrNull()
                        val max = row.pokedexStatFilterMax.text?.toString()?.trim()?.toIntOrNull()
                        return StatThreshold(min = min, max = max)
                    }
                    return PokedexStatFilter(
                        hp = readThreshold(rows[0]),
                        attack = readThreshold(rows[1]),
                        defense = readThreshold(rows[2]),
                        specialAttack = readThreshold(rows[3]),
                        specialDefense = readThreshold(rows[4]),
                        speed = readThreshold(rows[5]),
                    )
                }

                fun updateApplyButton() {
                    val filter = readFilter()
                    setApplyEnabled(filter.isActive() && filter.isValid())
                }

                rows.forEach { row ->
                    row.pokedexStatFilterMin.doAfterTextChanged { updateApplyButton() }
                    row.pokedexStatFilterMax.doAfterTextChanged { updateApplyButton() }
                }
                updateApplyButton()
            },
            onApply = {
                val fields = statFields ?: return@show
                fun readThreshold(row: ItemPokedexStatFilterRowBinding): StatThreshold {
                    val min = row.pokedexStatFilterMin.text?.toString()?.trim()?.toIntOrNull()
                    val max = row.pokedexStatFilterMax.text?.toString()?.trim()?.toIntOrNull()
                    return StatThreshold(min = min, max = max)
                }
                val filter = PokedexStatFilter(
                    hp = readThreshold(fields[0]),
                    attack = readThreshold(fields[1]),
                    defense = readThreshold(fields[2]),
                    specialAttack = readThreshold(fields[3]),
                    specialDefense = readThreshold(fields[4]),
                    speed = readThreshold(fields[5]),
                )
                if (!filter.isActive() || !filter.isValid()) return@show
                viewModel.applyStatFilter(filter)
                fragment.requireContext().playClickEmeraldSound()
            },
        )
    }
}
