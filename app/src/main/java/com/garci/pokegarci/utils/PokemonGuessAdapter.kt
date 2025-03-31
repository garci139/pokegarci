package com.garci.pokegarci.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.garci.pokegarci.Pokemon
import com.garci.pokegarci.R

class PokemonGuessAdapter(
    private var pokemonList: List<Pokemon>,
    private val onItemClick: (Pokemon) -> Unit
) : RecyclerView.Adapter<PokemonGuessAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.guessEachPokemonName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonGuessAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.each_pokemon_guess, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonGuessAdapter.ViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.nameTextView.text = pokemon.name.ifEmpty { "???" }
        holder.itemView.setOnClickListener { onItemClick(pokemon) }
    }

    override fun getItemCount(): Int = pokemonList.size

    fun updateList(newList: List<Pokemon>) {
        pokemonList = newList
        notifyDataSetChanged()
    }

}