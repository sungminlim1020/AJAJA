package com.example.ajaja

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat

class HomeworkDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homework_detail)

        val imgView = findViewById<ImageView>(R.id.imgHomework)
        val path = intent.getStringExtra("image_path") ?: return

        val bmp = BitmapFactory.decodeFile(path)
        imgView.setImageBitmap(bmp)

        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }
}
