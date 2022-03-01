package com.kfouri.truereddit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kfouri.truereddit.database.DatabaseHelper

class ViewModelFactory(private val databaseHelper: DatabaseHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PostListViewModel(databaseHelper) as T
    }
}