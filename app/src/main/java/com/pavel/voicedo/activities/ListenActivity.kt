package com.pavel.voicedo.activities

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.voice.ActionParser
import com.pavel.voicedo.voice.Speaker
import com.pavel.voicedo.listeners.SpeakerProgressListener
import java.util.*


class ListenActivity : AppCompatActivity(), TextToSpeech.OnInitListener, RecognitionListener, SpeakerProgressListener.SpeakerController  {
    companion object {
        private const val TAG = "ListenActivity"

        enum class EnumStatus {
            WAITING_COMMAND,
            WAITING_REMOVE_LIST,
            WAITING_REMOVE_TASK,
            WAITING_REMOVE_EVENT
        }
    }

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.icon_waves)
    lateinit var iconWaves: View

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.microphone)
    lateinit var iconMicro: ImageView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.text_indicator)
    lateinit var textIndicator: TextView

    private lateinit var speechRecognizer: SpeechRecognizer

    private var status : EnumStatus = EnumStatus.WAITING_COMMAND
    private var previousAction : ActionParser.Action? = null

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listen)

        ButterKnife.bind(this)
        Speaker.init(this, this, SpeakerProgressListener(this))

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_cancel)
    fun onCancel() {
        this.finish()
    }

    override fun onInit(status: Int) {
        if (Speaker.onInit(status)) {
            Speaker.speak(R.string.response_how_can_help, textIndicator)
        }
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
            val animation: Animation = ScaleAnimation(
                    1f, 1f,
                    0.5f, percentage.toFloat(),
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            )
            animation.duration = 50
            iconWaves.startAnimation(animation)
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

        if (error == 7) {
            if (previousAction == null || previousAction?.action != ActionParser.Action.ActionType.BACK) {
                onNoOrder()
                finish()
            }
        }
        else if (error != 5) startListening()
    }

    override fun onResults(results: Bundle?) {
        Log.d(TAG, "onResults")

        var message = ""
        val data: ArrayList<String> = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!!
        for (i in 0 until data.size)
            message += data[i]

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        stopListening()

        val action = ActionParser.parse(message, getExpectedOrders())
        when (action.action) {
            ActionParser.Action.ActionType.NOT_UNDERSTAND -> onInvalidAction()
            ActionParser.Action.ActionType.NOT_EXPECTED -> onUnexpectedAction()
            else -> {
                when (this.status) {
                    EnumStatus.WAITING_COMMAND -> applyCommand(action)
                    else -> {
                        if (action.action == ActionParser.Action.ActionType.CONFIRMATION) {
                            val resultIntent = Intent()
                            resultIntent.putExtra(MainActivity.PARAM_ACTION, this.previousAction)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            Speaker.speak(R.string.response_remove_canceled, textIndicator)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun getExpectedOrders() : List<ActionParser.Action.ActionType> {
        return if (this.status == EnumStatus.WAITING_COMMAND) {
            listOf(
                ActionParser.Action.ActionType.CREATE_TASK,
                ActionParser.Action.ActionType.CREATE_EVENT,
                ActionParser.Action.ActionType.CREATE_LIST,
                ActionParser.Action.ActionType.VIEW_TASK,
                ActionParser.Action.ActionType.VIEW_EVENT,
                ActionParser.Action.ActionType.VIEW_LIST,
                ActionParser.Action.ActionType.DELETE_EVENT,
                ActionParser.Action.ActionType.DELETE_LIST,
                ActionParser.Action.ActionType.DELETE_TASK,
                ActionParser.Action.ActionType.SHOW_ALL_TASKS,
                ActionParser.Action.ActionType.SHOW_ALL_EVENTS,
                ActionParser.Action.ActionType.SHOW_ALL_LISTS,
                ActionParser.Action.ActionType.SHOW_UNDONE_TASKS,
                ActionParser.Action.ActionType.SHOW_TASKS_IN_PROCESS,
                ActionParser.Action.ActionType.SHOW_EVENTS_DAY,
                ActionParser.Action.ActionType.SHOW_EVENTS_CURRENT_WEEK,
                ActionParser.Action.ActionType.SHOW_EVENTS_NEXT_WEEK,
                ActionParser.Action.ActionType.SHOW_LOCATION,
                ActionParser.Action.ActionType.BACK,
                ActionParser.Action.ActionType.HELP
            )
        } else {
            listOf(
                ActionParser.Action.ActionType.CONFIRMATION,
                ActionParser.Action.ActionType.CANCELLATION,
                ActionParser.Action.ActionType.HELP
            )
        }
    }

    private fun applyCommand(action: ActionParser.Action) {
        when (action.action) {
            ActionParser.Action.ActionType.DELETE_TASK -> {
                this.status = EnumStatus.WAITING_REMOVE_TASK
                this.previousAction = action
                Speaker.speak(resources.getString(R.string.response_confirm_remove_task, action.param), textIndicator)
            }
            ActionParser.Action.ActionType.DELETE_LIST -> {
                this.status = EnumStatus.WAITING_REMOVE_LIST
                this.previousAction = action
                Speaker.speak(resources.getString(R.string.response_confirm_remove_list, action.param), textIndicator)
            }
            ActionParser.Action.ActionType.DELETE_EVENT -> {
                this.status = EnumStatus.WAITING_REMOVE_EVENT
                this.previousAction = action
                Speaker.speak(resources.getString(R.string.response_confirm_remove_event, action.param), textIndicator)
            }
            ActionParser.Action.ActionType.BACK,
            ActionParser.Action.ActionType.CANCELLATION -> {
                this.previousAction = action
                this.finish()
            }
            else -> {
                if (ListenableActivity.isCalendarAction(action) && !ListenableActivity.hasCalendarPermissions(this))
                    Speaker.speak(R.string.unable_to_calendar, textIndicator)
                else {
                    val resultIntent = Intent()
                    resultIntent.putExtra(MainActivity.PARAM_ACTION, action)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    private fun onInvalidAction() {
        Speaker.speak(R.string.response_not_understand, textIndicator)
    }

    private fun onUnexpectedAction() {
        Speaker.speak(R.string.unexpected_action, textIndicator)
    }

    private fun onNoOrder() {
        Speaker.speak(R.string.response_no_order_found, null, false)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d(TAG, "onPartialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent")
    }

    override fun startListening() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)

            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.pavel.voicedo")

            handler?.removeCallbacksAndMessages(null)
            handler = Handler(Looper.getMainLooper())
            handler!!.postDelayed({
                stopListening()
                finish()
            }, ListenableActivity.MAX_LISTEN_TIMEOUT)

            speechRecognizer.startListening(intent)
            iconMicro.setImageResource(R.drawable.ic_microphone)
        }, 50)
    }

    override fun stopListening() {
        Handler(Looper.getMainLooper()).post {
            handler?.removeCallbacksAndMessages(null)
            speechRecognizer.stopListening()
            iconMicro.setImageResource(R.drawable.ic_microphone_disabled)
        }
    }
}