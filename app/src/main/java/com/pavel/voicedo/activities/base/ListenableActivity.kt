package com.pavel.voicedo.activities.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.view.isVisible
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pavel.voicedo.R
import com.pavel.voicedo.dialogs.HelpDialog


abstract class ListenableActivity : ToolbarActivity() {
    companion object {
        const val MAX_LISTEN_TIMEOUT : Long = 3000
        const val ANIMATION_DURATION : Long = 150
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
}