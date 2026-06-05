package com.garci.pokegarci.ui.pokedex

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.ViewTreeObserver
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
import com.garci.pokegarci.domain.model.hasShinySprites
import com.garci.pokegarci.domain.model.spriteUrl
import com.garci.pokegarci.presentation.pokedex.PokedexViewModel
import com.garci.pokegarci.ui.adapter.PokemonAdapter
import com.garci.pokegarci.util.DataLoadingUi
import com.garci.pokegarci.util.GridSpacingItemDecoration
import com.garci.pokegarci.util.PokemonCryPlayer
import com.garci.pokegarci.util.setupAppTopBar
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
    private var showingShinySprites = false
    private lateinit var filterUi: PokedexFilterUi
    private var pendingScrollPokedexToTop = false
    private var displayedPokemon: List<Pokemon> = emptyList()
    private var expandedCardSlideInProgress = false

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
        if (binding.pokedexBox.itemDecorationCount == 0) {
            binding.pokedexBox.addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = 3,
                    spacingPx = resources.getDimensionPixelSize(R.dimen.pokedex_card_spacing)
                ),
            )
        }

        filterUi = PokedexFilterUi(
            fragment = this,
            binding = binding,
            viewModel = viewModel,
            onFiltersCleared = ::requestPokedexScrollToTop,
        )
        filterUi.setup()

        setupAppTopBar(
            topBar = binding.appTopBarInclude,
            title = getString(R.string.pokedex_title),
            insetHost = binding.pokedexLayout,
        ) {
            if (binding.expandedPokemonCard.visibility != View.VISIBLE) return@setupAppTopBar false
            if (expandedCardSlideInProgress) return@setupAppTopBar true
            closeExpandedCard()
            true
        }
        binding.pokedexLayout.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.pokedexLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    applyPokedexListInsets()
                }
            },
        )
        binding.pokedexLayout.post { applyPokedexListInsets() }

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
            if (expandedCardSlideInProgress) return@setOnClickListener
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            closeExpandedCard()
        }

        binding.expandedCardPrevButton.setOnClickListener {
            navigateExpandedCard(-1)
        }
        binding.expandedCardNextButton.setOnClickListener {
            navigateExpandedCard(1)
        }

        setupExpandedCardSwipe()

        val spriteFlipClickListener = View.OnClickListener {
            if (expandedCardSlideInProgress) return@OnClickListener
            expandedCardPokemon?.let { pokemon ->
                if (pokemon.backImageUrl.isNotBlank() && flipExpandedSprite()) {
                    requireContext().vibrate()
                    requireContext().playClickEmeraldSound()
                }
            }
        }
        binding.expandedSubView.setOnClickListener(spriteFlipClickListener)
        binding.expandedPokemonImage.setOnClickListener(spriteFlipClickListener)

        binding.expandedShinyToggleButton.setOnClickListener {
            if (expandedCardSlideInProgress) return@setOnClickListener
            val pokemon = expandedCardPokemon ?: return@setOnClickListener
            if (!pokemon.hasShinySprites()) return@setOnClickListener
            if (PokemonSpriteFlipAnimator.isFlipInProgress(binding.expandedPokemonImage)) return@setOnClickListener
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            showingShinySprites = !showingShinySprites
            loadExpandedSpriteImage()
            updateShinyToggleUi()
        }

        DataLoadingUi.bind(
            lifecycleOwner = viewLifecycleOwner,
            dataUiState = viewModel.dataUiState,
            views = DataLoadingUi.Views(
                progressBar = binding.progressBar,
                errorText = binding.dataErrorText,
                retryButton = binding.dataRetryButton,
                contentViews = listOf(
                    binding.pokedexHeader,
                    binding.pokedexBox,
                )
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
                    displayedPokemon = pokemon
                    adapter.submitList(pokemon) {
                        if (pendingScrollPokedexToTop) {
                            pendingScrollPokedexToTop = false
                            scrollPokedexListToTop()
                        }
                    }
                    if (pendingScrollPokedexToTop) {
                        binding.pokedexBox.post {
                            if (pendingScrollPokedexToTop) {
                                pendingScrollPokedexToTop = false
                                scrollPokedexListToTop()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun applyPokedexListInsets() {
        val topInset = binding.pokedexListTopAnchor.bottom - binding.pokedexBox.top
        val bottomInset = resources.getDimensionPixelSize(R.dimen.pokedex_list_bottom_inset)
        if (topInset > 0 &&
            (binding.pokedexBox.paddingTop != topInset || binding.pokedexBox.paddingBottom != bottomInset)
        ) {
            binding.pokedexBox.setPadding(0, topInset, 0, bottomInset)
        }
    }

    private fun requestPokedexScrollToTop() {
        pendingScrollPokedexToTop = true
    }

    private fun scrollPokedexListToTop() {
        val recycler = binding.pokedexBox
        recycler.stopScroll()
        val layoutManager = recycler.layoutManager as? GridLayoutManager ?: return
        layoutManager.scrollToPositionWithOffset(0, recycler.paddingTop)
    }

    @SuppressLint("DefaultLocale")
    private fun showExpandedCard(pokemon: Pokemon) {
        binding.expandedPokemonCard.translationX = 0f
        bindExpandedCardContent(pokemon)
        updateExpandedNavButtons()

        binding.pokedexBox.isEnabled = false
        val inputMethodManager =
            requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.pokedexBox.windowToken, 0)

        binding.disableRecyclerView.visibility = View.VISIBLE
        binding.expandedPokemonCard.visibility = View.VISIBLE
        binding.expandedPokemonCard.bringToFront()
    }

    @SuppressLint("DefaultLocale")
    private fun bindExpandedCardContent(pokemon: Pokemon) {
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
        showingShinySprites = false
        PokemonSpriteFlipAnimator.reset(binding.expandedPokemonImage)

        loadExpandedSpriteImage()
        preloadExpandedSpriteVariants(pokemon)
        updateShinyToggleUi()

        bindTypeIcon(binding.expandedFirstTypeIcon, pokemon.type1)
        bindTypeIcon(binding.expandedSecondTypeIcon, pokemon.type2)

        binding.expandedSubView.background = TypeBackgroundProvider.createBackground(pokemon.type1, pokemon.type2)

        cryPlayer.play(pokemon.legacyCryUrl)
    }

    private fun setupExpandedCardSwipe() {
        binding.expandedPokemonCard.onSwipe = { direction -> navigateExpandedCard(direction) }
        binding.expandedPokemonCard.swipeEnabled = {
            binding.expandedPokemonCard.visibility == View.VISIBLE && !expandedCardSlideInProgress
        }
    }

    private fun navigateExpandedCard(direction: Int) {
        if (expandedCardSlideInProgress) return
        val current = expandedCardPokemon ?: return
        val list = displayedPokemon
        val index = list.indexOfFirst { it.id == current.id }
        if (index < 0) return
        val targetIndex = index + direction
        if (targetIndex !in list.indices) return

        requireContext().vibrate()
        requireContext().playClickEmeraldSound()

        expandedCardSlideInProgress = true
        setExpandedCardInteractionEnabled(false)

        val targetPokemon = list[targetIndex]
        PokedexExpandedCardSlideAnimator.slide(
            card = binding.expandedPokemonCard,
            direction = direction,
            onSwapContent = { bindExpandedCardContent(targetPokemon) },
            onComplete = {
                expandedCardSlideInProgress = false
                updateExpandedNavButtons()
                setExpandedCardInteractionEnabled(true)
            },
        )
    }

    private fun updateExpandedNavButtons() {
        val current = expandedCardPokemon
        if (current == null) {
            binding.expandedCardPrevButton.visibility = View.GONE
            binding.expandedCardNextButton.visibility = View.GONE
            binding.expandedCardNavDividerStart.visibility = View.GONE
            binding.expandedCardNavDividerEnd.visibility = View.GONE
            return
        }
        val index = displayedPokemon.indexOfFirst { it.id == current.id }
        val showPrev = index > 0
        val showNext = index >= 0 && index < displayedPokemon.lastIndex
        binding.expandedCardPrevButton.visibility = if (showPrev) View.VISIBLE else View.GONE
        binding.expandedCardNextButton.visibility = if (showNext) View.VISIBLE else View.GONE
        binding.expandedCardNavDividerStart.visibility = if (showPrev) View.VISIBLE else View.GONE
        binding.expandedCardNavDividerEnd.visibility = if (showNext) View.VISIBLE else View.GONE
    }

    private fun setExpandedCardInteractionEnabled(enabled: Boolean) {
        val blockDuringSlide = !enabled || expandedCardSlideInProgress
        binding.closeCardButton.isEnabled = !blockDuringSlide
        binding.closeCardButton.isClickable = !blockDuringSlide
        binding.expandedCardPrevButton.isEnabled = !blockDuringSlide
        binding.expandedCardNextButton.isEnabled = !blockDuringSlide
        binding.expandedSubView.isEnabled = !blockDuringSlide
        binding.expandedPokemonImage.isEnabled = !blockDuringSlide
        if (blockDuringSlide) {
            binding.expandedShinyToggleButton.isEnabled = false
        } else {
            updateShinyToggleUi()
        }
    }

    private fun flipExpandedSprite(): Boolean {
        if (expandedCardSlideInProgress) return false
        val pokemon = expandedCardPokemon ?: return false
        val started = PokemonSpriteFlipAnimator.toggle(
            imageView = binding.expandedPokemonImage,
            showingBack = showingBackSprite,
            frontUrl = pokemon.spriteUrl(showingBack = false, shiny = showingShinySprites),
            backUrl = pokemon.spriteUrl(showingBack = true, shiny = showingShinySprites),
            onComplete = {
                showingBackSprite = it
                updateShinyToggleUi()
            }
        )
        if (started) updateShinyToggleUi()
        return started
    }

    private fun loadExpandedSpriteImage() {
        val pokemon = expandedCardPokemon ?: return
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(this)
            .load(pokemon.spriteUrl(showingBack = showingBackSprite, shiny = showingShinySprites))
            .apply(requestOptions)
            .into(binding.expandedPokemonImage)
    }

    private fun preloadExpandedSpriteVariants(pokemon: Pokemon) {
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        listOf(
            pokemon.spriteUrl(showingBack = false, shiny = false),
            pokemon.spriteUrl(showingBack = true, shiny = false),
            pokemon.spriteUrl(showingBack = false, shiny = true),
            pokemon.spriteUrl(showingBack = true, shiny = true),
        )
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { url ->
                Glide.with(this).load(url).apply(requestOptions).preload()
            }
    }

    private fun updateShinyToggleUi() {
        val pokemon = expandedCardPokemon
        val hasShiny = pokemon?.hasShinySprites() == true
        val flipInProgress = PokemonSpriteFlipAnimator.isFlipInProgress(binding.expandedPokemonImage)

        binding.expandedShinyToggleDisabledStrike.visibility =
            if (hasShiny) View.GONE else View.VISIBLE
        binding.expandedShinyToggleButton.isEnabled = hasShiny && !flipInProgress
        binding.expandedShinyToggleButton.isSelected = hasShiny && showingShinySprites
        binding.expandedShinyToggleButton.refreshDrawableState()
        binding.expandedShinyToggleButton.alpha = if (hasShiny) 1f else 0.55f
    }

    private fun closeExpandedCard() {
        binding.expandedPokemonCard.animate().cancel()
        binding.expandedPokemonCard.translationX = 0f
        cryPlayer.stop()
        expandedCardPokemon = null
        expandedCardSlideInProgress = false
        showingBackSprite = false
        showingShinySprites = false
        PokemonSpriteFlipAnimator.reset(binding.expandedPokemonImage)
        updateShinyToggleUi()
        updateExpandedNavButtons()
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