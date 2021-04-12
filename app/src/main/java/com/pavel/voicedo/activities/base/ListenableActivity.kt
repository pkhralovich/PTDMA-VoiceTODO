package com.pavel.voicedo.activities.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pavel.voicedo.R
import com.pavel.voicedo.dialogs.HelpDialog
import com.pavel.voicedo.voice.ActionParser
import com.pavel.voicedo.voice.Speaker
import com.pavel.voicedo.voice.SpeakerProgressListener
import java.util.ArrayList

abstract class ListenableActivity : ToolbarActivity(), RecognitionListener, TextToSpeech.OnInitListener, SpeakerProgressListener.SpeakerController {
    companion object {
        const val MAX_LISTEN_TIMEOUT : Long = 10000
        const val ANIMATION_DURATION : Long = 150

        private const val TAG = "ListenableActivity"
    }

    @BindView(R.id.fab)
    lateinit var fab : FloatingActionButton

    @BindView(R.id.listener_waves)
    lateinit var listener_waves : ImageView
    @BindView(R.id.listener_micro)
    lateinit var listener_micro : ImageView
    @BindView(R.id.listener)
    lateinit var listener : View
    @BindView(R.id.listener_label)
    lateinit var listener_label: TextView

    lateinit var speechRecognizer: SpeechRecognizer
    private var viewsVisible: Boolean = false
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
        Speaker.init(this, this, getProgressListener())
    }

    @OnClick(R.id.fab)
    open fun onClickListen() {
        startListening()
    }

    @OnClick(R.id.info_icon)
    open fun onClickHelp() {
        HelpDialog(this, getHelpText()).show()
    }

    override fun onInit(status: Int) {
        Speaker.onInit(status)
    }

    abstract fun getHelpText() : List<String>

    private fun showView(view: View, bothAxis: Boolean = false, animate: Boolean = true) {
        val xTarget = if (bothAxis) 0f else 1f
        val xReference = if (bothAxis) 0.5f else 0f
        val yReference = if (bothAxis) 0.5f else 0f

        val anim: Animation = ScaleAnimation(
            xTarget, 1f,
            0f, 1f,
            Animation.RELATIVE_TO_SELF, xReference,
            Animation.RELATIVE_TO_SELF, yReference
        )

        if (animate) anim.duration = ANIMATION_DURATION
        else anim.duration = 0

        view.visibility = View.VISIBLE;
        view.startAnimation(anim)
    }

    private fun hideView(view: View, bothAxis: Boolean = false, animate: Boolean = true) {
        val xDestination = if (bothAxis) 0f else 1f
        val xReference = if (bothAxis) 0.5f else 0f
        val yReference = if (bothAxis) 0.5f else 0f

        val anim: Animation = ScaleAnimation(
            1f, xDestination,
            1f, 0.2f,
            Animation.RELATIVE_TO_SELF, xReference,
            Animation.RELATIVE_TO_SELF, yReference
        )

        if (animate) anim.duration = ANIMATION_DURATION
        else anim.duration = 0

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
            listener_waves.startAnimation(anim_waves)
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
        if(error == 8) {
            speechRecognizer.cancel()
            startListening()
        } else if (error == 7) {
            onNoOrder()
            hideListenable()
        }
        else if (error != 5) startListening()
    }

    private fun onNoOrder() {
        Speaker.speak(R.string.no_order_found, null, false)
    }

    override fun onResults(results: Bundle?) {
        Log.d(TAG, "onResults")

        var message = ""
        val data: ArrayList<String> = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!!
        for (i in 0 until data.size)
            message += data[i]

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        if (this.isWaitingInput()) onResult(ActionParser.Action(ActionParser.Action.eActionType.INPUT, message))
        else onResult(ActionParser.parse(message, listOf()))
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d(TAG, "onPartialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent")
    }

    abstract fun onResult(action: ActionParser.Action)

    fun getProgressListener() : SpeakerProgressListener {
        return SpeakerProgressListener(this)
    }

    override fun startListening() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)

            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.pavel.voicedo")

            showListenable(false)
            handler?.removeCallbacksAndMessages(null)

            handler = Handler(Looper.getMainLooper())
            handler!!.postDelayed({
                stopListening()
                hideListenable()
            }, MAX_LISTEN_TIMEOUT)



            speechRecognizer.startListening(intent)
            listener_micro.setImageResource(R.drawable.ic_microphone_primary)
        }, 50)
    }

    override fun stopListening() {
        Handler(Looper.getMainLooper()).post {
            handler?.removeCallbacksAndMessages(null)
            speechRecognizer.stopListening()
            listener_micro.setImageResource(R.drawable.ic_microphone_disabled_primary)
        }
    }

    fun hideListenable() {
        showView(fab, true)
        hideView(listener)
        viewsVisible = false
    }

    fun showListenable(animate: Boolean) {
        if (viewsVisible) return

        showView(listener, animate)
        hideView(fab, true, animate)
        viewsVisible = true
    }

    abstract fun isWaitingInput() : Boolean
}