package com.pavel.voicedo.voice

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.io.Serializable
import java.util.*

class ActionParser {
    class Action(val action: ActionType, val param: String?) : Serializable {

        enum class ActionType {
            CREATE_TASK, CREATE_EVENT, CREATE_LIST,
            DELETE_TASK, DELETE_EVENT, DELETE_LIST,
            VIEW_TASK, VIEW_EVENT, VIEW_LIST,
            SHOW_ALL_TASKS, SHOW_ALL_EVENTS, SHOW_ALL_LISTS,
            SHOW_UNDONE_TASKS, SHOW_TASKS_IN_PROCESS,
            SHOW_EVENTS_DAY, SHOW_EVENTS_CURRENT_WEEK, SHOW_EVENTS_NEXT_WEEK,
            SHOW_LOCATION, INPUT, BACK, EDIT_NAME,
            EDIT_STATE, CLEAR_DATE, EDIT_DATE, NOT_UNDERSTAND, NOT_EXPECTED, HELP,
            ADD_PRODUCT, REMOVE_PRODUCT, CHECK_PRODUCT,
            CHANGE_LIST_NAME, CHANGE_TASK_NAME, CHANGE_EVENT_NAME,
            CONFIRMATION, CANCELATION, FINISH_EDITION
        }
    }

    companion object {
        //TASKS PATTERNS
        private const val VIEW_TASK_PATTERN = "((view|show|open) task)(.*)"
        private const val CREATE_TASK_PATTERN = "((create|new|insert) task)(.*)"
        private const val DELETE_TASK_PATTERN = "((delete|remove|clear) task)(.*)"

        private const val VIEW_EVENT_PATTERN = "((view|show|open) event)(.*)"
        private const val CREATE_EVENT_PATTERN = "((create|new|insert) event)(.*)"
        private const val DELETE_EVENT_PATTERN = "((delete|remove|clear) event)(.*)"

        private const val VIEW_LIST_PATTERN = "((view|show|open) list)(.*)"
        private const val CREATE_LIST_PATTERN = "((create|new|insert) list)(.*)"
        private const val DELETE_LIST_PATTERN = "((delete|remove|clear) list)(.*)"
        private const val ADD_PRODUCT_PATTERN = "((add|create) product)(.*)"
        private const val REMOVE_PRODUCT_PATTERN = "((remove|delete) product)(.*)"
        private const val CHECK_PRODUCT_PATTERN = "((marc|mark|check) product)(.*)"
        //END TASKS PATTERNS

        //QUERIES AND HELP PATTERNS
        private const val HELP_PATTERN = "(help|((show|view) help))(.*)"
        private const val SHOW_ALL_TASKS_PATTERN = "((show|view) all (tasks|task))(.*)"
        private const val SHOW_ALL_EVENTS_PATTERN = "((show|view) all (events|event))(.*)"
        private const val SHOW_ALL_LISTS_PATTERN = "((show|view) all (lists|list))(.*)"
        private const val SHOW_UNDONE_TASKS_PATTERN = "((show|view)( all)? undone (tasks|task))(.*)"
        private const val SHOW_TASKS_IN_PROCESS_PATTERN = "((show|view)( all)? (tasks|task) in (process|progress))(.*)"
        private const val SHOW_EVENTS_DAY_PATTERN = "((show|view) (events|event) on)(.*)"
        private const val SHOW_EVENTS_CURRENT_WEEK_PATTERN = "((show|view) (events|event) this week)(.*)"
        private const val SHOW_EVENTS_NEXT_WEEK_PATTERN = "((show|view) (events|event) tomorrow)(.*)"
        private const val SHOW_LOCATION_PATTERN = "((view|show)( my)? location)(.*)"
        //END QUERIES AND HELP PATTERNS

        //INPUT PATTERNS
        private const val CONFIRM_PATTERN = "(yes|confirm|accept)(.*)"
        private const val CANCEL_PATTERN = "(no|cancel|back)(.*)"
        private const val BACK_PATTERN = "(close|cancel|close|end|back)(.*)"
        private const val FINISH_EDITION_PATTERN = "((confirm|finish|end) (edition|creation))(.*)"

        private const val CHANGE_LIST_NAME_PATTERN = "((edit|change) list name)(.*)"
        private const val CHANGE_TASK_NAME_PATTERN = "((edit|change) task name)(.*)"
        private const val CHANGE_EVENT_NAME_PATTERN = "((edit|change) event name)(.*)"
        //END INPUT PATTERNS

        fun parse(raw_action: String, expected_orders: List<Action.ActionType>) : Action {
            val action = raw_action.toLowerCase(Locale.ROOT)

            val oRes = when {
                //QUERIES
                matches(action, SHOW_ALL_TASKS_PATTERN) -> getAction(Action.ActionType.SHOW_ALL_TASKS, action, SHOW_ALL_TASKS_PATTERN)
                matches(action, SHOW_ALL_EVENTS_PATTERN) -> getAction(Action.ActionType.SHOW_ALL_EVENTS, action, SHOW_ALL_EVENTS_PATTERN)
                matches(action, SHOW_ALL_LISTS_PATTERN) -> getAction(Action.ActionType.SHOW_ALL_LISTS, action, SHOW_ALL_LISTS_PATTERN)
                matches(action, SHOW_UNDONE_TASKS_PATTERN) -> getAction(Action.ActionType.SHOW_UNDONE_TASKS, action, SHOW_UNDONE_TASKS_PATTERN)
                matches(action, SHOW_TASKS_IN_PROCESS_PATTERN) -> getAction(Action.ActionType.SHOW_TASKS_IN_PROCESS, action, SHOW_TASKS_IN_PROCESS_PATTERN)
                matches(action, SHOW_EVENTS_DAY_PATTERN) -> getAction(Action.ActionType.SHOW_EVENTS_DAY, action, SHOW_EVENTS_DAY_PATTERN)
                matches(action, SHOW_EVENTS_CURRENT_WEEK_PATTERN) -> getAction(Action.ActionType.SHOW_EVENTS_CURRENT_WEEK, action, SHOW_EVENTS_CURRENT_WEEK_PATTERN)
                matches(action, SHOW_EVENTS_NEXT_WEEK_PATTERN) -> getAction(Action.ActionType.SHOW_EVENTS_NEXT_WEEK, action, SHOW_EVENTS_NEXT_WEEK_PATTERN)
                matches(action, SHOW_LOCATION_PATTERN) -> getAction(Action.ActionType.SHOW_LOCATION, action, SHOW_LOCATION_PATTERN)
                matches(action, HELP_PATTERN) -> getAction(Action.ActionType.HELP, action, HELP_PATTERN)
                //TASKS
                matches(action, VIEW_TASK_PATTERN) -> getAction(Action.ActionType.VIEW_TASK, action, VIEW_TASK_PATTERN, true)
                matches(action, DELETE_TASK_PATTERN) -> getAction(Action.ActionType.DELETE_TASK, action, DELETE_TASK_PATTERN, true)
                matches(action, CREATE_TASK_PATTERN) -> getAction(Action.ActionType.CREATE_TASK, action, CREATE_TASK_PATTERN)
                //EVENTS
                matches(action, VIEW_EVENT_PATTERN) -> getAction(Action.ActionType.VIEW_EVENT, action, VIEW_EVENT_PATTERN, true)
                matches(action, DELETE_EVENT_PATTERN) -> getAction(Action.ActionType.DELETE_EVENT, action, DELETE_EVENT_PATTERN, true)
                matches(action, CREATE_EVENT_PATTERN) -> getAction(Action.ActionType.CREATE_EVENT, action, CREATE_EVENT_PATTERN)
                //LISTS
                matches(action, VIEW_LIST_PATTERN) -> getAction(Action.ActionType.VIEW_LIST, action, VIEW_LIST_PATTERN, true)
                matches(action, DELETE_LIST_PATTERN) -> getAction(Action.ActionType.DELETE_LIST, action, DELETE_LIST_PATTERN, true)
                matches(action, CREATE_LIST_PATTERN) -> getAction(Action.ActionType.CREATE_LIST, action, CREATE_LIST_PATTERN)
                matches(action, ADD_PRODUCT_PATTERN) -> getAction(Action.ActionType.ADD_PRODUCT, action, ADD_PRODUCT_PATTERN, true)
                matches(action, REMOVE_PRODUCT_PATTERN) -> getAction(Action.ActionType.REMOVE_PRODUCT, action, REMOVE_PRODUCT_PATTERN, true)
                matches(action, CHECK_PRODUCT_PATTERN) -> getAction(Action.ActionType.CHECK_PRODUCT, action, CHECK_PRODUCT_PATTERN, true)
                //INPUT
                matches(action, CONFIRM_PATTERN) -> getAction(Action.ActionType.CONFIRMATION, action, CONFIRM_PATTERN)
                matches(action, BACK_PATTERN) -> getAction(Action.ActionType.BACK, action, BACK_PATTERN)
                matches(action, CANCEL_PATTERN) -> getAction(Action.ActionType.CANCELATION, action, CANCEL_PATTERN)
                matches(action, FINISH_EDITION_PATTERN) -> getAction(Action.ActionType.FINISH_EDITION, action, FINISH_EDITION_PATTERN)
                matches(action, CHANGE_EVENT_NAME_PATTERN) -> getAction(Action.ActionType.CHANGE_EVENT_NAME, action, CHANGE_EVENT_NAME_PATTERN)
                matches(action, CHANGE_LIST_NAME_PATTERN) -> getAction(Action.ActionType.CHANGE_LIST_NAME, action, CHANGE_LIST_NAME_PATTERN)
                matches(action, CHANGE_TASK_NAME_PATTERN) -> getAction(Action.ActionType.CHANGE_TASK_NAME, action, CHANGE_TASK_NAME_PATTERN)

                else -> Action(Action.ActionType.NOT_UNDERSTAND, "")
            }

            return if (expected_orders.isEmpty() || expected_orders.contains(oRes.action) || oRes.action == Action.ActionType.NOT_UNDERSTAND) oRes
            else Action(Action.ActionType.NOT_EXPECTED, "")
        }

        private fun matches(action: String, pattern: String) : Boolean {
            val compiledPattern: Pattern = Pattern.compile(pattern)
            val matcher: Matcher = compiledPattern.matcher(action)
            return matcher.matches()
        }

        private fun getAction(action_type: Action.ActionType, action: String, pattern: String, hasParams : Boolean = false) : Action {
            val compiledPattern = Pattern.compile(pattern)
            val matcher = compiledPattern.matcher(action.toLowerCase(Locale.ROOT))
            matcher.matches()

            if (hasParams) return Action(action_type, matcher.group(matcher.groupCount())!!.trim())
            else return Action(action_type, "")
        }
    }
}