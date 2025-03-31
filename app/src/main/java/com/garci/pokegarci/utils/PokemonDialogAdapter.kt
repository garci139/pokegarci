import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.garci.pokegarci.Pokemon
import com.garci.pokegarci.R
import java.util.Locale

class PokemonDialogAdapter(
    private var pokemonList: List<Pokemon>,
    private val onItemClick: (Pokemon) -> Unit
) : RecyclerView.Adapter<PokemonDialogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.pokemonName)
        val heightTextView: TextView = view.findViewById(R.id.pokemonHeight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.size_each_pokemon_selector, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.nameTextView.text = pokemon.name.ifEmpty { "???" }
        holder.heightTextView.text = String.format(Locale.US, "%.1f m", pokemon.height / 10.0) // Altura en dm

        // Obtener el View interior y aplicar el fondo
        val eachPkmnStrokeColorView = holder.itemView.findViewById<View>(R.id.eachPkmnStrokeCardViewSize)
        eachPkmnStrokeColorView.setBackgroundColor(Color.TRANSPARENT) // Resetear el fondo
        eachPkmnStrokeColorView.background = null // Evitar que conserve el anterior

        // Clonar drawable antes de asignarlo para evitar que RecyclerView lo pierda
        val safeViewSize = pokemon.backgroundDrawable.constantState?.newDrawable()?.mutate()


        if (safeViewSize != null) {
            eachPkmnStrokeColorView.background = safeViewSize
            eachPkmnStrokeColorView.invalidate()
        } else {
            eachPkmnStrokeColorView.setBackgroundColor(Color.GRAY) // Gris en caso de error
        }

        holder.itemView.setOnClickListener { onItemClick(pokemon) }
    }

    override fun getItemCount(): Int = pokemonList.size

    fun updateList(newList: List<Pokemon>) {
        pokemonList = newList
        notifyDataSetChanged()
    }
}
