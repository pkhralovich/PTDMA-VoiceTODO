package com.pavel.voicedo.voice

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.io.Serializable
import java.util.*

class ActionParser {
    class Action(action: eActionType, param: String?) : Serializable {
        val action : eActionType = action
        val param : String? = param

        enum class eActionType {
            CREATE_TASK, CREATE_EVENT, CREATE_LIST,
            DELETE_TASK, DELETE_EVENT, DELETE_LIST,
            VIEW_TASK, VIEW_EVENT, VIEW_LIST,
            SHOW_ALL_TASKS, SHOW_ALL_EVENTS, SHOW_ALL_LISTS,
            SHOW_UNDONE_TASKS, SHOW_TASKS_IN_PROCESS,
            SHOW_EVENTS_DAY, SHOW_EVENTS_CURRENT_WEEK, SHOW_EVENTS_NEXT_WEEK,
            SHOW_LOCATION, INPUT, CLEAR_NAME, CONFIRM_NAME, BACK, EDIT_NAME,
            EDIT_STATE, CLEAR_DATE, EDIT_DATE, NOT_UNDERSTAND, HELP,
            CONFIRMATION, CANCELATION
        }
    }

    companion object {
        //TASKS PATTERNS
        private val VIEW_TASK_PATTERN = "((view|show|open) task)(.*)"
        private val CREATE_TASK_PATTERN = "((create|new|insert) task)(.*)"
        private val DELETE_TASK_PATTERN = "((delete|remove|clear) task)(.*)"

        private val VIEW_EVENT_PATTERN = "((view|show|open) event)(.*)"
        private val CREATE_EVENT_PATTERN = "((create|new|insert) event)(.*)"
        private val DELETE_EVENT_PATTERN = "((delete|remove|clear) event)(.*)"

        private val VIEW_LIST_PATTERN = "((view|show|open) list)(.*)"
        private val CREATE_LIST_PATTERN = "((create|new|insert) list)(.*)"
        private val DELETE_LIST_PATTERN = "((delete|remove|clear) list)(.*)"
        //END TASKS PATTERNS

        //QUERIES AND HELP PATTERNS
        private val HELP_PATTERN = "(help|((show|view) help))(.*)"
        //END QUERIES AND HELP PATTERNS

        //INPUT PATTERNS
        private val CONFIRM_PATTERN = "(yes|confirm|accept)(.*)"
        private val CANCEL_PATTERN = "(no|cancel|back)(.*)"
        //END INPUT PATTERNS

        fun parse(action: String) : Action {
            var action = "delete event one"

            return when {
                matches(action, VIEW_TASK_PATTERN) -> getAction(Action.eActionType.VIEW_TASK, action, VIEW_TASK_PATTERN, true)
                matches(action, DELETE_TASK_PATTERN) -> getAction(Action.eActionType.DELETE_TASK, action, DELETE_TASK_PATTERN, true)
                matches(action, CREATE_TASK_PATTERN) -> getAction(Action.eActionType.CREATE_TASK, action, CREATE_TASK_PATTERN)
                matches(action, VIEW_EVENT_PATTERN) -> getAction(Action.eActionType.VIEW_EVENT, action, VIEW_EVENT_PATTERN, true)
                matches(action, DELETE_EVENT_PATTERN) -> getAction(Action.eActionType.DELETE_EVENT, action, DELETE_EVENT_PATTERN, true)
                matches(action, CREATE_EVENT_PATTERN) -> getAction(Action.eActionType.CREATE_EVENT, action, CREATE_EVENT_PATTERN)
                matches(action, VIEW_LIST_PATTERN) -> getAction(Action.eActionType.VIEW_LIST, action, VIEW_LIST_PATTERN, true)
                matches(action, DELETE_LIST_PATTERN) -> getAction(Action.eActionType.DELETE_LIST, action, DELETE_LIST_PATTERN, true)
                matches(action, CREATE_LIST_PATTERN) -> getAction(Action.eActionType.CREATE_LIST, action, CREATE_LIST_PATTERN)
                matches(action, HELP_PATTERN) -> getAction(Action.eActionType.HELP, action, HELP_PATTERN)
                matches(action, CONFIRM_PATTERN) -> getAction(Action.eActionType.CONFIRMATION, action, CONFIRM_PATTERN)
                matches(action, CANCEL_PATTERN) -> getAction(Action.eActionType.CANCELATION, action, CANCEL_PATTERN)
                else -> Action(Action.eActionType.NOT_UNDERSTAND, "")
            }
        }

        private fun matches(action: String, pattern: String) : Boolean {
            val compiledPattern: Pattern = Pattern.compile(pattern)
            val matcher: Matcher = compiledPattern.matcher(action)
            return matcher.matches()
        }

        private fun getAction(action_type: Action.eActionType, action: String, pattern: String, hasParams : Boolean = false) : Action {
            val compiledPattern = Pattern.compile(pattern)
            val matcher = compiledPattern.matcher(action.toLowerCase(Locale.ROOT))
            matcher.matches()

            if (hasParams) return Action(action_type, matcher.group(matcher.groupCount()).trim())
            else return Action(action_type, "")
        }
    }
}