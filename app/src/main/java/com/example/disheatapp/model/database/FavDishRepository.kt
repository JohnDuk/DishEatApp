package com.example.disheatapp.model.database

import androidx.annotation.WorkerThread
import com.example.disheatapp.model.entities.FavDish

import kotlinx.coroutines.flow.Flow

class FavDishRepository(private val favDishDao: FavDishDao) {

    /**
     * By default Room runs suspend queries off the main thread, therefore, we don't need to
     * implement anything else to ensure we're not doing long running database work
     * off the main thread.
     */
    @WorkerThread
    suspend fun insertFavDishData(favDish: FavDish) {
        favDishDao.insertFavDishDetails(favDish)
    }

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allDishesList: Flow<List<FavDish>> = favDishDao.getAllDishesList()

    @WorkerThread
    suspend fun updateFavDishData(favDish: FavDish) {
        favDishDao.updateFavDishDetails(favDish)
    }

    // TODO Step 2: Get the list of favorite dishes from the DAO and pass it to the ViewModel.
    // START
    val favoriteDishes: Flow<List<FavDish>> = favDishDao.getFavoriteDishesList()
    // END

    @WorkerThread
    suspend fun  deleteFavDishData(favDish: FavDish){
        favDishDao.deleteFavDishDetails(favDish)

    }

   fun filteredListDishes(value: String): Flow<List<FavDish>> = favDishDao.getFilteredDishesList(value)
}