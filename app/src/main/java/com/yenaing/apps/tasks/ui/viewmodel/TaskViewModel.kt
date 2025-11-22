package com.yenaing.apps.tasks.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.yenaing.apps.tasks.R
import com.yenaing.apps.tasks.model.SortOrder
import com.yenaing.apps.tasks.data.model.Task
import com.yenaing.apps.tasks.data.repository.TaskRepository
import com.yenaing.apps.tasks.model.TaskEvent
import com.yenaing.apps.tasks.util.AppConstants.SEARCH_INITIAL_VALUE
import com.yenaing.apps.tasks.util.AppConstants.SEARCH_QUERY
import com.yenaing.apps.tasks.util.preference.PreferenceManager
import com.yenaing.apps.tasks.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.yenaing.apps.tasks.util.preference.StreakPreferences


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager,
    state: SavedStateHandle,
) : ViewModel() {

    private val _streakFlow = MutableStateFlow(StreakPreferences(currentStreak = 0, bestStreak = 0))
    val streakFlow = _streakFlow.asStateFlow()

    private val _productivityFlow = MutableStateFlow(0)
    val productivityFlow = _productivityFlow.asStateFlow()

    init {
        viewModelScope.launch {
            preferenceManager.streakFlow
                .distinctUntilChanged()
                .collect { streak ->
                    _streakFlow.value = streak
                }
        }

        viewModelScope.launch {
            preferenceManager.productivityFlow
                .distinctUntilChanged()
                .collect { score ->
                    _productivityFlow.value = score
                }
        }
    }

    private val _taskFlow = MutableStateFlow<Task?>(null)
    val taskFlow get() = _taskFlow.asStateFlow()
    val searchQuery = state.getLiveData(SEARCH_QUERY, SEARCH_INITIAL_VALUE)
    val preferencesFlow = preferenceManager.preferencesFlow
    private val taskEventChannel = Channel<TaskEvent>()
    val tasksEvent = taskEventChannel.receiveAsFlow()



    val tasksFlow = combine(
        searchQuery.asFlow(), preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        repository.getAllTasks(
            query,
            filterPreferences.sortOrder
        )
    }.distinctUntilChanged()

    fun onSortOrderSelected(sortOrder: SortOrder, context: Context) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder, context)
    }

    //new method for layout of items
    fun onViewTypeChanged(viewType: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateViewType(viewType, context)
    }

    fun onTaskSwiped(task: Task, context: Context) = viewModelScope.launch {
        repository.deleteTask(task)
        preferenceManager.updateProductivityScore(-3, context)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean, context: Context) = viewModelScope.launch {
        val wasDone = task.isDone
        val nowDone = isChecked

        // Update the task status
        repository.updateTask(task.copy(isDone = nowDone))

        // If the task has just been completed (false -> true), update the streak
        if (!wasDone && nowDone) {
            preferenceManager.updateStreakOnTaskCompleted(context)
            preferenceManager.updateProductivityScore(+5, context)
        }
    }

    fun onUndoDeleteClick(task: Task, context: Context) = viewModelScope.launch {
        repository.insertTask(task)
        preferenceManager.updateProductivityScore(+3, context) // refund penalty
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAllCompletedScreen)
    }

    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTask(task)
    }

    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(task)
    }

    fun delete(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTask(task)
    }

    fun getById(id: Int) {
        viewModelScope.launch {
            _taskFlow.value = repository.getById(id)
        }
    }

    fun resetTaskValue() {
        _taskFlow.value = null
    }

    fun deleteCompletedTask() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllCompletedTask()
    }

    fun setTask(task: Task){
        _taskFlow.value = task
    }

    fun cancelReminderCompose(
        context: Context,
        task: Task
    ) {
        task.reminder = null
        update(task)
        context.toast {
            context.getString(R.string.cancel_reminder)
        }
    }
}