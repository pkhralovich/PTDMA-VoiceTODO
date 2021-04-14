package com.pavel.voicedo.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.*
import android.util.Log
import android.widget.TextView
import java.util.*

class Speaker {
    companion object {
        private lateinit var m_instance : TextToSpeech
        private lateinit var m_context: Context
        private lateinit var m_listener: SpeakerProgressListener

        fun init(activity: Context, listener: OnInitListener, progressListener: SpeakerProgressListener ) {
            m_context = activity
            m_instance = TextToSpeech(activity, listener)

            m_listener = progressListener
            m_instance.setOnUtteranceProgressListener(m_listener)
        }

        fun init(activity: Context, listener: OnInitListener) {
            m_context = activity
            m_instance = TextToSpeech(activity, listener)
        }

        fun onInit(status: Int) : Boolean {
            if (status == SUCCESS) {
                val result = m_instance.setLanguage(Locale.US)
                if (result == LANG_MISSING_DATA || result == LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                    return false
                }
                return true
            } else Log.e("TTS", "Unable to initialize speaker - ErrorCode: $status")
            return false
        }

        fun speak(text: String, view: TextView?, restartListening: Boolean = true) {
            m_listener.restartListenOnDone = restartListening
            m_instance.speak(text, QUEUE_FLUSH, null, this::class.java.name)
            if (view != null) view.text = text
        }

        fun speak(text: Int, view: TextView?, restartListening: Boolean = true) {
            speak(m_context.resources.getString(text), view, restartListening)
        }

        fun destroy() {
            //TODO: Veure si fer servir i com
            if (this::m_instance.isInitialized) {
                m_instance.stop()
                m_instance.shutdown()
            }
        }
    }
}