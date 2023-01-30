package com.example.disheatapp.application

import android.app.Application
import com.example.disheatapp.model.database.FavDishRepository
import com.example.disheatapp.model.entities.database.FavDishRoomDatabase

class FavDishApplication : Application(){
    private val database by lazy { FavDishRoomDatabase.getDatabase((this@FavDishApplication))}
    val repository by lazy { FavDishRepository(database.favDishDao())}


}