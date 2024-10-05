package com.raj.mydayquote.ApiService

import com.raj.mydayquote.item.Quote
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface QuoteApiService {
    @GET("random")
     suspend fun getQuoteOfTheDay(): Response<List<Quote>>
}