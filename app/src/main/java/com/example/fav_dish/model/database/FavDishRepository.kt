package com.example.fav_dish.model.database

import androidx.annotation.WorkerThread
import com.example.fav_dish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow

class FavDishRepository(private val favDishDao: FavDishDao) {

    @WorkerThread
    suspend fun insertfavDishData(favDish: FavDish) {
        favDishDao.insertFavDishDetails(favDish)
    }

    val allDishesList: Flow<List<FavDish>> = favDishDao.getAllDishesList()

    @WorkerThread
    suspend fun updateFavDishData(favDish: FavDish) {
        favDishDao.updateFavDishDetails(favDish)
    }

    @WorkerThread
    suspend fun deleteFavDishData(favDish: FavDish) {
        favDishDao.deleteFavDishDetails(favDish)
    }

    val favoriteDishList: Flow<List<FavDish>> = favDishDao.getFavoriteDishesList()

    fun filteredListDishes(value: String): Flow<List<FavDish>> =
        favDishDao.getFilteredDishesList(value)

}

