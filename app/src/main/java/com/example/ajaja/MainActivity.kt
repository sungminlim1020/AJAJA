package com.example.ajaja

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var titleView: TextView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleView = findViewById(R.id.tvTitle)
        bottomNav = findViewById(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_class -> {
                    titleView.text = "수업 관리"
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ClassFragment())
                        .commit()
                    true
                }
                R.id.nav_notice -> {
                    titleView.text = "공지/알림"
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NoticeFragment())
                        .commit()
                    true
                }
                R.id.nav_student -> {
                    titleView.text = "학생 탭 (등록/목록/상세)"
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, StudentFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // 앱 처음 진입 시 기본 탭 선택 (내 정보)
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_class
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }
}
