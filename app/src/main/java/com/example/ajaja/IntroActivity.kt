package com.example.ajaja

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val autoLogin = prefs.getBoolean("auto_login", false)
        val autoUserId = prefs.getString("auto_user_id", null)

        Handler(Looper.getMainLooper()).postDelayed({

            if (autoLogin && autoUserId != null) {
                // 자동 로그인 → 세션 설정
                SessionManager.setCurrentUser(this, autoUserId)

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // 자동로그인 X → 로그인 화면으로 이동
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

        }, 1000)
        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }
}
