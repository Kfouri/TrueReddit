package com.kfouri.truereddit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.kfouri.truereddit.GlideApp
import com.kfouri.truereddit.R
import com.kfouri.truereddit.api.model.Children
import kotlinx.android.synthetic.main.post_item.view.*
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostListAdapter(
    private val context: Context,
    private val clickImageListener: (String) -> Unit,
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list = ArrayList<Children>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        (holder as ViewHolder).bind(item,context, clickImageListener)
    }

    fun setData(newList: List<Children>, isRefreshing: Boolean) {
        if (isRefreshing) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        } else {
            val oldCount = list.size
            list.addAll(newList)
            notifyItemRangeInserted(oldCount, list.size)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: Children, context: Context, clickImageListener: (String) -> Unit){

            val timeAgo = getTimeAgo(context, item.dataChildren.created)
            itemView.textView_title.text = item.dataChildren.title
            itemView.textView_author.text = context.getString(R.string.published_by, item.dataChildren.authorFullName)
            itemView.textView_date.text = context.getString(R.string.time_ago, timeAgo)
            itemView.textView_numberComments.text = context.getString(R.string.num_comments, item.dataChildren.numComments.toString())
            item.dataChildren.thumbnail?.let { url ->
                loadImage(itemView.imageView_thumbnail, url, item.dataChildren.id)
            }

            item.dataChildren.thumbnail?.let { url ->
                itemView.imageView_thumbnail.setOnClickListener { clickImageListener(url) }
            }
        }

        private fun loadImage(imageView: ImageView, url: String, id: String) {
            GlideApp.with(imageView.context.applicationContext)
                .load(url)
                .listener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        CoroutineScope(Dispatchers.IO).launch {
                            saveImage(Glide.with(imageView.context.applicationContext)
                                .asBitmap()
                                .load(url)
                                .submit()
                                .get(), id, imageView.context.applicationContext)
                        }
                        return false
                    }

                })
                .into(imageView)
        }

        private fun saveImage(image: Bitmap, idPost: String, context: Context): String? {
            var savedImagePath: String? = null
            val imageFileName = "$idPost.jpg"
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/TrueReddit"
            )
            var success = true
            if (!storageDir.exists()) {
                success = storageDir.mkdirs()
            }
            if (success) {
                val imageFile = File(storageDir, imageFileName)
                savedImagePath = imageFile.absolutePath
                try {
                    val fOut: OutputStream = FileOutputStream(imageFile)
                    image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                    fOut.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Add the image to the system gallery
                galleryAddPic(savedImagePath, context)
            }
            return savedImagePath
        }

        private fun galleryAddPic(imagePath: String?, context: Context) {
            imagePath?.let { path ->
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val f = File(path)
                val contentUri: Uri = Uri.fromFile(f)
                mediaScanIntent.data = contentUri
                context.sendBroadcast(mediaScanIntent)
            }
        }

        private fun getTimeAgo(context: Context, created: Long): String {
            val now = System.currentTimeMillis() / 1000
            var timeAgo = (now - created) // in seconds
            val result: String

            when (timeAgo) {
                in 0..59 -> {
                    result = context.getString(R.string.time_ago_seconds, timeAgo.toString())
                }
                in 60..3600 -> {
                    timeAgo = (timeAgo / 60)
                    result = context.getString(R.string.time_ago_minutes, timeAgo.toString())
                }
                else -> {
                    timeAgo = (timeAgo / 3600)
                    result = context.getString(R.string.time_ago_hours, timeAgo.toString())
                }
            }
            return result
        }
    }
}