package com.pavel.voicedo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pavel.voicedo.R
import com.pavel.voicedo.models.BaseTask
import com.pavel.voicedo.models.Event
import com.pavel.voicedo.models.ShoppingList
import com.pavel.voicedo.models.Task

class TodoAdapter(_data: List<BaseTask>, _controller: Controller) : RecyclerView.Adapter<TodoAdapter.BaseViewHolder>() {
    interface Controller {
        fun onClickItem(item: View)
    }

    var data : List<BaseTask> = _data
    private val controller: Controller = _controller

    override fun getItemViewType(position: Int): Int {
        return data[position].type.value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when(viewType) {
            BaseTask.EnumTypes.TASK.value -> {
                val v: View = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
                v.setOnClickListener { view -> controller.onClickItem(view) }
                TaskViewHolder(v)
            }
            BaseTask.EnumTypes.EVENT.value -> {
                val v: View = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
                v.setOnClickListener { view -> controller.onClickItem(view) }
                EventViewHolder(v)
            }
            BaseTask.EnumTypes.LIST.value -> {
                val v: View = LayoutInflater.from(parent.context).inflate(R.layout.shopping_list_item, parent, false)
                v.setOnClickListener { view -> controller.onClickItem(view) }
                ListViewHolder(v)
            }
            else -> throw Exception("Unexpected viewType")
        }
    }

    private fun onBindTask(holderInstance: TaskViewHolder, taskInstance: Task) {
        holderInstance.labelId.text = holderInstance.itemView.context.resources.getString(R.string.task_id_placeholder, taskInstance.id)
        holderInstance.labelName.text = taskInstance.description

        holderInstance.imageDoing.setImageResource(R.drawable.ic_uncompleted)
        holderInstance.imageTodo.setImageResource(R.drawable.ic_uncompleted)
        holderInstance.imageDone.setImageResource(R.drawable.ic_uncompleted)
        holderInstance.separatorDoingDone.setBackgroundColor(ContextCompat.getColor(holderInstance.itemView.context, R.color.separator))
        holderInstance.separatorTodoDoing.setBackgroundColor(ContextCompat.getColor(holderInstance.itemView.context, R.color.separator))

        when (taskInstance.state) {
            Task.EnumTaskState.DOING -> {
                holderInstance.imageTodo.setImageResource(R.drawable.ic_completed)
                holderInstance.imageDoing.setImageResource(R.drawable.ic_completed)
                holderInstance.separatorTodoDoing.setBackgroundColor(ContextCompat.getColor(holderInstance.itemView.context, R.color.primary))
            }
            Task.EnumTaskState.TODO -> {
                holderInstance.imageTodo.setImageResource(R.drawable.ic_completed)
            }
            Task.EnumTaskState.DONE -> {
                holderInstance.imageTodo.setImageResource(R.drawable.ic_completed)
                holderInstance.imageDoing.setImageResource(R.drawable.ic_completed)
                holderInstance.imageDone.setImageResource(R.drawable.ic_completed_last)
                holderInstance.separatorTodoDoing.setBackgroundColor(ContextCompat.getColor(holderInstance.itemView.context, R.color.primary))
                holderInstance.separatorDoingDone.setBackgroundColor(ContextCompat.getColor(holderInstance.itemView.context, R.color.primary))
            }
            else -> {}
        }
    }

    private fun onBindEvent(holderInstance: EventViewHolder, eventInstance: Event) {
        holderInstance.labelDate.text = eventInstance.getStringDate()
        holderInstance.labelId.text = holderInstance.itemView.context.resources.getString(R.string.event_id_placeholder, eventInstance.id)
        holderInstance.labelName.text = eventInstance.description
    }

    private fun onBindList(holderInstance: ListViewHolder, listInstance: ShoppingList) {
        holderInstance.labelName.text = listInstance.description
        holderInstance.labelCount.text = listInstance.getStringProductsCount()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val task : BaseTask = data[position]

        when (task.type.value) {
            BaseTask.EnumTypes.TASK.value -> onBindTask(holder as TaskViewHolder, task as Task)
            BaseTask.EnumTypes.EVENT.value -> onBindEvent(holder as EventViewHolder, task as Event)
            BaseTask.EnumTypes.LIST.value -> onBindList(holder as ListViewHolder, task as ShoppingList)
        }
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    abstract class BaseViewHolder(_view: View) : RecyclerView.ViewHolder(_view) {
        var labelName: TextView = _view.findViewById(R.id.label_name)
    }

    class TaskViewHolder(_view: View) : BaseViewHolder(_view) {
        var labelId: TextView = _view.findViewById(R.id.label_id)
        var imageTodo: ImageView = _view.findViewById(R.id.state_todo)
        var imageDoing: ImageView = _view.findViewById(R.id.state_doing)
        var imageDone: ImageView = _view.findViewById(R.id.state_completed)
        var separatorTodoDoing: View = _view.findViewById(R.id.first_horizontal_rule)
        var separatorDoingDone: View = _view.findViewById(R.id.second_horizontal_rule)
    }

    class EventViewHolder(_view: View) : BaseViewHolder(_view) {
        var labelId: TextView = _view.findViewById(R.id.label_id)
        var labelDate: TextView = _view.findViewById(R.id.label_date)
    }

    class ListViewHolder(_view: View) : BaseViewHolder(_view) {
        var labelCount: TextView = _view.findViewById(R.id.label_count)
    }
}