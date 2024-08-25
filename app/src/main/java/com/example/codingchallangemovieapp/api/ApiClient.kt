package com.example.codingchallangemovieapp.api

import com.example.codingchallangemovieapp.model.GetMoviesRsp
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/3/movie/now_playing")
    fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") pageNo: Int
    ): Call<GetMoviesRsp>

}

object ApiClient {
    val apiService: ApiService by lazy {
        RetrofitUtil.instance.create(ApiService::class.java)
    }
}


