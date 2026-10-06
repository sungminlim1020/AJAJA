package com.example.ajaja

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ajaja.face.FaceNetInterpreter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream

class AttendanceActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvResult: TextView

    // facenet.tflite 로딩만 수행 — SVM 사용 안 함
    private lateinit var faceNet: FaceNetInterpreter

    private val CAMERA_REQUEST_CODE = 1001

    // 중복 팝업 방지 (3초)
    private var lastRecognized = 0L
    private val INTERVAL = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attendance_check)

        // UI 연결
        previewView = findViewById(R.id.previewView)
        tvResult = findViewById(R.id.tvResult)
        tvResult.text = "출석 대기 중..."

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // facenet.tflite 로딩
        faceNet = FaceNetInterpreter(this)

        // 카메라 실행
        if (allPermissionsGranted()) startCamera()
        else requestCameraPermission()

        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun allPermissionsGranted(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == CAMERA_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /** -------------------------
     *   CameraX 시작
     * ------------------------- */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val selector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    selector,
                    preview,
                    analyzer
                )
            } catch (e: Exception) {
                Log.e("CAMERA", "Camera binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /** ------------------------------------
     *   얼굴 감지 → 출석 성공 처리
     * ------------------------------------ */
    private fun processImageProxy(imageProxy: ImageProxy) {

        val media = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(media, rotation)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .enableTracking()
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {

                    val now = System.currentTimeMillis()
                    if (now - lastRecognized > INTERVAL) {

                        lastRecognized = now

                        tvResult.text = "얼굴 감지됨 → 출석 성공!"

                        showSuccessDialog()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FACE", "Face detection error", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /** 출석 성공 팝업 */
    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("출석 완료!")
            .setMessage("얼굴이 정상적으로 감지되었습니다.")
            .setPositiveButton("확인") { d, _ -> d.dismiss() }
            .show()
    }
}
