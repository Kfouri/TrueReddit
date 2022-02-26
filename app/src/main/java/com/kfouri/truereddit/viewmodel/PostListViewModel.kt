package com.kfouri.truereddit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kfouri.truereddit.api.APIService
import com.kfouri.truereddit.api.ApiClient
import com.kfouri.truereddit.api.model.Children
import com.kfouri.truereddit.api.model.PostResponse
import com.kfouri.truereddit.state.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostListViewModel: ViewModel(), CoroutineScope {

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
                    mAPIService?.let { Resource.success(data = it.getPosts(after)) }
                )
            } catch (e: Exception) {
                postListMutableLiveData.postValue(
                    Resource.error(data = null, message = e.message ?: "Error getting Posts")
                )
            }
        }
    }
}
