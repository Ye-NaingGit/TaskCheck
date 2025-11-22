package com.yenaing.apps.tasks.util.preference

import com.yenaing.apps.tasks.model.SortOrder

data class FilterPreferences(
    val sortOrder: SortOrder,
    val viewType: Boolean,
)