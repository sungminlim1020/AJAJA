package com.example.ajaja

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var tilId: TextInputLayout
    private lateinit var tilPw: TextInputLayout
    private lateinit var etId: TextInputEditText
    private lateinit var etPw: TextInputEditText
    private lateinit var cbAuto: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var tvSignup: TextView
    private lateinit var tvFindId: TextView
    private lateinit var tvFindPw: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tilId = findViewById(R.id.tilId)
        tilPw = findViewById(R.id.tilPw)
        etId = findViewById(R.id.etId)
        etPw = findViewById(R.id.etPw)
        cbAuto = findViewById(R.id.cbAuto)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignup = findViewById(R.id.tvSignup)
        tvFindId = findViewById(R.id.tvFindId)
        tvFindPw = findViewById(R.id.tvFindPw)

        val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        btnLogin.setOnClickListener {
            val id = etId.text?.toString()?.trim().orEmpty()
            val pw = etPw.text?.toString()?.trim().orEmpty()

            tilId.error = null
            tilPw.error = null

            if (id.isEmpty()) { tilId.error = "ID를 입력하세요."; return@setOnClickListener }
            if (pw.isEmpty()) { tilPw.error = "비밀번호를 입력하세요."; return@setOnClickListener }

            // 유저 조회
            val user = SessionManager.getUser(this, id)
            if (user == null) {
                tilId.error = "가입된 아이디가 아닙니다."
                return@setOnClickListener
            }
            if (user.userPw != pw) {
                tilPw.error = "비밀번호가 올바르지 않습니다."
                return@setOnClickListener
            }

            // 로그인 성공 → 세션 설정
            SessionManager.setCurrentUser(this, id)

            // ⭐ 자동로그인 저장 ⭐
            prefs.edit()
                .putBoolean("auto_login", cbAuto.isChecked)
                .putString("auto_user_id", id)
                .apply()

            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        tvFindId.setOnClickListener { Toast.makeText(this, "ID 찾기(미구현)", Toast.LENGTH_SHORT).show() }
        tvFindPw.setOnClickListener { Toast.makeText(this, "PW 찾기(미구현)", Toast.LENGTH_SHORT).show() }

        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }
}
