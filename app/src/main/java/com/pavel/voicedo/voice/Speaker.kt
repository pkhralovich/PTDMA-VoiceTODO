package com.pavel.voicedo.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.*
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class Speaker {
    companion object {
        private lateinit var m_instance : TextToSpeech
        private lateinit var m_context: Context

        fun init(activity: Context, listener: OnInitListener, progressListener: UtteranceProgressListener ) {
            m_instance = TextToSpeech(activity, listener)
            m_instance.setOnUtteranceProgressListener(progressListener)
            m_context = activity
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

        fun speak(text: String) {
            m_instance.speak(text, QUEUE_FLUSH, null, this::class.java.name)
        }

        fun speak(text: Int) {
            speak(m_context.resources.getString(text))
        }

        fun destroy() {
            if (!this::m_instance.isInitialized) {
                m_instance.stop()
                m_instance.shutdown()
            }
        }
    }
}