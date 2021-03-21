package com.pavel.voicedo.listeners

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HideFabOnScrollListener(private val fab: FloatingActionButton) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0 && fab.isShown) fab.hide()
        else if (dy < 0 && !fab.isShown) fab.show()
    }
}