import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.garci.pokegarci.Pokemon
import com.garci.pokegarci.R
import com.google.android.material.card.MaterialCardView
import com.garci.pokegarci.typeIconMap

class PokemonAdapter(
    private val pokemonList: List<Pokemon>,
    private val onItemClick: (Pokemon) -> Unit //Para que registre los clicks en el RecycleView
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagePokemon: ImageView = view.findViewById(R.id.imagePokemon)
        val textPokemonName: TextView = view.findViewById(R.id.textPokemonName)
        val textPokemonId: TextView = view.findViewById(R.id.eachPokemonId)
        val firstTypeIcon: ImageView = view.findViewById(R.id.firstTypeIcon)
        val secondTypeIcon: ImageView = view.findViewById(R.id.secondTypeIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.each_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position] // Elegimos el pokemon concreto

        // Verificacion de que llegan los IDs
        println("Asignando ID: ${pokemon.id} a ${pokemon.name}")

        holder.textPokemonName.text = pokemon.name.ifEmpty { "???" }
        holder.textPokemonId.text = if (pokemon.id > 0) {
            String.format("#%03d", pokemon.id)
        } else { "#???" }
        holder.textPokemonId.visibility = View.VISIBLE

        // Cargar imagen con Glide
        Glide.with(holder.itemView.context)
            .load(pokemon.imageUrl)
            .into(holder.imagePokemon)

        // Añadir el primer tipo, si lo hay
        val firstTypeIconRes = typeIconMap[pokemon.type1]
        if (firstTypeIconRes != null) {
            holder.firstTypeIcon.setImageResource(firstTypeIconRes)
            holder.firstTypeIcon.visibility = View.VISIBLE
        } else {
            holder.firstTypeIcon.visibility = View.GONE
        }

        // Añadir el segundo tipo, si lo hay
        val secondTypeIconRes = typeIconMap[pokemon.type2]
        if (secondTypeIconRes != null) {
            holder.secondTypeIcon.setImageResource(secondTypeIconRes)
            holder.secondTypeIcon.visibility = View.VISIBLE
        } else {
            holder.secondTypeIcon.visibility = View.GONE
        }

        // Obtener el View interior y aplicar el fondo
        val subView = holder.itemView.findViewById<View>(R.id.subView)
        subView.setBackgroundColor(Color.TRANSPARENT) // Resetear el fondo
        subView.background = null // Evitar que conserve el anterior

        // Clonar drawable antes de asignarlo para evitar que RecyclerView lo pierda
        val safeView = pokemon.backgroundDrawable?.constantState?.newDrawable()?.mutate()

        if (safeView != null) {
            subView.background = safeView
        } else {
            subView.setBackgroundColor(Color.GRAY) // Gris en caso de error
        }

        // Para que llame al listener al detectar un click en un pokemon
        holder.itemView.setOnClickListener {
            onItemClick(pokemon)
        }
    }

    override fun getItemCount(): Int = pokemonList.size

}
