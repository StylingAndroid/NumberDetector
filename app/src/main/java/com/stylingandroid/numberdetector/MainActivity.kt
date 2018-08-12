package com.stylingandroid.numberdetector

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {

    private lateinit var numberClassifier: NumberClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        numberClassifier = NumberClassifier()

        finger_canvas.drawingListener = { bitmap ->
            launch(CommonPool) {
                val start = System.currentTimeMillis()
                numberClassifier.classify(bitmap) { result, confidence, elapsed ->
                    val total = System.currentTimeMillis() - start
                    println("Result: $result, confidence: $confidence, elapsed: ${total}ms total, ${elapsed}ms in ML")
                    launch(UI) {
                        digit.text  = result.toString()
                    }
                }
            }
        }

        button_clear.setOnClickListener {
            finger_canvas.clear()
            digit.text = ""
        }
    }
}
