package com.kfouri.truereddit.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kfouri.truereddit.GlideApp
import com.kfouri.truereddit.databinding.ActivityFullScreenBinding
import com.kfouri.truereddit.view.PostListActivity.Companion.IMAGE_URL

class FullScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFullScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val urlImage = intent.extras?.getString(IMAGE_URL)

        GlideApp.with(this)
            .load(urlImage)
            .fitCenter()
            .into(binding.imageViewFullScreen)

        binding.imageViewFullScreen.setOnClickListener {
            finish()
        }
    }

}