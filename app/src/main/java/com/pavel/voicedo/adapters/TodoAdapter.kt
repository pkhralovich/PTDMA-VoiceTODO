package com.pavel.voicedo.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pavel.voicedo.R
import com.pavel.voicedo.models.BaseTask

class TodoAdapter : RecyclerView.Adapter<TodoAdapter.BaseViewHolder> {
    private val data : List<BaseTask>

    constructor(_data: List<BaseTask>) : super() {
        data = _data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    abstract class BaseViewHolder : RecyclerView.ViewHolder {
        val label_name: TextView

        constructor(_view: View) : super(_view) {
            label_name = _view.findViewById(R.id.label_name)
        }
    }

    class TaskViewHolder : BaseViewHolder {
        val label_date: TextView

        val states: Array<View>
        val 
    }

    class EventViewHolder : BaseViewHolder {
        val
    }

    class ListViewHolder : BaseViewHolder {

    }
}