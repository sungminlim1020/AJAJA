package com.example.ajaja

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.AlertDialog

class StudentFragment : Fragment(R.layout.fragment_student) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvStudentId: TextView = view.findViewById(R.id.tvStudentId)
        val tvClassInfo: TextView = view.findViewById(R.id.tvClassInfo)
        val tvEnrollDate: TextView = view.findViewById(R.id.tvEnrollDate)
        val tvLogout: TextView = view.findViewById(R.id.tvLogout)

        val ctx = requireContext()

        // 현재 로그인한 사용자 정보
        val user = SessionManager.getCurrentUser(ctx)
        if (user == null) {
            startActivity(Intent(ctx, LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        // UI 반영
        tvName.text = user.name.ifEmpty { "이름 없음" }
        tvStudentId.visibility = View.GONE

        tvClassInfo.text = when {
            user.grade.isNotEmpty() && user.subject.isNotEmpty() -> "${user.grade} ${user.subject}반"
            user.grade.isNotEmpty() -> user.grade
            user.subject.isNotEmpty() -> "${user.subject}반"
            else -> "학년/과목 정보 없음"
        }

        tvEnrollDate.text = user.enrollAt.toDateLabel()

        // ⭐ 로그아웃 버튼 클릭 - 자동로그인까지 초기화 ⭐
        tvLogout.setOnClickListener {

            val dialog = MaterialAlertDialogBuilder(ctx)
                .setTitle("로그아웃")
                .setMessage("로그아웃을 하시겠습니까?")
                .setPositiveButton("예") { _, _ ->

                    // ➤ 자동로그인 해제
                    val prefs = ctx.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    // ➤ 현재 사용자 로그아웃
                    SessionManager.logout(ctx)

                    // ➤ 로그인 화면으로 이동
                    val intent = Intent(ctx, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("아니오", null)
                .create()

            // ⭐ 팝업 배경 흰색 + 버튼 글씨 검정색 적용 ⭐
            dialog.setOnShowListener {
                dialog.window?.setBackgroundDrawableResource(android.R.color.white)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.BLACK)
            }

            dialog.show()
        }

    }

    private fun Long.toDateLabel(pattern: String = "yyyy.MM.dd"): String {
        val sdf = SimpleDateFormat(pattern, Locale.KOREA)
        return sdf.format(Date(this))
    }
}
