package com.pavel.voicedo.voice

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.io.Serializable

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
            EDIT_STATE, CLEAR_DATE, EDIT_DATE, NOT_UNDERSTAND, HELP
        }
    }

    companion object {
        //TASKS PATTERNS
        private val VIEW_TASK_PATTERN = "((view|show) task).*";
        private val CREATE_TASK_PATTERN = "((create|new|insert) task).*"
        private val DELETE_TASK_PATTERN = "((delete|remove|clear) task).*"
        //END TASKS PATTERNS



        //QUERIES AND HELP PATTERNS
        private val HELP_PATTERN = ""
        //END QUERIES AND HELP PATTERNS

        //INPUT PATTERNS
        //END INPUT PATTERNS

        fun parse(action: String) : Action {
            return when {
                matches(action, VIEW_TASK_PATTERN) -> getAction(Action.eActionType.VIEW_TASK, action, VIEW_TASK_PATTERN, true)
                matches(action, CREATE_TASK_PATTERN) -> getAction(Action.eActionType.CREATE_TASK, action, CREATE_TASK_PATTERN)
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
            val matcher = compiledPattern.matcher(action)

            if (hasParams) return Action(action_type, matcher.group(1))
            else return Action(action_type, "")
        }
    }
}