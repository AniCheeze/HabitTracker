package com.example.habittracker

data class Habit(
    val id: Int,
    val name: String,
    val minutes: Int,
    val category: String,
    val required: Boolean = false,
    val daily: Boolean = false,
    val difficulty: Int = 1,
    val done: Boolean = false
)