package com.example.madlabexam4

import java.util.Date

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val priority: Priority,
    val deadline: Date
)

enum class Priority {
    High,
    Medium,
    Low
}