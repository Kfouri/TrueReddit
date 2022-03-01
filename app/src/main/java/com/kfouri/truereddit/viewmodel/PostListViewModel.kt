package com.kfouri.truereddit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kfouri.truereddit.api.APIService
import com.kfouri.truereddit.api.ApiClient
import com.kfouri.truereddit.api.model.PostResponse
import com.kfouri.truereddit.database.DatabaseHelper
import com.kfouri.truereddit.database.model.Post
import com.kfouri.truereddit.state.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostListViewModel(private val databaseHelper: DatabaseHelper): ViewModel(), CoroutineScope {

    override val coroutineContext = Job()
    private val postListMutableLiveData = MutableLiveData<Resource<PostResponse>>()
    private var mAPIService: APIService? = null

    var postAfter: String = ""
    var isRefreshing: Boolean = false

    val postListLiveData: LiveData<Resource<PostResponse>>
        get() = postListMutableLiveData

    init {
        mAPIService = ApiClient.apiService
    }

    fun getPostList(after: String = "") {
        launch {
            postListMutableLiveData.postValue(Resource.loading(data = null))
            try {
                postListMutableLiveData.postValue(
                    mAPIService?.let { it ->
                        val response = it.getPosts(after)
                        val listPostRead = databaseHelper.getPostRead()
                        response.data.children.forEach { item ->
                            val found = listPostRead.find {
                                item.dataChildren.id == it.id
                            }
                            found?.let {
                                item.dataChildren.isRead = true
                            }
                        }

                        Resource.success(data = response)
                    }
                )
            } catch (e: Exception) {
                postListMutableLiveData.postValue(
                    Resource.error(data = null, message = e.message ?: "Error getting Posts")
                )
            }
        }
    }

    fun setPostRead(post: Post) {
        launch {
            databaseHelper.insertPostRead(post)
        }
    }
}
