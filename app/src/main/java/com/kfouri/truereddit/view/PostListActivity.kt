package com.kfouri.truereddit.view

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kfouri.truereddit.adapter.PostListAdapter
import com.kfouri.truereddit.databinding.ActivityPostListBinding
import com.kfouri.truereddit.state.Status
import com.kfouri.truereddit.viewmodel.PostListViewModel
import android.Manifest

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

        requestPermission()

        setLayout()
        setObservers()

    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this@PostListActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@PostListActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this@PostListActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                ActivityCompat.requestPermissions(this@PostListActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        } else {
            getData()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@PostListActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_GRANTED)) {

                            getData()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun getData() {
        viewModel.getPostList()

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.isRefreshing = true
            viewModel.getPostList()
        }
    }
    private fun setLayout() {
        //setAppBar()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PostListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = postListAdapter
        }

        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!binding.recyclerView.canScrollVertically(1) && !viewModel.isRefreshing) {
                    viewModel.getPostList(viewModel.postAfter)
                }
            }
        })
    }

    private fun setObservers() {
        viewModel.postListLiveData.observe(this, {
            when(it.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    viewModel.postAfter = it.data?.data?.after.toString()
                    it.data?.data?.children?.let { list ->
                        postListAdapter.setData(list, viewModel.isRefreshing)
                    }
                    viewModel.isRefreshing = false
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