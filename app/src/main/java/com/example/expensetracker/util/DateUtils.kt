package com.example.expensetracker.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val monthFormatter = DateTimeFormatter.ofPattern("yyyy年M月")

fun YearMonth.formatChinese(): String = this.format(monthFormatter)

fun Long.toLocalDateCompat(): LocalDate = LocalDate.ofEpochDay(this)
