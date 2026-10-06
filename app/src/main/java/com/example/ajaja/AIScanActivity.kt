package com.example.ajaja

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class AIScanActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var analysisText: TextView
    private lateinit var btnCapture: Button
    private lateinit var btnSubmit: Button

    private var photoUri: Uri? = null
    private lateinit var photoFile: File

    private val REQUEST_CAMERA_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_scan)

        imagePreview = findViewById(R.id.imagePreview)
        analysisText = findViewById(R.id.txtResult)
        btnCapture = findViewById(R.id.btnCapture)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.isEnabled = false

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        btnCapture.setOnClickListener { checkCameraPermission() }

        btnSubmit.setOnClickListener { showSubmitDialog() }

        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = File.createTempFile("homework_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                imagePreview.setImageBitmap(bitmap)
                analyzeHomework(bitmap)
            }
        }

    private fun analyzeHomework(bitmap: Bitmap) {

        analysisText.text = "사진 분석 중...\n"

        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { textResult ->

                val hasText = textResult.text.isNotBlank()

                // ⭐ StrengthClassifier 실행
                val classifier = StrengthClassifier(this)
                val strength = classifier.classify(bitmap)

                // ⭐ 색상 선택 (필기 강도)
                val color = when (strength) {
                    "strong" -> Color.parseColor("#8E44AD")
                    "weak" -> Color.parseColor("#E67E22")
                    else -> Color.parseColor("#7F8C8D")
                }
                analysisText.setTextColor(color)

                analysisText.text =
                    "📘 숙제 분석 결과\n\n" +
                            "✏ 글씨 감지: ${if (hasText) "감지됨" else "감지 안됨"}\n" +
                            "💪 필기 강도: $strength\n"

                // ⭐ 제출 버튼 색상 변경 로직 추가
                if (hasText) {
                    btnSubmit.isEnabled = true
                    btnSubmit.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#03A9F4"))
                } else {
                    btnSubmit.isEnabled = false
                    btnSubmit.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#BBDDFF"))
                }

            }
    }

    private fun showSubmitDialog() {

        val savedFile = File(filesDir, "homework_${System.currentTimeMillis()}.jpg")
        photoFile.copyTo(savedFile, overwrite = true)

        AlertDialog.Builder(this)
            .setTitle("제출 완료")
            .setMessage("숙제가 성공적으로 제출되었습니다.")
            .setPositiveButton("제출 목록 확인") { _, _ ->
                val intent = Intent(this, HomeworkListActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("닫기") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
