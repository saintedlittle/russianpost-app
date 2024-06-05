package ru.russianpost.digitalperiodicals.features.menu

import retrofit2.Response
import retrofit2.http.GET
import ru.russianpost.digitalperiodicals.entities.UsernameResponce

interface MenuRepository {

    @GET("api/v1/user/info ")
    suspend fun getInfo() : Response<UsernameResponce>
}