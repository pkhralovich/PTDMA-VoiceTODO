package com.pavel.voicedo.models

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat.checkSelfPermission
import com.pavel.voicedo.R
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import java.util.*


class Event : BaseTask(EnumTypes.EVENT) {
    companion object {
        private val EVENT_INSTANCE_PROJECTION = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.TITLE
        )

        private const val EVENT_PROJECTION_ID_INDEX = 0
        private const val EVENT_PROJECTION_BEGIN_INDEX = 1
        private const val EVENT_PROJECTION_TITLE_INDEX = 2

        private const val CALENDAR_PROJECTION_ID_INDEX = 0
        private var mainCalendar : Long = -1

        private fun getEventsOrder(): String {
            return CalendarContract.Instances.BEGIN + " ASC"
        }

        private fun getEventsQuery(): Uri {
            val jodaCurrentTime = DateTime()
            val jodaEndTime = jodaCurrentTime.plusDays(7)

            val beginTime = Calendar.getInstance()
            beginTime.set(jodaCurrentTime.year, jodaCurrentTime.monthOfYear - 1, jodaCurrentTime.dayOfMonth, 0, 0, 0)
            val startMillis = beginTime.timeInMillis

            val endTime = Calendar.getInstance()
            endTime.set(jodaEndTime.year, jodaEndTime.monthOfYear - 1, jodaEndTime.dayOfMonth, 23, 59, 59)
            val endMillis = endTime.timeInMillis

            val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startMillis)
            ContentUris.appendId(builder, endMillis)

            return builder.build()
        }

        private fun getCurrentWeekQuery() : Uri {
            val now = LocalDate()
            val currentWeekBegin = now.withDayOfWeek(DateTimeConstants.MONDAY)
            val beginTime = Calendar.getInstance()
            beginTime.set(currentWeekBegin.year, currentWeekBegin.monthOfYear-1, currentWeekBegin.dayOfMonth, 0, 0)

            val currentWeekEnd = DateTime(beginTime).plusDays(6)
            val endTime = Calendar.getInstance()
            endTime.set(currentWeekEnd.year, currentWeekEnd.monthOfYear-1, currentWeekEnd.dayOfMonth, 23, 59)

            val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, beginTime.timeInMillis)
            ContentUris.appendId(builder, endTime.timeInMillis)

            return builder.build()
        }

        private fun getNextWeekQuery() : Uri {
            val now = LocalDate().plusDays(7)
            val currentWeekBegin = now.withDayOfWeek(DateTimeConstants.MONDAY)
            val beginTime = Calendar.getInstance()
            beginTime.set(currentWeekBegin.year, currentWeekBegin.monthOfYear-1, currentWeekBegin.dayOfMonth, 0, 0)

            val currentWeekEnd = DateTime(beginTime).plusDays(6)
            val endTime = Calendar.getInstance()
            endTime.set(currentWeekEnd.year, currentWeekEnd.monthOfYear-1, currentWeekEnd.dayOfMonth, 23, 59)

            val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, beginTime.timeInMillis)
            ContentUris.appendId(builder, endTime.timeInMillis)

            return builder.build()
        }

        private fun getMainCalendar(c: Context): Long {
            if (mainCalendar < 0 && checkSelfPermission(c, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                val selection = CalendarContract.Calendars.VISIBLE + "=1 AND " + CalendarContract.Calendars.IS_PRIMARY + "=1"
                val cur: Cursor? = c.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, arrayOf(CalendarContract.Calendars._ID), selection, null, null)
                if (cur != null && cur.moveToNext()) {
                    mainCalendar = cur.getLong(CALENDAR_PROJECTION_ID_INDEX)
                    cur.close()
                }
            }

            return mainCalendar
        }

        fun listCurrentWeek(c: Context) : List<Event> {
            if (checkSelfPermission(c, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                return listOf()
            }

            val selection = CalendarContract.Instances.ALL_DAY + "= 0 AND " + CalendarContract.Instances.CALENDAR_ID + " = ?"
            val selectionArgs = arrayOf(getMainCalendar(c).toString())
            val cur: Cursor? = c.contentResolver.query(getCurrentWeekQuery(), EVENT_INSTANCE_PROJECTION, selection, selectionArgs, getEventsOrder())
            return parseEvents(cur!!)
        }

        fun listNextWeek(c: Context) : List<Event> {
            if (checkSelfPermission(c, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                return listOf()
            }

            val selection = CalendarContract.Instances.ALL_DAY + "= 0 AND " + CalendarContract.Instances.CALENDAR_ID + " = ?"
            val selectionArgs = arrayOf(getMainCalendar(c).toString())
            val cur: Cursor? = c.contentResolver.query(getNextWeekQuery(), EVENT_INSTANCE_PROJECTION, selection, selectionArgs, getEventsOrder())
            return parseEvents(cur!!)
        }

        fun listAll(c: Context): List<Event> {
            if (checkSelfPermission(c, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                return listOf()
            }

            val selection = CalendarContract.Instances.ALL_DAY + "= 0 AND " + CalendarContract.Instances.CALENDAR_ID + " = ?"
            val selectionArgs = arrayOf(getMainCalendar(c).toString())
            val cur: Cursor? = c.contentResolver.query(getEventsQuery(), EVENT_INSTANCE_PROJECTION, selection, selectionArgs, getEventsOrder())
            return parseEvents(cur!!)
        }

        private fun parseEvents(cur: Cursor) : List<Event> {
            val events: ArrayList<Event> = ArrayList()
            var currentId : Long = 1

            while (cur.moveToNext()) {
                val eventID = cur.getLong(EVENT_PROJECTION_ID_INDEX)
                val beginVal = cur.getLong(EVENT_PROJECTION_BEGIN_INDEX)
                val title = cur.getString(EVENT_PROJECTION_TITLE_INDEX)

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = beginVal

                val event = Event()
                event.id = currentId
                event.description = title
                event.date = calendar.time
                event.internalId = eventID

                events.add(event)
                currentId++
            }
            cur.close()

            return events
        }
    }

    var date: Date? = null
    var internalId : Long = -1

    fun getStringDate(): String {
        return "${DateTime(date).toString("dd")}\n${DateTime(date).toString("MMM")}"
    }

    fun getStringLongDate() : String {
        return DateTime(date).toString("dd.MM.YYYY")
    }

    fun getStringTime() : String {
        return DateTime(date).toString("hh:mm aa")
    }

    fun save(c: Context) : Boolean {
        if (checkSelfPermission(c, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) return false

        val beginDate = DateTime(this.date)
        val endDate = beginDate.plusHours(1)

        val beginTime = Calendar.getInstance()
        beginTime.set(beginDate.year, beginDate.monthOfYear - 1, beginDate.dayOfMonth, beginDate.hourOfDay, beginDate.minuteOfHour)

        val endTime = Calendar.getInstance()
        beginTime.set(endDate.year, endDate.monthOfYear - 1, endDate.dayOfMonth, endDate.hourOfDay, endDate.minuteOfHour)


        if (this.internalId < 0) {
            val values = ContentValues()
            values.put(CalendarContract.Events.DTSTART, beginTime.timeInMillis)
            values.put(CalendarContract.Events.DTEND, endTime.timeInMillis)
            values.put(CalendarContract.Events.TITLE, this.description)
            values.put(CalendarContract.Events.CALENDAR_ID, getMainCalendar(c))
            values.put(CalendarContract.Events.ORGANIZER, c.resources.getString(R.string.app_name))
            values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().timeZone.toString())
            val uri = c.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

            this.internalId = uri!!.lastPathSegment!!.toLong()
            this.id = 1 //Forma guarra de dir que ha estat creat
        } else {
            val values = ContentValues()
            values.put(CalendarContract.Events.TITLE, this.description)
            values.put(CalendarContract.Instances.DTSTART, beginTime.timeInMillis)
            values.put(CalendarContract.Events.DTEND, endTime.timeInMillis)
            val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, this.internalId)
            c.contentResolver.update(updateUri, values, null, null)
        }

        return true
    }

    fun delete(c: Context): Boolean {
        val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, this.internalId)
        val rows: Int = c.contentResolver.delete(deleteUri, null, null)
        return rows > 0
    }
}