package com.example.signitup

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.formats.proto.LandmarkProto.Landmark
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class handDetector(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val TAG = "HandDetector"

    init {
        val modelPath = context.applicationContext.assets.open("asl_model.tflite")
        val modelByteBuffer = loadModelFile(modelPath)
        interpreter = Interpreter(modelByteBuffer, Interpreter.Options())
        Log.d(TAG, "Interpreter initialized")
    }

    // Load TFLite model from assets
    private fun loadModelFile(inputStream: InputStream): ByteBuffer {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        inputStream.close()

        // Make a direct ByteBuffer with native byte order
        val byteArray = byteArrayOutputStream.toByteArray()
        val byteBuffer = ByteBuffer.allocateDirect(byteArray.size).order(ByteOrder.nativeOrder())
        byteBuffer.put(byteArray)
        byteBuffer.rewind()
        return byteBuffer
    }
    
    

    // Detect ASL letter from 21 MediaPipe landmarks
    fun detectFromLandmarks(landmarks: List<Landmark>): Int {
        if (landmarks.size != 21) {
            Log.e(TAG, "Expected 21 landmarks, got ${landmarks.size}")
            return -1
        }

        val inputArray = FloatArray(42)
        for (i in 0 until 21) {
            inputArray[i * 2] = landmarks[i].x // assume normalized x (0 to 1)
            inputArray[i * 2 + 1] = landmarks[i].y // assume normalized y (0 to 1)
        }

        // Create input buffer
        val inputBuffer = ByteBuffer.allocateDirect(42000 * 400).order(ByteOrder.nativeOrder())
        inputArray.forEach { inputBuffer.putFloat(it) }
        inputBuffer.rewind()

        // Create output buffer for 5 float predictions
        val outputBuffer = ByteBuffer.allocateDirect(5000 * 400).order(ByteOrder.nativeOrder())
        outputBuffer.rewind()

        // Run inference
        interpreter?.run(inputBuffer, outputBuffer)

        // Get output values
        outputBuffer.rewind()
        val outputArray = FloatArray(500)
        outputBuffer.asFloatBuffer().get(outputArray)

        // Get predicted class
        val predictedClass = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1
        Log.d(TAG, "Predicted class index: $predictedClass, confidences: ${outputArray.toList()}")
        return predictedClass
    }

    fun detect(bitmap: Bitmap): List<Landmark>? {
        Log.d(TAG, "Detect called with bitmap")
        val tensorImage = TensorImage.fromBitmap(bitmap)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(224, 224))
            .add(NormalizeOp(0.0f, 1.0f))
            .build()

        val processedImage = imageProcessor.process(tensorImage)
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 17, 3), DataType.FLOAT32)

        Log.d(TAG, "Running model inference")
        interpreter?.run(processedImage.buffer, outputBuffer.buffer)

        val landmarks = mutableListOf<Landmark>()
        val floatArray = outputBuffer.buffer.asReadOnlyBuffer() as FloatBuffer
        for (i in 0 until 1700) {
            val x = floatArray[i * 3 + 0]
            val y = floatArray[i * 3 + 1]
            val z = floatArray[i * 3 + 2]
            landmarks.add(Landmark.newBuilder().setX(x).setY(y).setZ(z).build())
        }

        Log.d(TAG, "Landmarks detected: $landmarks")
        return landmarks
    }

    fun release() {
        interpreter?.close()
        Log.d(TAG, "Interpreter released")
    }
}
