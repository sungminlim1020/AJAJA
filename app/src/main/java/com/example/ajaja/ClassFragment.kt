package com.example.ajaja

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class ClassFragment : Fragment(R.layout.fragment_class) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 메뉴 버튼들
        val menuAttendance = view.findViewById<LinearLayout>(R.id.menuAttendance)
        val menuReport = view.findViewById<LinearLayout>(R.id.menuReport)
        val menuAIScan = view.findViewById<LinearLayout>(R.id.menuAIScan)
        val menuHomework = view.findViewById<LinearLayout>(R.id.menuHomework)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)

        // ✅ 출석 체크 클릭 → AttendanceActivity 이동
        menuAttendance.setOnClickListener {
            val intent = Intent(requireContext(), AttendanceActivity::class.java)
            startActivity(intent)
        }

        // 학습 리포트 화면 이동
        menuReport.setOnClickListener {
            val intent = Intent(requireContext(), ReportActivity::class.java)
            startActivity(intent)
        }

        // AI 스캔 화면 이동
        menuAIScan.setOnClickListener {
            val intent = Intent(requireContext(), AIScanActivity::class.java)
            startActivity(intent)
        }

        // 과제 제출 목록 화면 이동
        menuHomework.setOnClickListener {
            val intent = Intent(requireContext(), HomeworkListActivity::class.java)
            startActivity(intent)
        }

        // 달력 날짜 클릭 이벤트
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = "$year.${month + 1}.$dayOfMonth"
            // 원하는 기능 넣으면 됨 (Toast는 필요하면 추가)
        }


    }
}
