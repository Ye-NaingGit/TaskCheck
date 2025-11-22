package com.yenaing.apps.tasks.ui.navigation

import com.yenaing.apps.tasks.util.NavigationConstants.Screen.NOTIFICATION
import com.yenaing.apps.tasks.util.NavigationConstants.Screen.OVERVIEW
import com.yenaing.apps.tasks.util.NavigationConstants.Screen.SETTING
import com.yenaing.apps.tasks.util.NavigationConstants.Screen.SUBTASK
import com.yenaing.apps.tasks.util.NavigationConstants.Screen.TASK

sealed class NavigationItem(val route: String) {
    object Task : NavigationItem(TASK)
    object SubTask : NavigationItem(SUBTASK)
    object Overview : NavigationItem(OVERVIEW)
    object Setting : NavigationItem(SETTING)
    object Notification : NavigationItem(NOTIFICATION)
}