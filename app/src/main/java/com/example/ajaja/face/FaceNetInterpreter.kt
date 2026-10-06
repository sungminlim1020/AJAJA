package com.example.ajaja.face

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceNetInterpreter(context: Context) {

    private val interpreter: Interpreter

    init {
        val model = context.assets.open("facenet.tflite").readBytes()
        val buffer = ByteBuffer.allocateDirect(model.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(model)
        interpreter = Interpreter(buffer)
    }

    fun getEmbedding(bitmap: Bitmap): FloatArray {

        val scaled = Bitmap.createScaledBitmap(bitmap, 160, 160, true)
        val input = ByteBuffer.allocateDirect(160 * 160 * 3 * 4)
        input.order(ByteOrder.nativeOrder())

        for (y in 0 until 160) {
            for (x in 0 until 160) {
                val px = scaled.getPixel(x, y)
                input.putFloat(((px shr 16 and 0xFF) - 128f) / 128f)
                input.putFloat(((px shr 8 and 0xFF) - 128f) / 128f)
                input.putFloat(((px and 0xFF) - 128f) / 128f)
            }
        }

        val embedding = Array(1) { FloatArray(128) }
        interpreter.run(input, embedding)
        return embedding[0]
    }
}
