package com.example.ajaja

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import java.util.*

class HomeworkListActivity : AppCompatActivity() {

    private val selectedFiles = mutableSetOf<String>()   // ✔ 선택된 파일 경로 저장
    private var selectionMode = false                    // ✔ 선택 모드 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homework_list)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnDelete = findViewById<ImageButton>(R.id.btnDelete)
        val container = findViewById<LinearLayout>(R.id.containerHomework)

        btnBack.setOnClickListener {
            if (selectionMode) exitSelectionMode()
            else finish()
        }

        // 🔍 파일 목록 가져오기
        val files = filesDir.listFiles {
                file -> file.name.startsWith("homework_") && file.name.endsWith(".jpg")
        }

        if (files.isNullOrEmpty()) {
            val txt = TextView(this).apply {
                text = "제출된 숙제가 없습니다."
                textSize = 18f
                setPadding(0, 40, 0, 0)
            }
            container.addView(txt)
            return
        }

        val inflater = layoutInflater
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        for (file in files.sortedByDescending { it.lastModified() }) {

            val card = inflater.inflate(R.layout.item_homework, container, false) as CardView
            val tvDate = card.findViewById<TextView>(R.id.tvDate)
            val imgCheck = card.findViewById<ImageView>(R.id.imgCheck)

            val path = file.path
            tvDate.text = "제출일: ${sdf.format(Date(file.lastModified()))}"

            // 📌 일반 클릭 → 상세 보기 (선택 모드 아닐 때만)
            card.setOnClickListener {
                if (selectionMode) toggleSelect(path, imgCheck, card)
                else {
                    val intent = Intent(this, HomeworkDetailActivity::class.java)
                    intent.putExtra("image_path", path)
                    startActivity(intent)
                }
            }

            // 📌 길게 클릭 → 선택 모드 진입
            card.setOnLongClickListener {
                if (!selectionMode) enterSelectionMode()
                toggleSelect(path, imgCheck, card)
                true
            }

            container.addView(card)
        }

        // 🗑 삭제 버튼 클릭
        btnDelete.setOnClickListener {
            if (selectedFiles.isEmpty()) return@setOnClickListener

            val dialog = AlertDialog.Builder(this)
                .setTitle("삭제 확인")
                .setMessage("선택한 과제들을 삭제하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    deleteSelectedFiles()
                }
                .setNegativeButton("아니오", null)
                .create()

            // 🎨 버튼 글씨색 검정색으로 변경
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.BLACK)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.BLACK)
            }

            dialog.show()
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    /** ✔ 선택 모드 활성화 */
    private fun enterSelectionMode() {
        selectionMode = true
        findViewById<ImageButton>(R.id.btnDelete).visibility = android.view.View.VISIBLE
    }

    /** ✔ 선택 모드 종료 */
    private fun exitSelectionMode() {
        selectionMode = false
        selectedFiles.clear()

        findViewById<ImageButton>(R.id.btnDelete).visibility = android.view.View.GONE

        // 모든 체크박스 숨기기
        val container = findViewById<LinearLayout>(R.id.containerHomework)
        for (i in 0 until container.childCount) {
            val check = container.getChildAt(i).findViewById<ImageView>(R.id.imgCheck)
            check?.visibility = android.view.View.GONE

            val card = container.getChildAt(i) as? CardView
            card?.alpha = 1.0f
        }
    }

    /** ✔ 선택/해제 토글 */
    private fun toggleSelect(path: String, imgCheck: ImageView, card: CardView) {
        if (selectedFiles.contains(path)) {
            selectedFiles.remove(path)
            imgCheck.visibility = android.view.View.GONE
            card.alpha = 1.0f
        } else {
            selectedFiles.add(path)
            imgCheck.visibility = android.view.View.VISIBLE
            card.alpha = 0.6f
        }
    }

    /** ✔ 선택된 파일 삭제 후 리스트 새로고침 */
    private fun deleteSelectedFiles() {
        // 파일 삭제
        for (path in selectedFiles) {
            val file = java.io.File(path)
            if (file.exists()) file.delete()
        }

        // 화면 재시작
        finish()
        startActivity(intent)
    }
}
