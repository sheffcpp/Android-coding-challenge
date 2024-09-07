package com.example.codingchallangemovieapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codingchallangemovieapp.R
import com.example.codingchallangemovieapp.databinding.FragmentMainListBinding
import com.example.codingchallangemovieapp.di.IO
import com.example.codingchallangemovieapp.vm.MovieListViewModel
import com.example.codingchallangemovieapp.vm.MovieListViewModel.Companion.MIN_PAGE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.navigation.koinNavGraphViewModel

class MainListFragment : Fragment() {

    companion object {
        private val TAG = MainListFragment::class.simpleName
    }

    private var _binding: FragmentMainListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieListViewModel by koinNavGraphViewModel(R.id.main_navigation)

    private val ioDispatcher: CoroutineDispatcher by inject(IO)
    private val mainDispatcher = Dispatchers.Main

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

        viewLifecycleOwner.lifecycleScope.launch(mainDispatcher) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.movieListRecycler.visibility =
                        if (!isLoading) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(ioDispatcher) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.currentlyDisplayedMovies.collectLatest { moviesWrapper ->
//                    println("clt = ${moviesWrapper.movies}")
                    binding.movieListRecycler.post {
                        (binding.movieListRecycler.adapter as MovieListAdapter).updateMovieList(
                            moviesWrapper.movies
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(mainDispatcher) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.pagingDisplay.collectLatest { pagingData ->
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
        viewLifecycleOwner.lifecycleScope.launch(mainDispatcher) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.errorFlow.collect { errorId ->
                    withContext(Dispatchers.Main) {
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