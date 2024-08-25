package com.example.codingchallangemovieapp

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codingchallangemovieapp.MovieListViewModel.Companion.MIN_PAGE
import com.example.codingchallangemovieapp.databinding.FragmentMainListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainListFragment : Fragment() {

    companion object {
        private val TAG = MainListFragment::class.simpleName
    }

    private var _binding: FragmentMainListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating view")
        _binding = FragmentMainListBinding.inflate(inflater, container, false)
        binding.movieListRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.movieListRecycler.adapter = MovieListAdapter(
            { movieId ->
                Log.d(TAG, "Clicked details for movie $movieId")
                viewModel.setCurrentMovieDetails(movieId)
                findNavController().navigate(R.id.movieDetailsFragment)
            },
            { isFavourite, movieId ->
                Log.d(TAG, "Clicked star on $movieId is favourite $isFavourite")
                if (isFavourite) {
                    viewModel.addToFavourites(movieId)
                } else {
                    viewModel.removeFromFavourites(movieId)
                }
            })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.movieListRecycler.visibility =
                        if (!isLoading) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.currentlyDisplayedMovies.collectLatest { moviesWrapper ->
                    binding.movieListRecycler.post {
                        (binding.movieListRecycler.adapter as MovieListAdapter).updateMovieList(
                            moviesWrapper.movies
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.pagingDisplay.collectLatest { pagingData ->
                    binding.PageIndicator.post {
                        val (currPage, maxPages) = pagingData
                        binding.PageIndicator.text = getString(
                            R.string.page_out_of_max,
                            currPage.toString(),
                            maxPages.toString()
                        )
                        binding.nextPageButton.isEnabled = currPage < maxPages
                        binding.prevPageButton.isEnabled = currPage > MIN_PAGE
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.errorFlow.collect { errorId ->
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.Error)
                        .setMessage(errorId)
                        .setPositiveButton(R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }


        binding.nextPageButton.setOnClickListener {
            Log.d(TAG, "Next page click")
            viewModel.seeNextPage()
        }
        binding.prevPageButton.setOnClickListener {
            Log.d(TAG, "Previous page click")
            viewModel.seePreviousPage()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        _binding = null
    }
}