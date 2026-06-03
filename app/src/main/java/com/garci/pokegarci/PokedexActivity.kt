package com.garci.pokegarci

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.garci.pokegarci.databinding.ActivityPokedexBinding
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.presentation.pokedex.PokedexViewModel
import com.garci.pokegarci.ui.adapter.PokemonAdapter
import com.garci.pokegarci.util.BaseLocaleActivity
import com.garci.pokegarci.util.DataLoadingUi
import com.garci.pokegarci.util.SearchViewUtils
import com.garci.pokegarci.util.TypeBackgroundProvider
import com.garci.pokegarci.util.startGradientBackgroundAnimation
import com.garci.pokegarci.util.typeIconMap
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class PokedexActivity : BaseLocaleActivity() {

    private val viewModel: PokedexViewModel by viewModels()
    private lateinit var binding: ActivityPokedexBinding
    private lateinit var adapter: PokemonAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokedexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pokedexLayout.startGradientBackgroundAnimation()
        binding.searchView.queryHint = getString(R.string.queryHintSearchView)
        SearchViewUtils.applyDefaultStyle(binding.searchView)
        SearchViewUtils.hideCursorOnFocus(binding.searchView)

        adapter = PokemonAdapter { pokemon ->
            vibrate()
            showExpandedCard(pokemon)
        }
        binding.pokedexBox.layoutManager = GridLayoutManager(this, 3)
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
            vibrate()
            binding.pokedexBox.isEnabled = true
            binding.disableRecyclerView.visibility = View.INVISIBLE
            binding.expandedPokemonCard.visibility = View.GONE
        }

        DataLoadingUi.bind(
            lifecycleOwner = this,
            dataUiState = viewModel.dataUiState,
            views = DataLoadingUi.Views(
                progressBar = binding.progressBar,
                errorText = binding.dataErrorText,
                retryButton = binding.dataRetryButton,
                contentViews = listOf(binding.searchView, binding.pokedexBox),
            ),
            onRetry = { viewModel.retryLoad() },
            onLoaded = { viewModel.refreshPokemonList() },
        )

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        binding.expandedPokemonHeight.text = String.format(Locale.US, "%.1f m", pokemon.height / 10.0)
        binding.expandedPokemonWeight.text = String.format(Locale.US, "%.1f kg", pokemon.weight / 10.0)

        binding.expandedPokemonHP.text = String.format(Locale.US, "%d", pokemon.hp)
        binding.expandedPokemonAttack.text = String.format(Locale.US, "%d", pokemon.attack)
        binding.expandedPokemonDefense.text = String.format(Locale.US, "%d", pokemon.defense)
        binding.expandedPokemonSpAttack.text = String.format(Locale.US, "%d", pokemon.specialAttack)
        binding.expandedPokemonSpDefense.text = String.format(Locale.US, "%d", pokemon.specialDefense)
        binding.expandedPokemonSpeed.text = String.format(Locale.US, "%d", pokemon.speed)

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(this)
            .load(pokemon.imageUrl)
            .apply(requestOptions)
            .into(binding.expandedPokemonImage)

        bindTypeIcon(binding.expandedFirstTypeIcon, pokemon.type1)
        bindTypeIcon(binding.expandedSecondTypeIcon, pokemon.type2)

        binding.expandedSubView.background = TypeBackgroundProvider.createBackground(pokemon.type1, pokemon.type2)

        binding.pokedexBox.isEnabled = false
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.pokedexBox.windowToken, 0)

        binding.disableRecyclerView.visibility = View.VISIBLE
        binding.expandedPokemonCard.visibility = View.VISIBLE
    }

    private fun bindTypeIcon(imageView: ImageView, type: String?) {
        val iconRes = type?.let { typeIconMap[it] }
        if (iconRes != null) {
            imageView.setImageResource(iconRes)
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE
        }
    }
}
