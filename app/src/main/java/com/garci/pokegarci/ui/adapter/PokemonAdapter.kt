package com.garci.pokegarci.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.garci.pokegarci.R
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.util.TypeBackgroundProvider
import com.garci.pokegarci.util.typeIconMap

class PokemonAdapter(
    private val onItemClick: (Pokemon) -> Unit,
) : ListAdapter<Pokemon, PokemonAdapter.PokemonViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.each_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = getItem(position)

        holder.textPokemonName.text = pokemon.name.ifEmpty { "???" }
        holder.textPokemonId.text = if (pokemon.id > 0) {
            String.format("#%03d", pokemon.id)
        } else {
            "#???"
        }
        holder.textPokemonId.visibility = View.VISIBLE

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        Glide.with(holder.itemView.context)
            .load(pokemon.imageUrl)
            .apply(requestOptions)
            .into(holder.imagePokemon)

        bindTypeIcon(holder.firstTypeIcon, pokemon.type1)
        bindTypeIcon(holder.secondTypeIcon, pokemon.type2)

        val subView = holder.itemView.findViewById<View>(R.id.subView)
        subView.setBackgroundColor(Color.TRANSPARENT)
        subView.background = TypeBackgroundProvider.createBackground(pokemon.type1, pokemon.type2)

        holder.itemView.setOnClickListener { onItemClick(pokemon) }
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

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagePokemon: ImageView = view.findViewById(R.id.imagePokemon)
        val textPokemonName: TextView = view.findViewById(R.id.textPokemonName)
        val textPokemonId: TextView = view.findViewById(R.id.eachPokemonId)
        val firstTypeIcon: ImageView = view.findViewById(R.id.firstTypeIcon)
        val secondTypeIcon: ImageView = view.findViewById(R.id.secondTypeIcon)
    }

    private object DiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem == newItem
        }
    }
}
