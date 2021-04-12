package com.pavel.voicedo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.models.BaseTask
import com.pavel.voicedo.models.Event
import com.pavel.voicedo.models.ShoppingList
import com.pavel.voicedo.models.Task

class TodoAdapter(_data: List<BaseTask>, _controller: Controller) : RecyclerView.Adapter<TodoAdapter.BaseViewHolder>() {
    interface Controller {
        fun onClickItem(item: View)
    }

    public var data : List<BaseTask> = _data
    private val controller: Controller = _controller

    override fun getItemViewType(position: Int): Int {
        return data.get(position).type.value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when(viewType) {
            BaseTask.eTypes.TASK.value -> {
                val v: View = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
                v.setOnClickListener { view -> controller.onClickItem(view) }
                TaskViewHolder(v)
            }
            BaseTask.eTypes.EVENT.value -> {
                val v: View = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
                v.setOnClickListener { view -> controller.onClickItem(view) }
                EventViewHolder(v)
            }
            BaseTask.eTypes.LIST.value -> {
                val v: View = LayoutInflater.from(parent.context).inflate(R.layout.shopping_list_item, parent, false)
                v.setOnClickListener { view -> controller.onClickItem(view) }
                ListViewHolder(v)
            }
            else -> throw Exception("Unexpected viewType")
        }
    }

    private fun onBindTask(holder_instance: TaskViewHolder, task_instance: Task) {
        holder_instance.label_id.text = "T${task_instance.id}"
        holder_instance.label_name.text = task_instance.description

        holder_instance.image_doing.setImageResource(R.drawable.ic_uncompleted)
        holder_instance.image_todo.setImageResource(R.drawable.ic_uncompleted)
        holder_instance.image_done.setImageResource(R.drawable.ic_uncompleted)
        holder_instance.separator_doing_done.setBackgroundColor(ContextCompat.getColor(holder_instance.itemView.context, R.color.separator))
        holder_instance.separator_todo_doing.setBackgroundColor(ContextCompat.getColor(holder_instance.itemView.context, R.color.separator))

        when (task_instance.state) {
            Task.eTaskState.DOING -> {
                holder_instance.image_todo.setImageResource(R.drawable.ic_completed)
                holder_instance.image_doing.setImageResource(R.drawable.ic_completed)
                holder_instance.separator_todo_doing.setBackgroundColor(ContextCompat.getColor(holder_instance.itemView.context, R.color.primary))
            }
            Task.eTaskState.TODO -> {
                holder_instance.image_todo.setImageResource(R.drawable.ic_completed)
            }
            Task.eTaskState.DONE -> {
                holder_instance.image_todo.setImageResource(R.drawable.ic_completed)
                holder_instance.image_doing.setImageResource(R.drawable.ic_completed)
                holder_instance.image_done.setImageResource(R.drawable.ic_completed_last)
                holder_instance.separator_todo_doing.setBackgroundColor(ContextCompat.getColor(holder_instance.itemView.context, R.color.primary))
                holder_instance.separator_doing_done.setBackgroundColor(ContextCompat.getColor(holder_instance.itemView.context, R.color.primary))
            }
            else -> {}
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val task : BaseTask = data.get(position)

        when (task.type.value) {
            BaseTask.eTypes.TASK.value -> onBindTask(holder as TaskViewHolder, task as Task)
            BaseTask.eTypes.EVENT.value -> {
                val holder_instance = holder as EventViewHolder
                val event_instance = task as Event

                holder_instance.label_date.text = event_instance.getStringDate()
                holder_instance.label_id.text = "C${event_instance.id}"
                holder_instance.label_name.text = event_instance.description
            }
            BaseTask.eTypes.LIST.value -> {
                val holder_instance = holder as ListViewHolder
                val shopping_list_instance = task as ShoppingList

                holder_instance.label_name.text = shopping_list_instance.description
                holder_instance.label_count.text = shopping_list_instance.getStringProductsCount()
            }
        }
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    abstract class BaseViewHolder : RecyclerView.ViewHolder {
        @BindView(R.id.label_name)
        lateinit var label_name: TextView

        constructor(_view: View) : super(_view) {
            ButterKnife.bind(this, _view)
        }
    }

    class TaskViewHolder(_view: View) : BaseViewHolder(_view) {
        @BindView(R.id.label_id)
        lateinit var label_id: TextView

        @BindView(R.id.state_todo)
        lateinit var image_todo: ImageView

        @BindView(R.id.state_doing)
        lateinit var image_doing: ImageView

        @BindView(R.id.state_completed)
        lateinit var image_done: ImageView

        @BindView(R.id.first_horizontal_rule)
        lateinit var separator_todo_doing: View

        @BindView(R.id.second_horizontal_rule)
        lateinit var separator_doing_done: View
    }

    class EventViewHolder(_view: View) : BaseViewHolder(_view) {
        @BindView(R.id.label_id)
        lateinit var label_id: TextView

        @BindView(R.id.label_date)
        lateinit var label_date: TextView
    }

    class ListViewHolder(_view: View) : BaseViewHolder(_view) {
        @BindView(R.id.label_count)
        lateinit var label_count: TextView
    }
}