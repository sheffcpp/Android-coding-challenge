package com.example.codingchallangemovieapp.ui

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.codingchallangemovieapp.R
import com.example.codingchallangemovieapp.databinding.FragmentMovieDetailsBinding
import com.example.codingchallangemovieapp.vm.MovieListViewModel
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.navigation.koinNavGraphViewModel

class MovieDetailsFragment : Fragment() {
    companion object {
        private val TAG = MovieDetailsFragment::class.simpleName
        private const val TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w500"
        private const val IMAGE_WIDTH = 1000
        private const val IMAGE_HEIGHT = 1000
    }

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieListViewModel by koinNavGraphViewModel(R.id.main_navigation)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating view")
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.currentMovieDetails.collect { _movieData ->
                    _movieData?.let { movieData ->
                        binding.title.text = movieData.title
                        binding.originalTitle.text =
                            getString(R.string.original_title, movieData.original_title)
                        binding.overview.text = movieData.overview
                        binding.originalLanguage.text =
                            getString(R.string.original_language, movieData.original_language)
                        binding.popularity.text =
                            getString(R.string.popularity, movieData.popularity.toString())
                        binding.releaseDate.text =
                            getString(R.string.release_date, movieData.release_date)
                        binding.voteAverage.text =
                            getString(R.string.vote_average, movieData.vote_average.toString())
                        binding.voteCount.text =
                            getString(R.string.vote_count, movieData.vote_count.toString())

                        val imageUrl = TMDB_IMAGE_URL + movieData.backdrop_path
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .apply(RequestOptions().override(IMAGE_WIDTH, IMAGE_HEIGHT).fitCenter())
                            .into(binding.image)

                        handleIsFavourite(_movieData.favourite)
                    }
                }
            }
        }

        binding.starButton.setOnClickListener {
            Log.d(TAG, "Star button clicked")
            val isFavourite = viewModel.handleDetailsStarButton()
            handleIsFavourite(isFavourite)
        }
    }

    private fun handleIsFavourite(isFavourite: Boolean) {
        val typedValue = TypedValue()
        val attr = if (isFavourite) R.attr.starOn else R.attr.starOff
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        binding.starButton.icon =
            IconicsDrawable(requireContext(), FontAwesome.Icon.faw_star).apply {
                colorInt = typedValue.data
                sizeDp = 24
            }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        _binding = null
    }
}