package com.garci.pokegarci.ui.pokedex

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.ActivityPokedexBinding
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.model.abilitiesDisplayText
import com.garci.pokegarci.presentation.pokedex.PokedexViewModel
import com.garci.pokegarci.ui.adapter.PokemonAdapter
import com.garci.pokegarci.util.DataLoadingUi
import com.garci.pokegarci.util.PokemonCryPlayer
import com.garci.pokegarci.util.PokemonSpriteFlipAnimator
import com.garci.pokegarci.util.SearchViewUtils
import com.garci.pokegarci.util.TypeBackgroundProvider
import com.garci.pokegarci.util.playClickEmeraldSound
import com.garci.pokegarci.util.typeIconMap
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class PokedexFragment : Fragment() {

    private val viewModel: PokedexViewModel by viewModels()
    private var _binding: ActivityPokedexBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PokemonAdapter
    private lateinit var cryPlayer: PokemonCryPlayer
    private var expandedCardPokemon: Pokemon? = null
    private var showingBackSprite = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityPokedexBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cryPlayer = PokemonCryPlayer(requireContext())

        binding.searchView.queryHint = getString(R.string.queryHintSearchView)
        SearchViewUtils.applyDefaultStyle(binding.searchView)
        SearchViewUtils.hideCursorOnFocus(binding.searchView)

        adapter = PokemonAdapter { pokemon ->
            requireContext().vibrate()
            showExpandedCard(pokemon)
        }
        binding.pokedexBox.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.pokedexBox.adapter = adapter
        binding.pokedexBox.isNestedScrollingEnabled = false

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.updateSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText.orEmpty())
                return true
            }
        })

        binding.closeCardButton.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            closeExpandedCard()
        }

        val spriteFlipClickListener = View.OnClickListener {
            expandedCardPokemon?.let { pokemon ->
                if (pokemon.backImageUrl.isNotBlank() && flipExpandedSprite()) {
                    requireContext().vibrate()
                    requireContext().playClickEmeraldSound()
                }
            }
        }
        binding.expandedSubView.setOnClickListener(spriteFlipClickListener)
        binding.expandedPokemonImage.setOnClickListener(spriteFlipClickListener)

        DataLoadingUi.bind(
            lifecycleOwner = viewLifecycleOwner,
            dataUiState = viewModel.dataUiState,
            views = DataLoadingUi.Views(
                progressBar = binding.progressBar,
                errorText = binding.dataErrorText,
                retryButton = binding.dataRetryButton,
                contentViews = listOf(binding.searchView, binding.pokedexBox)
            ),
            onRetry = { viewModel.retryLoad() },
            onLoaded = { viewModel.refreshPokemonList() }
        )

        observeViewModel()
    }

    override fun onDestroyView() {
        cryPlayer.stop()
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredPokemon.collect { pokemon ->
                    adapter.submitList(pokemon)
                    binding.pokedexBox.scrollToPosition(0)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showExpandedCard(pokemon: Pokemon) {
        binding.expandedPokemonName.text = pokemon.name
        binding.expandedPokemonId.text = String.format("#%03d", pokemon.id)
        binding.expandedPokemonDescription.text = pokemon.description
        binding.expandedPokemonAbilities.text = pokemon.abilitiesDisplayText()
        binding.expandedPokemonHeight.text = String.format(Locale.US, "%.1f m", pokemon.height / 10.0)
        binding.expandedPokemonWeight.text = String.format(Locale.US, "%.1f kg", pokemon.weight / 10.0)

        binding.expandedPokemonHP.text = String.format(Locale.US, "%d", pokemon.hp)
        binding.expandedPokemonAttack.text = String.format(Locale.US, "%d", pokemon.attack)
        binding.expandedPokemonDefense.text = String.format(Locale.US, "%d", pokemon.defense)
        binding.expandedPokemonSpAttack.text = String.format(Locale.US, "%d", pokemon.specialAttack)
        binding.expandedPokemonSpDefense.text = String.format(Locale.US, "%d", pokemon.specialDefense)
        binding.expandedPokemonSpeed.text = String.format(Locale.US, "%d", pokemon.speed)

        expandedCardPokemon = pokemon
        showingBackSprite = false
        PokemonSpriteFlipAnimator.reset(binding.expandedPokemonImage)

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(this)
            .load(pokemon.imageUrl)
            .apply(requestOptions)
            .into(binding.expandedPokemonImage)
        if (pokemon.backImageUrl.isNotBlank()) {
            Glide.with(this)
                .load(pokemon.backImageUrl)
                .apply(requestOptions)
                .preload()
        }

        bindTypeIcon(binding.expandedFirstTypeIcon, pokemon.type1)
        bindTypeIcon(binding.expandedSecondTypeIcon, pokemon.type2)

        binding.expandedSubView.background = TypeBackgroundProvider.createBackground(pokemon.type1, pokemon.type2)

        binding.pokedexBox.isEnabled = false
        val inputMethodManager =
            requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.pokedexBox.windowToken, 0)

        binding.disableRecyclerView.visibility = View.VISIBLE
        binding.expandedPokemonCard.visibility = View.VISIBLE

        cryPlayer.play(pokemon.legacyCryUrl)
    }

    private fun flipExpandedSprite(): Boolean {
        val pokemon = expandedCardPokemon ?: return false
        return PokemonSpriteFlipAnimator.toggle(
            imageView = binding.expandedPokemonImage,
            showingBack = showingBackSprite,
            frontUrl = pokemon.imageUrl,
            backUrl = pokemon.backImageUrl,
            onComplete = { showingBackSprite = it }
        )
    }

    private fun closeExpandedCard() {
        cryPlayer.stop()
        expandedCardPokemon = null
        showingBackSprite = false
        PokemonSpriteFlipAnimator.reset(binding.expandedPokemonImage)
        binding.pokedexBox.isEnabled = true
        binding.disableRecyclerView.visibility = View.INVISIBLE
        binding.expandedPokemonCard.visibility = View.GONE
    }

    private fun bindTypeIcon(imageView: ImageView, type: String?) {
        val iconRes = type?.let { typeIconMap[it] }
        if (iconRes != null) {
            imageView.setImageResource(iconRes)
            imageView.visibility = View.VISIBLE
        } else
            imageView.visibility = View.GONE
    }
}