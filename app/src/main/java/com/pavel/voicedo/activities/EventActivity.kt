package com.pavel.voicedo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.models.Event
import com.pavel.voicedo.voice.ActionParser
import com.pavel.voicedo.voice.Speaker
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class EventActivity : ListenableActivity() {
    companion object {
        private const val DATE_PATTERN = "(\\d\\d?)(th|nd|st|rd) of (.*)"
        private const val TIME_PATTERN_1 = "(\\d\\d?):(\\d\\d) (a.m.|p.m.|am|pm)"
        private const val TIME_PATTERN_2 = "(\\d\\d?) (a.m.|p.m.|am|pm)"

        enum class EnumStatus {
            VIEW, SAY_NAME, SAY_DATE, SAY_TIME, WAITING_CONFIRMATION
        }
    }

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_description)
    lateinit var inputDescription: TextView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_time)
    lateinit var inputTime: TextView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_date)
    lateinit var inputDate: TextView

    private lateinit var event : Event
    private var status : EnumStatus = EnumStatus.VIEW
    private var timeSet : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event)
        ButterKnife.bind(this)

        if (intent.hasExtra(MainActivity.PARAMS.EVENT)) {
            event = intent.getSerializableExtra(MainActivity.PARAMS.EVENT) as Event
            event.id = intent.getLongExtra(MainActivity.PARAMS.EVENT_ID, -1)
            timeSet = true
            status = EnumStatus.VIEW
        } else {
            event = Event()
            timeSet = false
            status = EnumStatus.SAY_DATE
        }
    }

    override fun onInit(status: Int) {
        if (Speaker.onInit(status)) {
            updateUI()
        }
    }

    override fun onNoOrderFound() {
        if (this.isWaitingInput() && event.date != null && event.description.isNotEmpty())
            this.status = EnumStatus.VIEW
        super.onNoOrderFound()
    }

    override fun onBackPressed() {
        if (event.id == null && event.description.isNotEmpty() && this.status != EnumStatus.WAITING_CONFIRMATION) {
            this.status = EnumStatus.WAITING_CONFIRMATION
            showListenable(false)
            Speaker.speak(R.string.response_confirm_exit, listenerLabel, true)
        } else super.onBackPressed()
    }

    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf()
        when (status) {
            EnumStatus.VIEW -> {
                list.add(resources.getString(R.string.remove_event_help))
                list.add(resources.getString(R.string.edit_event_name_help))
                list.add(resources.getString(R.string.edit_event_date_help))
                list.add(resources.getString(R.string.edit_event_back_help))
            }
            EnumStatus.WAITING_CONFIRMATION -> {
                list.add(resources.getString(R.string.confirmation_help))
                list.add(resources.getString(R.string.cancelation_help))
            }
            EnumStatus.SAY_NAME -> list.add(resources.getString(R.string.say_event_name_help))
            else -> list.add(resources.getString(R.string.say_event_date_help))
        }
        return list
    }

    override fun onResult(action: ActionParser.Action) {
        if (this.status == EnumStatus.WAITING_CONFIRMATION) {
            when (action.action) {
                ActionParser.Action.ActionType.CONFIRMATION -> this.finish()
                else -> {
                    hideListenable()
                    this.status = EnumStatus.VIEW
                    updateUI()
                }
            }
        } else {
            when (action.action) {
                ActionParser.Action.ActionType.CHANGE_EVENT_NAME -> {
                    status = EnumStatus.SAY_NAME
                    updateUI()
                }
                ActionParser.Action.ActionType.CHANGE_EVENT_DATE -> {
                    status = EnumStatus.SAY_DATE
                    updateUI()
                }
                ActionParser.Action.ActionType.CHANGE_EVENT_TIME -> {
                    status = EnumStatus.SAY_TIME
                    updateUI()
                }
                ActionParser.Action.ActionType.FINISH_EDITION -> {
                    event.save(this)
                    status = EnumStatus.VIEW
                    Speaker.speak(R.string.event_saved, null, false)
                    hideListenable()
                    updateUI()
                }
                ActionParser.Action.ActionType.BACK,
                ActionParser.Action.ActionType.CANCELLATION -> {
                    if (isWaitingInput() && event.date != null) {
                        if (!timeSet) this.status = EnumStatus.SAY_TIME
                        else if (event.description.isEmpty()) this.status = EnumStatus.SAY_NAME
                        else this.status = EnumStatus.VIEW
                        updateUI()
                    } else this.onBackPressed()
                }
                ActionParser.Action.ActionType.INPUT -> {
                    when (status) {
                        EnumStatus.SAY_NAME -> onInputName(action)
                        EnumStatus.SAY_DATE -> onInputDate(action)
                        EnumStatus.SAY_TIME -> onInputTime(action)
                        else -> onInvalidAction()
                    }
                }
                ActionParser.Action.ActionType.DELETE_LIST -> {
                    event.delete(this)
                    Speaker.speak(R.string.response_removing_event, null)
                    finish()
                }
                else -> onInvalidAction()
            }
        }
    }

    private fun onInputName(action: ActionParser.Action) {
        event.description = action.param!!

        if (event.id != null) event.save(this)
        status = EnumStatus.VIEW

        hideListenable()
        updateUI()
    }

    private fun onInputTime(action: ActionParser.Action) {
        try {
            if (ActionParser.matches(action.param!!, TIME_PATTERN_1)) {
                val formatter = DateTimeFormat.forPattern("hh:mm aa")
                val date = formatter.parseDateTime(action.param.replace(".", ""))
                setDate(date)
            } else if (ActionParser.matches(action.param!!, TIME_PATTERN_2)) {
                val formatter = DateTimeFormat.forPattern("hh aa")
                val date = formatter.parseDateTime(action.param.replace(".", ""))
                setDate(date)
            } else Speaker.speak(R.string.response_invalid_time, listenerLabel, true)
        } catch (e: Exception) {
            Speaker.speak(R.string.response_invalid_time, listenerLabel, true)
        }
    }

    private fun setDate(date: DateTime) {
        val eventDate = DateTime(event.date)
        event.date = DateTime(eventDate.year, eventDate.monthOfYear, eventDate.dayOfMonth, date.hourOfDay, date.minuteOfHour).toDate()
        timeSet = true

        if (event.id != null) {
            event.save(this)
            status = EnumStatus.VIEW
            hideListenable()
        } else status = EnumStatus.SAY_NAME

        updateUI()
    }

    @SuppressLint("SimpleDateFormat")
    fun onInputDate(action: ActionParser.Action) {
        if (ActionParser.matches(action.param!!, DATE_PATTERN)) {
            val compiledPattern = Pattern.compile(DATE_PATTERN)
            val matcher = compiledPattern.matcher(action.param.toLowerCase(Locale.ROOT))
            matcher.matches()

            val day = matcher.group(1)!!.trim()
            val month = matcher.group(matcher.groupCount())!!.trim()

            try {
                val monthParsed : Int = DateTime(SimpleDateFormat("MMMM").parse(month)).monthOfYear
                val dayParsed : Int = Integer.parseInt(day)

                if (dayParsed < 1 || dayParsed > 31)
                    throw Exception("Unable to parse day")

                var date = DateTime(DateTime().year, monthParsed, (dayParsed), 0, 0)
                if (DateTime().isAfter(date))
                    date = DateTime(DateTime().year + 1, monthParsed, (dayParsed), 0, 0)

                if (timeSet) {
                    val eventDate = DateTime(event.date)
                    event.date = DateTime(date.year, date.monthOfYear, date.dayOfMonth, eventDate.hourOfDay, eventDate.minuteOfHour).toDate()
                }
                else event.date = date.toDate()

                if (event.id != null) {
                    event.save(this)
                    status = EnumStatus.VIEW
                    hideListenable()
                } else status = EnumStatus.SAY_TIME

                updateUI()
            } catch (e: Exception) {
                Speaker.speak(R.string.response_invalid_date, listenerLabel, true)
            }
        } else Speaker.speak(R.string.response_invalid_date, listenerLabel, true)
    }

    override fun updateUI() {
        inputDescription.text = event.description

        if (event.date != null) {
            if (timeSet) inputTime.text = event.getStringTime()
            inputDate.text = event.getStringLongDate()
        } else {
            inputTime.text = ""
            inputDate.text = ""
        }

        updateTitle()

        when (status) {
            EnumStatus.SAY_NAME -> {
                showListenable(false)
                Speaker.speak(R.string.ask_event_name, listenerLabel)
            }
            EnumStatus.SAY_DATE -> {
                showListenable(false)
                Speaker.speak(R.string.ask_event_date, listenerLabel)
            }
            EnumStatus.SAY_TIME -> {
                showListenable(false)
                Speaker.speak(R.string.ask_event_time, listenerLabel)
            }
            else -> {}
        }
    }

    override fun isWaitingInput() : Boolean {
        return status != EnumStatus.VIEW && status != EnumStatus.WAITING_CONFIRMATION
    }

    override fun hasCustomTitle() : Boolean {
        return true
    }

    override fun getTitleResource() : Int {
        return if (event.id != null) R.string.view_event
        else R.string.create_event
    }

    override fun onClickListen() {
        showListenable(true)
        when (this.status) {
            EnumStatus.SAY_NAME -> Speaker.speak(R.string.ask_event_name, listenerLabel)
            EnumStatus.SAY_DATE -> Speaker.speak(R.string.ask_event_date, listenerLabel)
            EnumStatus.SAY_TIME -> Speaker.speak(R.string.ask_event_time, listenerLabel)
            EnumStatus.WAITING_CONFIRMATION -> Speaker.speak(R.string.response_confirm_exit, listenerLabel, true)
            else -> Speaker.speak(R.string.response_how_can_help, listenerLabel)
        }
    }
}