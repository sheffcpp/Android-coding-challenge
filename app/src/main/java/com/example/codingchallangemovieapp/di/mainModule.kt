package com.example.codingchallangemovieapp.di

import com.example.codingchallangemovieapp.ui.FavouriteManager
import com.example.codingchallangemovieapp.vm.MovieListViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    single { FavouriteManager(get()) }
    viewModelOf(::MovieListViewModel)
}