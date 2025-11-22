package com.yenaing.apps.tasks.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yenaing.apps.tasks.data.model.Notification
import com.yenaing.apps.tasks.data.model.SubTask
import com.yenaing.apps.tasks.data.model.Task

@Database(
    entities = [Task::class, SubTask::class, Notification::class],
    version = 5
)
abstract class TaskDatabase : RoomDatabase() {
    abstract val dao: TaskDao
    abstract val notificationDao: NotificationDao
}
