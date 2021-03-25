package com.pavel.voicedo.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.*
import android.util.Log
import java.util.*

class Speaker private constructor(c: Context) {
    companion object {
        private lateinit var m_instance : TextToSpeech

        /*fun instance(): TextToSpeech {
            if (!this::m_instance.isInitialized) throw Exception("Speaker not initialized")
            return m_instance
        }*/

        /*fun init(activity: Context) {
            m_instance = TextToSpeech(activity) { onInit(it) }
        }*/

        fun init(activity: Context, listener: OnInitListener) {
            m_instance = TextToSpeech(activity, listener)
        }

        fun onInit(status: Int) : Boolean {
            if (status == SUCCESS) {
                val result = m_instance!!.setLanguage(Locale.US)
                if (result == LANG_MISSING_DATA || result == LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                    return false
                }
                return true
            } else Log.e("TTS", "Unable to initialize speaker")
            return false
        }

        fun speak(text: String) {
            m_instance.speak(text, TextToSpeech.QUEUE_FLUSH, null, "abc")
        }

        fun speak(text: Int) {

        }

        fun destroy() {
            if (!this::m_instance.isInitialized) {
                m_instance.stop()
                m_instance.shutdown()
            }
        }
    }
}