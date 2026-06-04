package com.garci.pokegarci.ui.size

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.ActivitySizeBinding
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.presentation.size.SizeViewModel
import com.garci.pokegarci.ui.adapter.PokemonDialogAdapter
import com.garci.pokegarci.util.DataLoadingUi
import com.garci.pokegarci.util.SearchViewUtils
import com.garci.pokegarci.util.TypeBackgroundProvider
import com.garci.pokegarci.util.playClickEmeraldSound
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class SizeFragment : Fragment() {

    private val viewModel: SizeViewModel by viewModels()
    private var _binding: ActivitySizeBinding? = null
    private val binding get() = _binding!!

    private lateinit var pokemonList: List<Pokemon>
    private lateinit var firstSelectedPokemon: Pokemon
    private lateinit var secondSelectedPokemon: Pokemon

    private var dX = 0f
    private var dY = 0f
    private var originalX1 = 0f
    private var originalY1 = 0f
    private var originalX2 = 0f
    private var originalY2 = 0f

    private var lastSelectedPosition1: Int = 0
    private var lastSelectedPosition2: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.changePkmn1Size.isEnabled = false
        binding.changePkmn2Size.isEnabled = false

        binding.pokemon1Shape.post {
            originalX1 = binding.pokemon1Shape.x
            originalY1 = binding.pokemon1Shape.y
        }

        binding.pokemon2Shape.post {
            originalX2 = binding.pokemon2Shape.x
            originalY2 = binding.pokemon2Shape.y
        }
        enableDrag(binding.pokemon1Shape)
        enableDrag(binding.pokemon2Shape)

        binding.resetImagesPositionSize.setOnClickListener {
            binding.pokemon1Shape.animate().x(originalX1).y(originalY1).setDuration(200).start()
            binding.pokemon2Shape.animate().x(originalX2).y(originalY2).setDuration(200).start()

            val tallestPokemon = if (binding.pokemon1Shape.height > binding.pokemon2Shape.height)
                binding.pokemon1Shape
            else
                binding.pokemon2Shape

            val shortestPokemon = if (binding.pokemon1Shape.height > binding.pokemon2Shape.height)
                binding.pokemon2Shape
            else
                binding.pokemon1Shape

            val tallestOriginalY = if (tallestPokemon == binding.pokemon1Shape) originalY1 else originalY2
            val tallestHeight = tallestPokemon.height
            val shortestHeight = shortestPokemon.height
            val shortestTargetY = tallestOriginalY + (tallestHeight - shortestHeight) / 2

            shortestPokemon.animate().y(shortestTargetY).setDuration(200).start()
        }

        DataLoadingUi.bind(
            lifecycleOwner = viewLifecycleOwner,
            dataUiState = viewModel.dataUiState,
            views = DataLoadingUi.Views(
                progressBar = binding.progressBarSize,
                errorText = binding.dataErrorText,
                retryButton = binding.dataRetryButton,
                contentViews = listOf(
                    binding.sizeInstructions,
                    binding.sizeBox,
                    binding.changePkmn1Size,
                    binding.changePkmn2Size
                ),
                extraProgressBars = listOf(binding.progressBarSize2)
            ),
            onRetry = { viewModel.retryLoad() },
            onLoaded = {
                binding.changePkmn1Size.isEnabled = true
                binding.changePkmn2Size.isEnabled = true
                viewModel.refreshPokemonList()
            }
        )

        pokemonList = emptyList()
        observeViewModel()

        binding.changePkmn1Size.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            showPokemonSelectorDialog(true) { selectedPokemon ->
                firstSelectedPokemon = selectedPokemon
                if (::secondSelectedPokemon.isInitialized) updateSizeComparison()
            }
        }

        binding.changePkmn2Size.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            showPokemonSelectorDialog(false) { selectedPokemon ->
                secondSelectedPokemon = selectedPokemon
                if (::firstSelectedPokemon.isInitialized) updateSizeComparison()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pokemonList.collect { loadedPokemon ->
                    pokemonList = loadedPokemon
                    if (loadedPokemon.isNotEmpty() && !::firstSelectedPokemon.isInitialized)
                        initializeDefaultPokemon(loadedPokemon)
                }
            }
        }
    }

    private fun showPokemonSelectorDialog(isFirstButton: Boolean, onPokemonSelected: (Pokemon) -> Unit) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_size_selector)

        val searchViewSize = dialog.findViewById<SearchView>(R.id.searchViewSize)
        val recyclerPokemon = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerPokemon)

        SearchViewUtils.applyDefaultStyle(searchViewSize)
        SearchViewUtils.hideCursorOnFocus(searchViewSize)

        val adapter = PokemonDialogAdapter(pokemonList) { selectedPokemon ->
            requireContext().vibrate()
            if (isFirstButton) {
                lastSelectedPosition1 = pokemonList.indexOf(selectedPokemon)
                if (lastSelectedPosition1 > 2) lastSelectedPosition1 += 2
            } else {
                lastSelectedPosition2 = pokemonList.indexOf(selectedPokemon)
                if (lastSelectedPosition2 > 2) lastSelectedPosition2 += 2
            }
            onPokemonSelected(selectedPokemon)
            dialog.dismiss()
        }

        recyclerPokemon.layoutManager = LinearLayoutManager(requireContext())
        recyclerPokemon.adapter = adapter

        val scrollToPosition = if (isFirstButton) lastSelectedPosition1 else lastSelectedPosition2
        recyclerPokemon.post {
            recyclerPokemon.scrollToPosition(scrollToPosition)
        }

        searchViewSize.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().lowercase()
                val filteredList = pokemonList.filter { it.name.lowercase().contains(query) }
                adapter.updateList(filteredList)
                return true
            }
        })

        dialog.show()
    }

    private fun updateSizeComparison() {
        if (!::firstSelectedPokemon.isInitialized || !::secondSelectedPokemon.isInitialized) return

        binding.changePkmn1SizeText.text = firstSelectedPokemon.name.uppercase()
        binding.changePkmn2SizeText.text = secondSelectedPokemon.name.uppercase()
        binding.typeChangePkmn1Size.background = TypeBackgroundProvider.createBackground(
            firstSelectedPokemon.type1,
            firstSelectedPokemon.type2,
        )
        binding.typeChangePkmn2Size.background = TypeBackgroundProvider.createBackground(
            secondSelectedPokemon.type1,
            secondSelectedPokemon.type2,
        )

        val height1 = firstSelectedPokemon.height.toFloat()
        val height2 = secondSelectedPokemon.height.toFloat()
        val baseHeightInPixels = 600f
        val maxHeight = maxOf(height1, height2)
        val minHeight = minOf(height1, height2)
        val scaleFactor = if (height1 == height2) 1f else minHeight / maxHeight

        if (height1 > height2) {
            binding.pokemon1Shape.layoutParams.height = baseHeightInPixels.toInt()
            binding.pokemon2Shape.layoutParams.height = (baseHeightInPixels * scaleFactor).toInt()
        } else {
            binding.pokemon1Shape.layoutParams.height = (baseHeightInPixels * scaleFactor).toInt()
            binding.pokemon2Shape.layoutParams.height = baseHeightInPixels.toInt()
        }

        val tallestPokemon = if (height1 > height2) binding.pokemon1Shape else binding.pokemon2Shape
        val shortestPokemon = if (height1 > height2) binding.pokemon2Shape else binding.pokemon1Shape

        val tallestY = tallestPokemon.y
        val tallestHeight = tallestPokemon.height
        val shortestHeight = shortestPokemon.height
        shortestPokemon.y = tallestY + (tallestHeight - shortestHeight) / 2

        Glide.with(this)
            .load(firstSelectedPokemon.imageUrl)
            .into(binding.pokemon1Shape)
        binding.pokemon1Shape.scaleX = -1f

        Glide.with(this)
            .load(secondSelectedPokemon.imageUrl)
            .into(binding.pokemon2Shape)

        binding.sizeHP1.text = String.format(Locale.US, "%d", firstSelectedPokemon.hp)
        binding.sizeHP2.text = String.format(Locale.US, "%d", secondSelectedPokemon.hp)
        binding.sizeAttack1.text = String.format(Locale.US, "%d", firstSelectedPokemon.attack)
        binding.sizeAttack2.text = String.format(Locale.US, "%d", secondSelectedPokemon.attack)
        binding.sizeDefense1.text = String.format(Locale.US, "%d", firstSelectedPokemon.defense)
        binding.sizeDefense2.text = String.format(Locale.US, "%d", secondSelectedPokemon.defense)
        binding.sizeSpAttack1.text = String.format(Locale.US, "%d", firstSelectedPokemon.specialAttack)
        binding.sizeSpAttack2.text = String.format(Locale.US, "%d", secondSelectedPokemon.specialAttack)
        binding.sizeSpDefense1.text = String.format(Locale.US, "%d", firstSelectedPokemon.specialDefense)
        binding.sizeSpDefense2.text = String.format(Locale.US, "%d", secondSelectedPokemon.specialDefense)
        binding.sizeSpeed1.text = String.format(Locale.US, "%d", firstSelectedPokemon.speed)
        binding.sizeSpeed2.text = String.format(Locale.US, "%d", secondSelectedPokemon.speed)

        setStatBackground(binding.sizeHP1, binding.sizeHP2, firstSelectedPokemon.hp, secondSelectedPokemon.hp)
        setStatBackground(binding.sizeAttack1, binding.sizeAttack2, firstSelectedPokemon.attack, secondSelectedPokemon.attack)
        setStatBackground(binding.sizeDefense1, binding.sizeDefense2, firstSelectedPokemon.defense, secondSelectedPokemon.defense)
        setStatBackground(binding.sizeSpAttack1, binding.sizeSpAttack2, firstSelectedPokemon.specialAttack, secondSelectedPokemon.specialAttack)
        setStatBackground(binding.sizeSpDefense1, binding.sizeSpDefense2, firstSelectedPokemon.specialDefense, secondSelectedPokemon.specialDefense)
        setStatBackground(binding.sizeSpeed1, binding.sizeSpeed2, firstSelectedPokemon.speed, secondSelectedPokemon.speed)

        binding.pokemon1Shape.requestLayout()
        binding.pokemon2Shape.requestLayout()
    }

    private fun initializeDefaultPokemon(pokemonList: List<Pokemon>) {
        val bulbasaur = pokemonList.find { it.id == 1 }
        if (bulbasaur != null) {
            firstSelectedPokemon = bulbasaur
            secondSelectedPokemon = bulbasaur
            updateSizeComparison()
        }
    }

    private fun setStatBackground(textView1: TextView, textView2: TextView, value1: Int, value2: Int) {
        val green = ContextCompat.getColor(textView1.context, R.color.green_goodStat)
        val red = ContextCompat.getColor(textView1.context, R.color.red_badStat)
        val white = ContextCompat.getColor(textView1.context, R.color.white)

        when {
            value1 > value2 -> {
                textView1.setBackgroundColor(green)
                textView2.setBackgroundColor(red)
            }
            value1 < value2 -> {
                textView1.setBackgroundColor(red)
                textView2.setBackgroundColor(green)
            }
            else -> {
                textView1.setBackgroundColor(white)
                textView2.setBackgroundColor(white)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(imageView: ImageView) {
        imageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }
            true
        }
    }
}