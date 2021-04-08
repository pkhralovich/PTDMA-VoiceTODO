package com.pavel.voicedo.activities.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.core.view.isVisible
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pavel.voicedo.R
import com.pavel.voicedo.dialogs.HelpDialog
import com.pavel.voicedo.voice.ActionParser
import java.util.ArrayList

abstract class ListenableActivity : ToolbarActivity(), RecognitionListener {
    companion object {
        const val MAX_LISTEN_TIMEOUT : Long = 3000
        const val ANIMATION_DURATION : Long = 150

        private const val TAG = "ListenableActivity"
    }

    @BindView(R.id.fab)
    lateinit var fab : FloatingActionButton

    @BindView(R.id.listener)
    lateinit var listener : View

    @OnClick(R.id.fab)
    open fun onClickListen() {
        if (!listener.isVisible) {
            showView(listener)
            hideView(fab, true)

            Handler(Looper.getMainLooper()).postDelayed({
                showView(fab, true)
                hideView(listener)
            }, MAX_LISTEN_TIMEOUT)

            val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(this)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.pavel.voicedo")
            speechRecognizer.startListening(intent)
        }
    }

    @OnClick(R.id.info_icon)
    open fun onClickHelp() {
        HelpDialog(this, getHelpText()).show()
    }

    abstract fun getHelpText() : List<String>

    private fun showView(view: View, bothAxis: Boolean = false) {
        val xTarget = if (bothAxis) 0f else 1f
        val xReference = if (bothAxis) 0.5f else 0f
        val yReference = if (bothAxis) 0.5f else 0f

        val anim: Animation = ScaleAnimation(
            xTarget, 1f,
            0f, 1f,
            Animation.RELATIVE_TO_SELF, xReference,
            Animation.RELATIVE_TO_SELF, yReference
        )

        anim.duration = ANIMATION_DURATION
        view.visibility = View.VISIBLE;
        view.startAnimation(anim)
    }

    private fun hideView(view: View, bothAxis: Boolean = false) {
        val xDestination = if (bothAxis) 0f else 1f
        val xReference = if (bothAxis) 0.5f else 0f
        val yReference = if (bothAxis) 0.5f else 0f

        val anim: Animation = ScaleAnimation(
            1f, xDestination,
            1f, 0.2f,
            Animation.RELATIVE_TO_SELF, xReference,
            Animation.RELATIVE_TO_SELF, yReference
        )

        anim.duration = ANIMATION_DURATION
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        view.startAnimation(anim)
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "SpeechToText ready")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        if (rmsdB > 0) {
            val percentage = (0.5 + ((rmsdB / 10)))
            val anim_waves: Animation = ScaleAnimation(
                    1f, 1f,
                    0.5f, percentage.toFloat(),
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            )
            anim_waves.duration = 50
            fab.startAnimation(anim_waves)
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
        onResult(ActionParser.parse(message))
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d(TAG, "onPartialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent")
    }

    abstract fun onResult(action: ActionParser.Action)
}