package com.example.fav_dish.application

import android.app.Application
import com.example.fav_dish.model.database.FavDishRepository
import com.example.fav_dish.model.database.FavDishRoomDatabase

class FavDishApplication :Application(){
    private val database by lazy {
        FavDishRoomDatabase.getDatabase(this@FavDishApplication)
    }

    val repository by lazy{
        FavDishRepository(database.favDishDao())
    }
}