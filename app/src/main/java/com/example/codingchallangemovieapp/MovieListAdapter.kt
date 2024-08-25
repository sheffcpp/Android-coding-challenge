package com.example.codingchallangemovieapp

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codingchallangemovieapp.databinding.MovieItemBinding
import com.example.codingchallangemovieapp.model.Movie
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp

class MovieListAdapter(
    private val onDetailsClick: (Int) -> Unit,
    private val onStarClick: (Boolean, Int) -> Unit
) : RecyclerView.Adapter<MovieListAdapter.ViewHolder>() {

    private val movieList = mutableListOf<Movie>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateMovieList(items: List<Movie>) {
        movieList.clear()
        movieList.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val itemBinding: MovieItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(itemData: Movie) {
            itemBinding.movieTitle.text = itemData.title
            itemBinding.releaseDate.text = itemData.release_date
            val ctx = itemBinding.root.context
            itemBinding.popularity.text =
                ctx.getString(R.string.popularity, itemData.popularity.toString())
            itemBinding.voteAverage.text =
                ctx.getString(R.string.vote_average, itemData.vote_average.toString())
            itemBinding.detailsBtn.setOnClickListener { onDetailsClick(itemData.id) }
            itemBinding.starButton.setOnClickListener {
                itemData.favourite = !itemData.favourite
                updateStarButton(itemData.favourite)
                onStarClick(itemData.favourite, itemData.id)
            }
            updateStarButton(itemData.favourite)
        }

        private fun updateStarButton(isFavourite: Boolean) {
            val ctx = itemBinding.starButton.context
            val typedValue = TypedValue()
            val attr = if (isFavourite) R.attr.starOn else R.attr.starOff
            ctx.theme.resolveAttribute(attr, typedValue, true)
            itemBinding.starButton.icon = IconicsDrawable(ctx, FontAwesome.Icon.faw_star).apply {
                colorInt = typedValue.data
                sizeDp = 24
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding =
            MovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movieData = movieList[position]
        holder.bind(movieData)
    }
}