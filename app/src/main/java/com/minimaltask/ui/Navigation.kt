package com.minimaltask.ui

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object AddTask : Route("add_task")
    data object EditTask : Route("edit_task/{taskId}") {
        fun create(taskId: Int) = "edit_task/$taskId"
    }
    data object Focus : Route("focus")
    data object Stats : Route("stats")
    data object Settings : Route("settings")
    data object Premium : Route("premium")
}
