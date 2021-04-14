package com.pavel.voicedo.listeners

import android.speech.tts.UtteranceProgressListener

class SpeakerProgressListener(private val controller: SpeakerController) : UtteranceProgressListener() {
    interface SpeakerController {
        fun startListening()
        fun stopListening()
    }

    var restartListenOnDone : Boolean = false

    override fun onStart(utteranceId: String?) {
        controller.stopListening()
    }

    override fun onDone(utteranceId: String?) {
        if (restartListenOnDone) controller.startListening()
    }

    override fun onError(utteranceId: String?) {
        controller.startListening()
    }
}