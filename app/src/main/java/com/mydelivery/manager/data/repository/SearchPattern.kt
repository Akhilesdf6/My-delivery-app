package com.mydelivery.manager.data.repository

/** Turns user text into a safe LIKE pattern so "%" and "_" are matched literally. */
internal fun containsPattern(query: String): String {
    val escaped = query.trim()
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
    return "%$escaped%"
}
