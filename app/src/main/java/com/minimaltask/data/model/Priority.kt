package com.minimaltask.data.model

enum class Priority(val value: Int, val label: String) {
    LOW(0, "Bassa"),
    MEDIUM(1, "Media"),
    HIGH(2, "Alta");

    companion object {
        fun fromValue(value: Int): Priority = entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}
