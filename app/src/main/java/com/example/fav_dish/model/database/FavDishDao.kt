package com.example.fav_dish.model.database

import androidx.room.*
import com.example.fav_dish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow


@Dao
interface FavDishDao {

    @Insert
    suspend fun insertFavDishDetails(favDish: FavDish)

    @Query("SELECT * FROM FAV_DISHES_TABLE ORDER BY id")
    fun getAllDishesList(): Flow<List<FavDish>>

    @Update
    suspend fun updateFavDishDetails(favDish: FavDish)

    @Query("SELECT * FROM fav_dishes_table WHERE favorite_dish = 1")
    fun getFavoriteDishesList(): Flow<List<FavDish>>

    @Delete
    suspend fun deleteFavDishDetails(favDish: FavDish)

    @Query("SELECT * FROM fav_dishes_table WHERE type =:filterType")
    fun getFilteredDishesList(filterType: String): Flow<List<FavDish>>
}