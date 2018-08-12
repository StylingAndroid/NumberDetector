package com.stylingandroid.numberdetector

import android.graphics.Bitmap
import android.graphics.Color
import com.google.firebase.ml.custom.FirebaseModelDataType
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions
import com.google.firebase.ml.custom.FirebaseModelInputs
import com.google.firebase.ml.custom.FirebaseModelInterpreter
import com.google.firebase.ml.custom.FirebaseModelOutputs

class NumberClassifier(
        provider: InterpreterProvider = InterpreterProvider(),
        private val interpreter: FirebaseModelInterpreter =
                provider.getInterpreter() ?: throw Exception("Unable to get Interpreter")
) {
    private val classCount = 10
    private val imageWidth = 28
    private val imageHeight = 28
    private val imageSize = imageWidth * imageHeight

    private val imagePixels = IntArray(imageSize)

    private val options = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, imageWidth, imageHeight, 1))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, classCount))
            .build()

    fun classify(
            bitmap: Bitmap,
            failure: (Exception) -> Nothing = { exception -> throw(exception) },
            success: (Int, Float, Long) -> Unit
    ) {
        val inputs = FirebaseModelInputs.Builder()
                .add(bitmap.toVector())
                .build()
        val start = System.currentTimeMillis()
        interpreter.run(inputs, options)
                .addOnSuccessListener { outputs ->
                    outputs.map().entries.maxBy { it.value }?.also {
                        success(it.key, it.value, System.currentTimeMillis() - start)
                    }
                }
                .addOnFailureListener {
                    failure(it)
                }
    }

    private fun Bitmap.toVector(): Array<Array<Array<FloatArray>>> {
        getPixels(imagePixels, 0, width, 0, 0, width, height)
        return Array(1) {
            Array(imageHeight) { y ->
                Array(imageWidth) { x ->
                    floatArrayOf(imagePixels[x + (y * imageWidth)].convertToGreyScale())
                }
            }
        }
    }

    private fun Int.convertToGreyScale(): Float =
            1f - ((Color.red(this) + Color.green(this) + Color.blue(this)).toFloat() / 3f / 255f)

    private fun FirebaseModelOutputs.map(): Map<Int, Float> {
        return getOutput<Array<FloatArray>>(0)[0].mapIndexed { index, fl -> index to fl }.toMap()
    }
}
