package com.example.ajaja

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.DataType
import java.nio.ByteBuffer
import java.nio.ByteOrder

class StrengthClassifier(context: Context) {

    private val interpreter: Interpreter

    private val inputSize = 128
    private val labels = listOf("none", "weak", "strong")

    init {
        val modelBytes = context.assets.open("ajaja_text_strength_cnn.tflite").readBytes()
        val buffer = ByteBuffer.allocateDirect(modelBytes.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(modelBytes)
        interpreter = Interpreter(buffer)
    }

    fun classify(bitmap: Bitmap): String {

        val processor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)

        val processed = processor.process(tensorImage)

        val output = Array(1) { FloatArray(3) }

        interpreter.run(processed.buffer, output)

        val maxIdx = output[0].indices.maxBy { output[0][it] }
        return labels[maxIdx]
    }
}
