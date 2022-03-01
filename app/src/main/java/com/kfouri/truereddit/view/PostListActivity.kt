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
import android.content.DialogInterface
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import com.kfouri.truereddit.R
import com.kfouri.truereddit.api.model.Children
import com.kfouri.truereddit.api.model.DataChildren
import com.kfouri.truereddit.database.DatabaseBuilder
import com.kfouri.truereddit.database.DatabaseHelperImpl
import com.kfouri.truereddit.database.model.Post
import com.kfouri.truereddit.viewmodel.ViewModelFactory

class PostListActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_URL = "IMAGE_URL"
    }

    private val postListAdapter = PostListAdapter(
        this@PostListActivity
    ) { dataChildren : DataChildren, position: Int -> itemClicked(dataChildren, position) }
    private lateinit var binding: ActivityPostListBinding
    private lateinit var postList: ArrayList<Children>

    private val dbHelper by lazy {
        this.let {
            DatabaseBuilder.getInstance(
                it
            )
        }.let { DatabaseHelperImpl(it)}
    }

    val viewModel: PostListViewModel by viewModels { ViewModelFactory(dbHelper) }

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
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
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
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PostListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = postListAdapter
            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(this)
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
                        postList = ArrayList(list)
                        postListAdapter.setData(postList, viewModel.isRefreshing)
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

    private fun itemClicked(dataChildren: DataChildren, position: Int) {
        viewModel.setPostRead(Post(dataChildren.id))
        postList.filter {
            it.dataChildren.id == dataChildren.id
        }[0].dataChildren.isRead = true
        postListAdapter.notifyItemChanged(position)

        val intent = Intent(this, FullScreenActivity::class.java).putExtra(IMAGE_URL,dataChildren.thumbnail)
        startActivity(intent)
    }

    private val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            postList.removeAt(position)
            postListAdapter.setData(postList, true)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_remuve_all -> {
                removeAllPosts()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun removeAllPosts() {

        val positiveButtonClick = { _: DialogInterface, _: Int ->
            viewModel.isRefreshing = true
            postList.clear()
            postListAdapter.setData(postList, true)
        }

        val negativeButtonClick = { _: DialogInterface, _: Int ->
            //do nothing
        }

        if (postList.size > 0) {
            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle(getString(R.string.app_name))
                setMessage(getString(R.string.remove_all_message))
                setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener(function = positiveButtonClick))
                setNegativeButton(android.R.string.cancel, negativeButtonClick)
                show()
            }
        }
    }
}