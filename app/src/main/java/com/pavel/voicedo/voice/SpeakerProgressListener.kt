package com.pavel.voicedo.voice

import android.speech.tts.UtteranceProgressListener

class SpeakerProgressListener(controller: SpeakerController) : UtteranceProgressListener() {
    interface SpeakerController {
        fun startListening()
        fun stopListening()
    }

    private val controller = controller
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