package com.pavel.voicedo.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.adapters.TodoAdapter
import com.pavel.voicedo.models.*
import org.joda.time.DateTime
import android.Manifest
import android.os.Debug
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import butterknife.OnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ToolbarActivity
import com.pavel.voicedo.dialogs.MainHelpDialog
import com.pavel.voicedo.listeners.HideFabOnScrollListener
import com.pavel.voicedo.voice.ActionParser
import com.pavel.voicedo.voice.Speaker


class MainActivity : ToolbarActivity(), TodoAdapter.Controller, TextToSpeech.OnInitListener {
    class PARAMS {
        companion object {
            const val TASK : String = "TASK"
            const val LIST : String = "LIST"
            const val EVENT : String = "EVENT"
        }
    }

    companion object {
        const val REQUEST_CODE : Int = 100
        const val PERMISSION_REQUEST_LOCATION = 99
        const val PARAM_ACTION = "action"
    }

    @BindView(R.id.recycler_view)
    lateinit var recycler : RecyclerView
    @BindView(R.id.fab)
    lateinit var fab : FloatingActionButton

    lateinit var list : ArrayList<BaseTask>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        val products = arrayListOf(Product("Best product 1", false),
                                            Product("Best product 2", true),
                                            Product("Best product 3", true),
                                            Product("Best product 4", false),
                                            Product("Best product 5", false),
                                            Product("Best product 6", true),
                                            Product("Best product 7", false),
                                            Product("Best product 8", true),
                                            Product("Best product 9", false),
                                            Product("Best product 10", true))

        Speaker.init(this, this)

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
        list.add(ShoppingList(10, "List test 2", products))
        list.add(ShoppingList(11, "List test 3", products))

        recycler.addOnScrollListener(HideFabOnScrollListener(fab))
        recycler.adapter = TodoAdapter(list, this)

        checkPermissions()
    }

    @OnClick(R.id.fab)
    fun onClickListen() {
        val i = Intent(this, ListenActivity::class.java)
        startActivityForResult(i, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                var action : ActionParser.Action? = null;
                if (data.hasExtra(PARAM_ACTION)) action = data.getSerializableExtra(PARAM_ACTION) as ActionParser.Action

                if (action != null) onResult(action)
            }
        }
    }

    fun onResult(action: ActionParser.Action) {
        when (action.action) {
            ActionParser.Action.eActionType.HELP -> onClickHelp()
            ActionParser.Action.eActionType.VIEW_TASK -> viewTask(action.param!!)
            ActionParser.Action.eActionType.DELETE_TASK -> deleteTask(action.param!!)
            ActionParser.Action.eActionType.CREATE_TASK -> createTask()
            ActionParser.Action.eActionType.VIEW_EVENT -> viewEvent(action.param!!)
            ActionParser.Action.eActionType.DELETE_EVENT -> deleteEvent(action.param!!)
            ActionParser.Action.eActionType.CREATE_EVENT -> createEvent()
            ActionParser.Action.eActionType.VIEW_LIST -> viewList(action.param!!)
            ActionParser.Action.eActionType.DELETE_LIST -> deleteList(action.param!!)
            ActionParser.Action.eActionType.CREATE_LIST -> createList()
            ActionParser.Action.eActionType.CANCELATION -> {}
            else -> Speaker.speak(R.string.response_not_unserstand)
        }
    }

    private fun createList() {
        Speaker.speak(R.string.response_creating_list)
        openList(null)
    }

    private fun createTask() {
        Speaker.speak(R.string.response_creating_task)
        openTask(null)
    }

    private fun createEvent() {
        Speaker.speak(R.string.response_creating_event)
        openEvent(null)
    }

    private fun viewTask(task_id: String) {
        val task : Task? = BaseTask.getTask(list, task_id)

        if (task == null) Speaker.speak(R.string.response_task_not_found)
        else {
            Speaker.speak(resources.getString(R.string.response_opening_task, task_id))
            openTask(task)
        }
    }

    private fun deleteTask(list_id: String) {
        val event : ShoppingList? = BaseTask.getList(list, list_id)

        if (event == null) Speaker.speak(R.string.response_list_not_found)
        else {
            //TODO: Fisical remove
            Speaker.speak(R.string.response_removing_list)
        }
    }

    private fun viewEvent(event_id: String) {
        val event : Event? = BaseTask.getEvent(list, event_id)

        if (event == null) Speaker.speak(R.string.response_event_not_found)
        else {
            Speaker.speak(resources.getString(R.string.response_opening_event, event_id))
            openEvent(event)
        }
    }

    private fun deleteEvent(event_id: String) {
        val event : Event? = BaseTask.getEvent(list, event_id)

        if (event == null) Speaker.speak(R.string.response_event_not_found)
        else {
            //TODO: Fisical remove
            Speaker.speak(R.string.response_removing_event)
        }
    }

    private fun viewList(list_id: String) {
        val list : ShoppingList? = BaseTask.getList(list, list_id)

        if (list == null) Speaker.speak(R.string.response_list_not_found)
        else {
            Speaker.speak(resources.getString(R.string.response_opening_list, list_id))
            openList(list)
        }
    }

    private fun deleteList(list_id: String) {
        val list : ShoppingList? = BaseTask.getList(list, list_id)

        if (list == null) Speaker.speak(R.string.response_list_not_found)
        else {
            //TODO: Fisical remove
            Speaker.speak(R.string.response_removing_list)
        }
    }

    override fun onInit(status: Int) { }

    @OnClick(R.id.info_icon)
    fun onClickHelp() {
        MainHelpDialog(this).show()
    }

    override fun onClickItem(item: View) {
        val itemPosition: Int = recycler.getChildLayoutPosition(item)
        val task : BaseTask = list[itemPosition]

        when (task.type) {
            BaseTask.eTypes.TASK -> openTask(task as Task)
            BaseTask.eTypes.EVENT -> openEvent(task as Event)
            BaseTask.eTypes.LIST -> openList(task as ShoppingList)
            else -> { }
        }
    }

    private fun openTask(task: Task?) {
        val intent = Intent(this, TaskActivity::class.java)

        if (task != null) intent.putExtra(PARAMS.TASK, task)

        startActivity(intent)
    }

    private fun openEvent(event: Event?) {
        val intent = Intent(this, EventActivity::class.java)

        if (event != null) intent.putExtra(PARAMS.EVENT, event)

        startActivity(intent)
    }

    private fun openList(list: ShoppingList?) {
        val intent = Intent(this, ListActivity::class.java)

        if (list != null) intent.putExtra(PARAMS.LIST, list)

        startActivity(intent)
    }

    //region CHECK_LOCATION_PERMISSION
    private fun checkPermissions() {
        val permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionMicrophone = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        val permissions : ArrayList<String> = ArrayList()
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionMicrophone != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.RECORD_AUDIO)

        if (permissions.size > 0) {
            ActivityCompat.requestPermissions(
                    this@MainActivity,
                    permissions.toTypedArray(),
                    PERMISSION_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                if (permissions.isNotEmpty()) {
                    for ((index, permission: String) in permissions.withIndex()) {
                        when (permission) {
                            Manifest.permission.ACCESS_FINE_LOCATION -> onLocationPermission(grantResults[index])
                            Manifest.permission.RECORD_AUDIO -> onAudioPermission(grantResults[index])
                        }
                    }
                }
            }
        }
    }

    private fun onLocationPermission(result: Int) {
        if (result == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, R.string.location_permission_granted, Toast.LENGTH_LONG).show()
        else
            Toast.makeText(this, R.string.location_permission_not_granted, Toast.LENGTH_LONG).show()
    }

    private fun onAudioPermission(result: Int) {
        if (result == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, R.string.microphone_permission_granted, Toast.LENGTH_LONG).show()
        else
            Toast.makeText(this, R.string.microphone_permission_not_granted, Toast.LENGTH_LONG).show()
    }
    //endregion
}