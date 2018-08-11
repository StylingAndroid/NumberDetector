package com.stylingandroid.numberdetector

import android.os.Build
import com.google.firebase.ml.custom.FirebaseModelInterpreter
import com.google.firebase.ml.custom.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseModelOptions
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions

class InterpreterProvider {

    private val options: FirebaseModelOptions

    init {
        val initialConditions = FirebaseModelDownloadConditions.Builder()
                .build()
        val updateConditions = FirebaseModelDownloadConditions.Builder().run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requireCharging()
                requireDeviceIdle()
            }
            requireWifi()
            build()
        }
        FirebaseCloudModelSource.Builder("mnist")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(initialConditions)
                .setUpdatesDownloadConditions(updateConditions)
                .build().also { source ->
                    FirebaseModelManager.getInstance().registerCloudModelSource(source)
                }
        options = FirebaseModelOptions.Builder()
                .setCloudModelName("mnist")
                .build()
    }

    fun getInterpreter() = FirebaseModelInterpreter.getInstance(options)
}
