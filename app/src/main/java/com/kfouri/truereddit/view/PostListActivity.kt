package com.kfouri.truereddit.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kfouri.truereddit.adapter.PostListAdapter
import com.kfouri.truereddit.databinding.ActivityPostListBinding
import com.kfouri.truereddit.state.Status
import com.kfouri.truereddit.viewmodel.PostListViewModel

class PostListActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_URL = "IMAGE_URL"
    }

    private val viewModel: PostListViewModel by viewModels()
    private val postListAdapter = PostListAdapter(
        this@PostListActivity
    ) { urlImage : String -> itemClicked(urlImage) }
    private lateinit var binding: ActivityPostListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setLayout()
        setObservers()
        viewModel.getPostList()
    }

    private fun setLayout() {
        //setAppBar()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PostListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = postListAdapter
        }
    }

    private fun setObservers() {
        viewModel.postListLiveData.observe(this, {
            when(it.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    it.data?.data?.children?.let { list ->
                        postListAdapter.setData(list)
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: " + it.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun itemClicked(urlImage: String) {
        val intent = Intent(this, FullScreenActivity::class.java).putExtra(IMAGE_URL,urlImage)
        startActivity(intent)
    }
}