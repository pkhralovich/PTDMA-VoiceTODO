package com.pavel.voicedo.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orm.SugarRecord
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ToolbarActivity
import com.pavel.voicedo.adapters.TodoAdapter
import com.pavel.voicedo.dialogs.MainHelpDialog
import com.pavel.voicedo.listeners.HideFabOnScrollListener
import com.pavel.voicedo.models.*
import com.pavel.voicedo.voice.ActionParser
import com.pavel.voicedo.voice.Speaker
import org.joda.time.DateTime
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : ToolbarActivity(), TodoAdapter.Controller, TextToSpeech.OnInitListener {
    class PARAMS {
        companion object {
            const val TASK : String = "TASK"
            const val TASK_ID : String = "TASK_ID"
            const val LIST : String = "LIST"
            const val LIST_ID : String = "LIST_ID"
            const val EVENT : String = "EVENT"
        }
    }

    companion object {
        const val REQUEST_CODE : Int = 100
        const val PERMISSION_REQUEST_LOCATION = 99
        const val PARAM_ACTION = "action"
    }

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_view)
    lateinit var recycler : RecyclerView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.fab)
    lateinit var fab : FloatingActionButton

    private var list : ArrayList<BaseTask> = ArrayList()
    private var currentFilter : ActionParser.Action? = null
    private lateinit var locationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)
        Speaker.init(this, this)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        recycler.addOnScrollListener(HideFabOnScrollListener(fab))

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun updateUI() {
        list.clear()
        list.addAll(SugarRecord.listAll(Event::class.java))
        list.addAll(SugarRecord.listAll(Task::class.java))
        list.addAll(SugarRecord.listAll(ShoppingList::class.java))

        if (this.currentFilter != null) {
            list = when (currentFilter?.action) {
                ActionParser.Action.ActionType.SHOW_ALL_TASKS -> list.filter { it.type == BaseTask.EnumTypes.TASK }
                ActionParser.Action.ActionType.SHOW_ALL_EVENTS -> list.filter { it.type == BaseTask.EnumTypes.EVENT }
                ActionParser.Action.ActionType.SHOW_ALL_LISTS -> list.filter { it.type == BaseTask.EnumTypes.LIST }
                ActionParser.Action.ActionType.SHOW_UNDONE_TASKS -> {
                    list.filter {
                        if (it.type == BaseTask.EnumTypes.TASK) {
                            val item = it as Task
                            item.state != Task.EnumTaskState.DONE
                        } else false
                    }
                }
                ActionParser.Action.ActionType.SHOW_TASKS_IN_PROCESS -> {
                    list.filter {
                        if (it.type == BaseTask.EnumTypes.TASK) {
                            val item = it as Task
                            item.state == Task.EnumTaskState.DOING
                        } else false
                    }
                }
                ActionParser.Action.ActionType.SHOW_EVENTS_DAY -> {
                    list.filter {
                        if (it.type == BaseTask.EnumTypes.EVENT) {
                            val item = it as Event
                            val itemDate = DateTime(item.date)
                            val weekday = this.currentFilter?.param
                            val itemWeekday = DayOfWeek.of(itemDate.dayOfWeek).getDisplayName(
                                TextStyle.FULL,
                                Locale.ENGLISH
                            )
                            itemWeekday.equals(weekday, ignoreCase = true)
                        } else false
                    }
                }
                ActionParser.Action.ActionType.SHOW_EVENTS_CURRENT_WEEK -> {
                    //val now = LocalDate.now()
                    //val monday: LocalDate = now.withDayOfWeek(DateTimeConstants.MONDAY)
                    //TODO
                    list.filter { it.type == BaseTask.EnumTypes.TASK }
                }
                ActionParser.Action.ActionType.SHOW_EVENTS_NEXT_WEEK -> {
                    //TODO
                    list.filter { it.type == BaseTask.EnumTypes.TASK }
                }
                else -> list
            } as ArrayList<BaseTask>
        }

        list.sortBy { it.creationDate }

        if (recycler.adapter == null) recycler.adapter = TodoAdapter(list, this)
        else {
            (recycler.adapter as TodoAdapter).data = list
            recycler.adapter!!.notifyDataSetChanged()
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.fab)
    fun onClickListen() {
        val i = Intent(this, ListenActivity::class.java)
        startActivityForResult(i, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                var action : ActionParser.Action? = null
                if (data.hasExtra(PARAM_ACTION)) action = data.getSerializableExtra(PARAM_ACTION) as ActionParser.Action

                if (action != null) onResult(action)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun showLocation() {
        this.locationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                Toast.makeText(
                    this,
                    resources.getString(R.string.response_location, addresses[0].locality),
                    Toast.LENGTH_LONG
                ).show()
            }
            else Toast.makeText(this, getString(R.string.response_no_location_detected), Toast.LENGTH_LONG).show()
        }
    }

    private fun onResult(action: ActionParser.Action) {
        when (action.action) {
            ActionParser.Action.ActionType.HELP -> onClickHelp()

            ActionParser.Action.ActionType.SHOW_LOCATION -> showLocation()

            ActionParser.Action.ActionType.SHOW_ALL_TASKS,
            ActionParser.Action.ActionType.SHOW_ALL_EVENTS,
            ActionParser.Action.ActionType.SHOW_ALL_LISTS,
            ActionParser.Action.ActionType.SHOW_UNDONE_TASKS,
            ActionParser.Action.ActionType.SHOW_TASKS_IN_PROCESS,
            ActionParser.Action.ActionType.SHOW_EVENTS_DAY,
            ActionParser.Action.ActionType.SHOW_EVENTS_CURRENT_WEEK,
            ActionParser.Action.ActionType.SHOW_EVENTS_NEXT_WEEK -> {
                currentFilter = action
                updateUI()
            }

            ActionParser.Action.ActionType.VIEW_TASK -> viewTask(action.param!!)
            ActionParser.Action.ActionType.DELETE_TASK -> deleteTask(action.param!!)
            ActionParser.Action.ActionType.CREATE_TASK -> createTask()

            ActionParser.Action.ActionType.VIEW_EVENT -> viewEvent(action.param!!)
            ActionParser.Action.ActionType.DELETE_EVENT -> deleteEvent(action.param!!)
            ActionParser.Action.ActionType.CREATE_EVENT -> createEvent()

            ActionParser.Action.ActionType.VIEW_LIST -> viewList(action.param!!)
            ActionParser.Action.ActionType.DELETE_LIST -> deleteList(action.param!!)
            ActionParser.Action.ActionType.CREATE_LIST -> createList()

            ActionParser.Action.ActionType.CANCELATION -> {
            }
            else -> Speaker.speak(R.string.response_not_unserstand, null)
        }
    }

    private fun createList() {
        Speaker.speak(R.string.response_creating_list, null)
        openList(null)
    }

    private fun createTask() {
        Speaker.speak(R.string.response_creating_task, null)
        openTask(null)
    }

    private fun createEvent() {
        Speaker.speak(R.string.response_creating_event, null)
        openEvent(null)
    }

    private fun viewTask(task_id: String) {
        val task : Task? = BaseTask.getTask(list, task_id)

        if (task == null) Speaker.speak(R.string.response_task_not_found, null)
        else {
            Speaker.speak(resources.getString(R.string.response_opening_task, task_id), null)
            openTask(task)
        }
    }

    private fun deleteTask(list_id: String) {
        val task : Task? = BaseTask.getTask(list, list_id)

        if (task == null) Speaker.speak(R.string.response_list_not_found, null)
        else {
            task.delete()
            Speaker.speak(R.string.response_removing_list, null)
            updateUI()
        }
    }

    private fun viewEvent(event_id: String) {
        val event : Event? = BaseTask.getEvent(list, event_id)

        if (event == null) Speaker.speak(R.string.response_event_not_found, null)
        else {
            Speaker.speak(resources.getString(R.string.response_opening_event, event_id), null)
            openEvent(event)
        }
    }

    private fun deleteEvent(event_id: String) {
        val event : Event? = BaseTask.getEvent(list, event_id)

        if (event == null) Speaker.speak(R.string.response_event_not_found, null)
        else {
            event.delete()
            Speaker.speak(R.string.response_removing_event, null)
            updateUI()
        }
    }

    private fun viewList(list_id: String) {
        val list : ShoppingList? = BaseTask.getList(list, list_id)

        if (list == null) Speaker.speak(R.string.response_list_not_found, null)
        else {
            Speaker.speak(resources.getString(R.string.response_opening_list, list_id), null)
            openList(list)
        }
    }

    private fun deleteList(list_id: String) {
        val list : ShoppingList? = BaseTask.getList(list, list_id)

        if (list == null) Speaker.speak(R.string.response_list_not_found, null)
        else {
            list.delete()
            Speaker.speak(R.string.response_removing_list, null)
            updateUI()
        }
    }

    override fun onInit(status: Int) {
        Speaker.onInit(status)
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.info_icon)
    fun onClickHelp() {
        MainHelpDialog(this).show()
    }

    override fun onClickItem(item: View) {
        val itemPosition: Int = recycler.getChildLayoutPosition(item)
        val task : BaseTask = list[itemPosition]

        when (task.type) {
            BaseTask.EnumTypes.TASK -> openTask(task as Task)
            BaseTask.EnumTypes.EVENT -> openEvent(task as Event)
            BaseTask.EnumTypes.LIST -> openList(task as ShoppingList)
            else -> { }
        }
    }

    private fun openTask(task: Task?) {
        val intent = Intent(this, TaskActivity::class.java)

        if (task != null) {
            intent.putExtra(PARAMS.TASK_ID, task.id)
            intent.putExtra(PARAMS.TASK, task)
        }

        startActivity(intent)
    }

    private fun openEvent(event: Event?) {
        val intent = Intent(this, EventActivity::class.java)

        if (event != null) intent.putExtra(PARAMS.EVENT, event)

        startActivity(intent)
    }

    private fun openList(list: ShoppingList?) {
        val intent = Intent(this, ListActivity::class.java)

        if (list != null) {
            intent.putExtra(PARAMS.LIST_ID, list.id)
            intent.putExtra(PARAMS.LIST, list)
        }

        startActivity(intent)
    }

    override fun hasCustomTitle() : Boolean {
        return true
    }

    override fun getTitleResource() : Int {
        //TODO: Pendent
        return R.string.app_name
    }

    //region CHECK_LOCATION_PERMISSION
    private fun checkPermissions() {
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionMicrophone = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                if (permissions.isNotEmpty()) {
                    for ((index, permission: String) in permissions.withIndex()) {
                        when (permission) {
                            Manifest.permission.ACCESS_FINE_LOCATION -> onLocationPermission(
                                grantResults[index]
                            )
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