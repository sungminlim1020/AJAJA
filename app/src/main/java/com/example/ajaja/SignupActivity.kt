package com.example.ajaja

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ajaja.databinding.ActivitySignupBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private val levelItems = listOf("초등학교", "중학교", "고등학교")
    private val gradesElem = (1..6).map { "${it}학년" }
    private val gradesMidHigh = (1..3).map { "${it}학년" }

    // 과목 리스트
    private val subjects = listOf("C언어", "파이썬", "엔트리", "로블록스", "ITQ", "정보올림피아드")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** ⭐ TOOLBAR 설정 */
        setSupportActionBar(binding.toolbar)

        // Toolbar에 뒤로가기 버튼 추가
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        // 상태바 색상 적용
        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        /** 뒤로가기 버튼 클릭 시 LoginActivity로 이동 */
        binding.toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        /** 물리 뒤로가기 버튼도 동일 동작 */
        onBackPressedDispatcher.addCallback(this) {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }

        /** 학교급 드롭다운 */
        (binding.actSchoolLevel as MaterialAutoCompleteTextView).setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, levelItems)
        )

        setGradeAdapter(gradesElem)

        binding.actSchoolLevel.setOnItemClickListener { _, _, pos, _ ->
            when (levelItems[pos]) {
                "초등학교" -> setGradeAdapter(gradesElem)
                "중학교", "고등학교" -> setGradeAdapter(gradesMidHigh)
            }
            binding.actGrade.setText("", false) // 학년 초기화
        }

        /** 과목 드롭다운 */
        (binding.actSubject as MaterialAutoCompleteTextView).setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, subjects)
        )

        /** 아이디 중복 확인 */
        binding.btnCheckId.setOnClickListener {
            val inputId = binding.etUserId.text?.toString()?.trim().orEmpty()
            binding.tilUserId.error = null

            if (inputId.isEmpty()) {
                binding.tilUserId.error = "아이디를 입력하세요."
                return@setOnClickListener
            }

            val isDup = SessionManager.isUserIdTaken(this, inputId)
            MaterialAlertDialogBuilder(this)
                .setTitle("아이디 중복 확인")
                .setMessage(if (isDup) "이미 가입한 아이디입니다." else "사용 가능한 아이디입니다.")
                .setPositiveButton("확인", null)
                .show()
        }

        /** 회원가입 버튼 */
        binding.btnSignup.setOnClickListener { doSignup() }
    }

    /** Toolbar 뒤로가기 클릭 시 실행되는 함수 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setGradeAdapter(items: List<String>) {
        (binding.actGrade as MaterialAutoCompleteTextView).setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        )
    }

    /** 회원가입 처리 */
    private fun doSignup() {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val level = binding.actSchoolLevel.text?.toString()?.trim().orEmpty()
        val grade = binding.actGrade.text?.toString()?.trim().orEmpty()
        val subject = binding.actSubject.text?.toString()?.trim().orEmpty()
        val userId = binding.etUserId.text?.toString()?.trim().orEmpty()
        val pw = binding.etPassword.text?.toString()?.trim().orEmpty()
        val pw2 = binding.etPasswordConfirm.text?.toString()?.trim().orEmpty()

        // 에러 초기화
        binding.tilName.error = null
        binding.tilSchoolLevel.error = null
        binding.tilGrade.error = null
        binding.tilSubject.error = null
        binding.tilUserId.error = null
        binding.tilPassword.error = null
        binding.tilPasswordConfirm.error = null

        // 입력값 검증
        if (name.isEmpty()) { binding.tilName.error = "이름을 입력하세요."; return }
        if (level.isEmpty()) { binding.tilSchoolLevel.error = "학교급을 선택하세요."; return }
        if (grade.isEmpty()) { binding.tilGrade.error = "학년을 선택하세요."; return }
        if (subject.isEmpty()) { binding.tilSubject.error = "과목명을 선택하세요."; return }
        if (userId.isEmpty()) { binding.tilUserId.error = "아이디를 입력하세요."; return }
        if (pw.isEmpty()) { binding.tilPassword.error = "비밀번호를 입력하세요."; return }
        if (pw != pw2) {
            binding.tilPasswordConfirm.error = "비밀번호가 일치하지 않습니다."
            return
        }

        // 중복 확인
        if (SessionManager.isUserIdTaken(this, userId)) {
            binding.tilUserId.error = "이미 가입된 아이디입니다."
            MaterialAlertDialogBuilder(this)
                .setTitle("회원가입 실패")
                .setMessage("이미 가입된 아이디입니다.")
                .setPositiveButton("확인", null)
                .show()
            return
        }

        // 계정 생성
        val profile = UserProfile(
            userId = userId,
            userPw = pw,
            name = name,
            level = level,
            grade = grade,
            subject = subject,
            enrollAt = System.currentTimeMillis()
        )

        val added = SessionManager.addUser(this, profile)
        if (!added) {
            Toast.makeText(this, "회원 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 현재 로그인 사용자 저장
        SessionManager.setCurrentUser(this, userId)

        Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java).putExtra("last_user_id", userId))
        finish()
    }
}
