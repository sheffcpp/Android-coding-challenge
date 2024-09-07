package com.example.codingchallangemovieapp.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codingchallangemovieapp.BuildConfig
import com.example.codingchallangemovieapp.R
import com.example.codingchallangemovieapp.api.ApiClient
import com.example.codingchallangemovieapp.model.Movie
import com.example.codingchallangemovieapp.model.MovieListWrapper
import com.example.codingchallangemovieapp.ui.FavouriteManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MovieListViewModel(
    private val favouriteManager: FavouriteManager,
    private val ioDispatcher: CoroutineDispatcher,
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {
    companion object {
        private val TAG = MovieListViewModel::class.simpleName
        const val MIN_PAGE = 1
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val currentPage = MutableStateFlow(MIN_PAGE)

    private val maxPages = MutableStateFlow(MIN_PAGE)

    private val favManagerMutex = Mutex()

    private val _errorFlow = MutableSharedFlow<Int>()
    val errorFlow = _errorFlow.asSharedFlow()


    private val moviesForCurrentPage: StateFlow<List<Movie>> = currentPage.map { pageNo ->
        println("a")
        withContext(uiDispatcher) {
            _isLoading.emit(true)
        }
        println("b")
        Log.d(TAG, "requesting page $pageNo")
        val call = ApiClient.apiService.getNowPlayingMovies(BuildConfig.API_KEY, pageNo)
        val responseData = runCatching {

            val response = withContext(ioDispatcher) { call.execute() }
            response.also {
                it.body()?.let { moviesRsp ->
                    withContext(uiDispatcher) {
                        maxPages.emit(moviesRsp.total_pages)
                    }
                    Log.d(TAG, "Total pages ${moviesRsp.total_pages}")
                }
            }
            response
        }.onFailure {
            Log.e(TAG, "Error getting data")
            showErrorDialog()
        }.getOrNull()

        withContext(uiDispatcher) {
            _isLoading.emit(false)
        }

//        println("clt mfcp ${responseData?.body()?.results ?: emptyList()}")
        responseData?.body()?.results ?: emptyList()
    }.stateIn(viewModelScope + ioDispatcher, SharingStarted.Eagerly, emptyList())

    val currentlyDisplayedMovies: StateFlow<MovieListWrapper> =
        moviesForCurrentPage.combine(favouriteManager.favouriteMovies) { movies, favouriteIds ->
//            println("clt cdm = ${movies} ${favouriteIds}")
            val c = MovieListWrapper(movies.apply {
                onEach { movie ->
                    movie.favourite = favouriteIds.contains(movie.id)
                }
            })
//            println("clt cdm 2 $c")
            c
        }.stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.Eagerly,
            MovieListWrapper(emptyList())
        )

    val pagingDisplay: StateFlow<Pair<Int, Int>> = currentPage.combine(maxPages) { crPage, mPages ->
        Pair(crPage, mPages)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Pair(currentPage.value, maxPages.value)
    )

    fun seeNextPage() {
        viewModelScope.launch {
            val pageNo = currentPage.value
            if (pageNo < maxPages.value) {
                currentPage.emit(pageNo + 1)
            }
        }
    }

    fun seePreviousPage() {
        viewModelScope.launch {
            val pageNo = currentPage.value
            if (pageNo > MIN_PAGE) {
                currentPage.emit(pageNo - 1)
            }
        }
    }

    fun addToFavourites(movieId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            favManagerMutex.withLock {
                favouriteManager.addFavourite(movieId)
            }
        }
    }

    fun removeFromFavourites(movieId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            favManagerMutex.withLock {
                favouriteManager.removeFavourite(movieId)
            }
        }
    }

    private val _currentMovieDetails = MutableStateFlow<Movie?>(null)
    val currentMovieDetails = _currentMovieDetails.asStateFlow()

    fun setCurrentMovieDetails(movieId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            currentlyDisplayedMovies.value.movies.firstOrNull {
                it.id == movieId
            }?.run {
                withContext(uiDispatcher) {
                    _currentMovieDetails.emit(this@run)
                }
            }

        }
    }

    fun handleDetailsStarButton(): Boolean {
        _currentMovieDetails.value?.let { currentMovie ->
            val isFav = !currentMovie.favourite
            viewModelScope.launch(ioDispatcher) {
                if (isFav) {
                    addToFavourites(currentMovie.id)
                } else {
                    removeFromFavourites(currentMovie.id)
                }
            }
            return isFav
        }
        return false
    }

    private fun showErrorDialog() {
        viewModelScope.launch(uiDispatcher) {
            _errorFlow.emit(R.string.Error_message)
        }
    }
}