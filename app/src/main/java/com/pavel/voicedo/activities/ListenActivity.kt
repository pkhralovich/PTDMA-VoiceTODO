package com.pavel.voicedo.activities

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.pavel.voicedo.R
import com.pavel.voicedo.voice.Speaker
import java.util.*


class ListenActivity : AppCompatActivity(), TextToSpeech.OnInitListener, RecognitionListener {
    companion object {
        private const val TAG = "ListenActivity"
    }

    @BindView(R.id.icon_waves)
    lateinit var icon_waves: View
    @BindView(R.id.microphone_container)
    lateinit var icon_micro: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listen)

        ButterKnife.bind(this)
        Speaker.init(this, this)

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.pavel.voicedo")
        speechRecognizer.startListening(intent)
    }

    @OnClick(R.id.btn_cancel)
    fun onCancel() {
        this.finish()
    }

    override fun onDestroy() {
        Speaker.destroy()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (Speaker.onInit(status)) {
            Speaker.speak(R.string.response_how_can_help)
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "SpeechToText ready")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        //Log.d(TAG, "onRmsChanged: $rmsdB")
        if (rmsdB > 0) {
            val percentage = (0.5 + ((rmsdB / 10)))
            //Log.d(TAG, "percentage: $percentage")

            val anim_waves: Animation = ScaleAnimation(
                    1f, 1f,
                    0.5f, percentage.toFloat(),
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            )
            anim_waves.duration = 50
            icon_waves.startAnimation(anim_waves)
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d(TAG, "onBufferReceived")
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech")
    }

    override fun onError(error: Int) {
        Log.d(TAG, "onError: $error")
    }

    override fun onResults(results: Bundle?) {
        Log.d(TAG, "onResults")

        var message = ""
        val data: ArrayList<String> = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!!
        for (i in 0 until data.size)
            message += data[i]
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d(TAG, "onPartialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent")
    }
}