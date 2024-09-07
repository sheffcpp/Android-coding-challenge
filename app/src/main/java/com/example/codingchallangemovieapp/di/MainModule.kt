package com.example.codingchallangemovieapp.di

import com.example.codingchallangemovieapp.ui.FavouriteManager
import com.example.codingchallangemovieapp.vm.MovieListViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    single(IO) { Dispatchers.IO }
    single { FavouriteManager(get(), get(IO)) }
    viewModel { MovieListViewModel(get(), get(IO)) }
}
