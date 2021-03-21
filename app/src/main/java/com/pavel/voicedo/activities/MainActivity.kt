package com.pavel.voicedo.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pavel.voicedo.R
import com.pavel.voicedo.adapters.TodoAdapter
import com.pavel.voicedo.models.*
import org.joda.time.DateTime

class MainActivity : AppCompatActivity(), TodoAdapter.Controller{
    @BindView(R.id.recycler_view)
    lateinit var recycler : RecyclerView
    @BindView(R.id.fab)
    lateinit var fab : FloatingActionButton

    lateinit var list : ArrayList<BaseTask>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        list = arrayListOf()
        list.add(Event(1, "Event test 1", DateTime.now()))
        list.add(Task(2, "Task test 1", Task.eTaskState.TODO))
        list.add(Task(3, "Task test 2", Task.eTaskState.DOING))
        list.add(Task(4, "Task test 3", Task.eTaskState.DONE))
        list.add(Event(5, "Event test 2", DateTime.now()))
        list.add(ShoppingList(6, "List test 1", arrayListOf()))
        list.add(Task(7, "Task test 4", Task.eTaskState.DOING))
        list.add(Task(8, "Task test 5", Task.eTaskState.DONE))
        list.add(Event(9, "Event test 3", DateTime.now()))
        list.add(ShoppingList(10, "List test 2", arrayListOf()))
        list.add(ShoppingList(11, "List test 3", arrayListOf()))

        recycler.addOnScrollListener(HideFabListener(fab))
        recycler.adapter = TodoAdapter(list, this)
    }

    class HideFabListener(private val fab: FloatingActionButton) : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0 && fab.isShown) fab.hide()
            else if (dy < 0 && !fab.isShown) fab.show()
        }
    }

    override fun onClickItem(item: View) {
        val itemPosition: Int = recycler.getChildLayoutPosition(item)
        val task : BaseTask = list.get(itemPosition)

        when (task.type) {
            BaseTask.eTypes.TASK -> {
                val intent = Intent(this, TaskActivity::class.java)
                intent.putExtra(PARAMS.TASK, task as Task)
                startActivity(intent)
            }
            BaseTask.eTypes.EVENT -> {
                val intent = Intent(this, EventActivity::class.java)
                intent.putExtra(PARAMS.EVENT, task as Event)
                startActivity(intent)
            }
            BaseTask.eTypes.LIST -> {
                val intent = Intent(this, ListActivity::class.java)
                intent.putExtra(PARAMS.LIST, task as ShoppingList)
                startActivity(intent)
            }
            else -> { }
        }
    }

    class PARAMS {
        companion object {
            const val TASK : String = "TASK"
            const val LIST : String = "LIST"
            const val EVENT : String = "EVENT"
        }
    }
}