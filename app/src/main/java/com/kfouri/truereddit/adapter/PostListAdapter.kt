package com.kfouri.truereddit.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.kfouri.truereddit.GlideApp
import com.kfouri.truereddit.R
import com.kfouri.truereddit.api.model.Children
import java.lang.Math.round
import java.util.Date
import kotlin.math.roundToInt
import kotlinx.android.synthetic.main.post_item.view.*

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
            item.dataChildren.thumbnail?.let { itemView.imageView_thumbnail.loadImage(it) }

            item.dataChildren.thumbnail?.let { url ->
                itemView.imageView_thumbnail.setOnClickListener { clickImageListener(url) }
            }
        }

        private fun ImageView.loadImage(url: String) {
            GlideApp.with(this.context.applicationContext)
                .load(url)
                .into(this)
        }

        private fun getTimeAgo(context: Context, created: Long): String {
            val now = System.currentTimeMillis() / 1000
            var timeAgo = (now - created) // in seconds
            val result: String

            when (timeAgo) {
                in 0..59 -> {
                    //result = "$timeAgo segundos"
                    result = context.getString(R.string.time_ago_seconds, timeAgo.toString())
                }
                in 60..3600 -> {
                    timeAgo = (timeAgo / 60)
                    //result = "$timeAgo minutos"
                    result = context.getString(R.string.time_ago_minutes, timeAgo.toString())
                }
                else -> {
                    timeAgo = (timeAgo / 3600)
                    //result = "$timeAgo horas"
                    result = context.getString(R.string.time_ago_hours, timeAgo.toString())
                }
            }
            return result
        }
    }
}